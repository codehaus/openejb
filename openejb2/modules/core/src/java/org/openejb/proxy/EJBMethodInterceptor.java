package org.openejb.proxy;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import javax.ejb.EJBException;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.NoSuchObjectLocalException;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.core.service.InvocationResult;
import org.openejb.ContainerNotFoundException;
import org.openejb.EJBComponentType;
import org.openejb.EJBContainer;
import org.openejb.EJBInterfaceType;
import org.openejb.EJBInvocation;
import org.openejb.EJBInvocationImpl;

public class EJBMethodInterceptor implements MethodInterceptor, Serializable {
    private static final long serialVersionUID = -5271735029122675779L;

    /**
     * Proxy factory for this proxy
     */
    private final EJBProxyFactory proxyFactory;

    /**
     * The type of the ejb interface.  This is used during construction of the EJBInvocation object.
     */
    private final EJBInterfaceType interfaceType;

    /**
     * Primary key of this proxy, or null if this is a home proxy.
     */
    private final Object primaryKey;

    /**
     * Has this interceptor been initialized.  Interceptor is uninitialized between deserialization and
     * the first method call.
     */
    private transient boolean initialized;

    /**
     * The container we are invokeing
     */
    private transient EJBContainer container;

    /**
     * Map from interface method ids to vop ids.
     */
    private transient int[] operationMap;

    /**
     * Metadata for the proxy
     */
    private transient ProxyInfo proxyInfo;

    /**
     * Should we copy args into the target classloader
     */
    private transient boolean shouldCopy;

    /**
     * The classloader of the proxy factory
     */
    private transient ClassLoader sourceClassLoader;

    /**
     * The classloader of the target ejb container
     */
    private transient ClassLoader targetClassLoader;

    /**
     * Does this proxy target a container in a differenct classloader?
     */
    private transient boolean crossClassLoader;

    private final static org.apache.commons.logging.Log log = LogFactory.getLog(Log.class);

    //TODO : This is pretty much a hack to introduce the ability to change from pass-by-value and pass-by-reference
    // This really should be on a bean by bean basis.
    static String localCopyProperty = null;
    static boolean openejbLocalcopy = true;
    static {
        localCopyProperty = System.getProperty("openejb.localcopy");
        if (localCopyProperty == null) localCopyProperty = "true";
        boolean openejbLocalcopy = localCopyProperty.equals("true");
        log.info("OPENEJB: Calls to remote interfaces will "+(openejbLocalcopy?"":"not ")+"have their values copied.");
    }


    public EJBMethodInterceptor(EJBProxyFactory proxyFactory, EJBInterfaceType type, EJBContainer container, int[] operationMap) {
        this(proxyFactory, type, container, operationMap, null);
    }

    public EJBMethodInterceptor(EJBProxyFactory proxyFactory, EJBInterfaceType type, EJBContainer container, int[] operationMap, Object primaryKey) {
        this.proxyFactory = proxyFactory;
        this.interfaceType = type;
        this.container = container;
        this.operationMap = operationMap;
        this.primaryKey = primaryKey;

        if (container != null) {
            sourceClassLoader = proxyFactory.getClassLoader();
            targetClassLoader = container.getClassLoader();
            crossClassLoader = isCrossClassLoader(sourceClassLoader, targetClassLoader);

            // @todo REMOVE: this is a dirty dirty dirty hack to make the old openejb code work
            // this lets really stupid clients get access to the primary key of the proxy, which is readily
            // available from several other sources
            this.proxyInfo = new ProxyInfo(container.getProxyInfo(), primaryKey);

            initialized = true;
        }

        shouldCopy = !interfaceType.isLocal() && openejbLocalcopy;
    }

    public EJBProxyFactory getProxyFactory() {
        return proxyFactory;
    }

    public ProxyInfo getProxyInfo() throws ContainerNotFoundException {
        initialize();
        return proxyInfo;
    }

    public Object getPrimaryKey() {
        return primaryKey;
    }

    public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        // make sure the interceptor has been initialized
        try {
            initialize();
        } catch (ContainerNotFoundException e) {
            if (!interfaceType.isLocal()) {
                throw new NoSuchObjectException(e.getMessage());
            } else {
                throw new NoSuchObjectLocalException(e.getMessage());
            }
        }

        EJBInvocation invocation = createEJBInvocation(method, methodProxy, args);
        if (invocation == null) {
            return null;
        }

        // copy the arguments into the target classloader
        if (shouldCopy) {
            args = invocation.getArguments();
            copyArgsToTargetCL(args);
        }

        // invoke the EJB container
        InvocationResult result;
        try {
            result = container.invoke(invocation);
        } catch (Throwable t) {
            // system exceptions must be throw as either EJBException or a RemoteException
            if (interfaceType.isLocal()) {
                if (!(t instanceof EJBException)) {
                    t = new EJBException().initCause(t);
                }
            } else {
                if (!(t instanceof RemoteException)) {
                    t = new RemoteException(t.getMessage(), t);
                }
            }
            throw t;
        }

        // get the object to return
        boolean normal = result.isNormal();
        Object returnObj;
        if (normal) {
            returnObj = result.getResult();
        } else {
            returnObj = result.getException();
        }

        if (shouldCopy && returnObj != null) {
            returnObj = copyReturnToSourceCL(returnObj);
        }

        if (normal) {
            return returnObj;
        } else {
            throw (Exception) returnObj;
        }
    }

    private EJBInvocation createEJBInvocation(Method method, MethodProxy methodProxy, Object[] args) throws Throwable {
        // extract the primary key from home ejb remove invocations
        Object id = primaryKey;

        // todo lookup id of remove to make this faster
        if ((interfaceType == EJBInterfaceType.REMOTE || interfaceType == EJBInterfaceType.LOCAL) && proxyInfo.getComponentType() == EJBComponentType.STATELESS && method.getName().equals("remove")) {
            // remove on a stateless bean does nothing
            return null;
        }

        int methodIndex = operationMap[methodProxy.getSuperIndex()];
        if (methodIndex < 0) throw new AssertionError("Unknown method: method=" + method);
        if ((interfaceType == EJBInterfaceType.HOME || interfaceType == EJBInterfaceType.LOCALHOME) && method.getName().equals("remove")) {

            if (args.length != 1) {
                throw new RemoteException().initCause(new EJBException("Expected one argument"));
            }
            id = args[0];
            if (id instanceof Handle && interfaceType == EJBInterfaceType.HOME) {
                HandleImpl handle = (HandleImpl) id;
                EJBObject ejbObject = handle.getEJBObject();
                EJBMethodInterceptor ejbHandler = ((BaseEJB) ejbObject).ejbHandler;
                id = ejbHandler.getPrimaryKey();
            }
        }

        return new EJBInvocationImpl(interfaceType, id, methodIndex, args);
    }

    private void copyArgsToTargetCL(Object[] args) throws IOException, ClassNotFoundException {
        if (args != null && args.length > 0) {
            ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(targetClassLoader);
                if (crossClassLoader) {
                    SerializationHandler.setStrategy(ReplacementStrategy.IN_VM_REPLACE);
                    SerializationHandler.copyArgs(targetClassLoader, args);
                } else {
                    SerializationHandler.setStrategy(ReplacementStrategy.COPY);
                    SerializationHandler.copyArgs(args);
                }
            } finally {
                SerializationHandler.setStrategy(null);
                Thread.currentThread().setContextClassLoader(oldCl);
            }
        }
    }

    private Object copyReturnToSourceCL(Object returnObj) throws IOException, ClassNotFoundException {
        if (returnObj == null) {
            return null;
        }

        // copy the result into the current classloader
        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(sourceClassLoader);
            if (crossClassLoader) {
                SerializationHandler.setStrategy(ReplacementStrategy.IN_VM_REPLACE);
                return SerializationHandler.copyObj(sourceClassLoader, returnObj);
            } else {
                SerializationHandler.setStrategy(ReplacementStrategy.COPY);
                return SerializationHandler.copyObj(returnObj);
            }
        } finally {
            SerializationHandler.setStrategy(null);
            Thread.currentThread().setContextClassLoader(oldCl);
        }
    }

    private void initialize() throws ContainerNotFoundException {
        // only initialize once... there is a race condition but it doesn't matter since
        // initialization only copies data fro the proxy factory and the values of that data doesn't change
        if (initialized) return;

        //
        // Copy all info out of the proxy factory, since access is synchronized in the proxy factory
        //
        container = proxyFactory.getContainer();
        operationMap = proxyFactory.getOperationMap(interfaceType);

        sourceClassLoader = proxyFactory.getClassLoader();
        targetClassLoader = container.getClassLoader();
        crossClassLoader = isCrossClassLoader(sourceClassLoader, targetClassLoader);

        // @todo REMOVE: this is a dirty dirty dirty hack to make the old openejb code work
        // this lets really stupid clients get access to the primary key of the proxy, which is readily
        // available from several other sources
        this.proxyInfo = new ProxyInfo(container.getProxyInfo(), primaryKey);

        initialized = true;
    }

    private static boolean isCrossClassLoader(ClassLoader source, ClassLoader target) {
        while (source != null) {
            if (source == target) {
                return false;
            }
            source = source.getParent();
        }
        return true;
    }
}
