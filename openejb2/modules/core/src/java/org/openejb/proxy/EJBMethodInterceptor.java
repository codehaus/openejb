package org.openejb.proxy;

import java.io.IOException;
import java.io.Serializable;
import java.io.ObjectStreamException;
import java.lang.reflect.Method;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import javax.ejb.EJBException;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.NoSuchObjectLocalException;
import javax.ejb.EJBHome;
import javax.ejb.EJBMetaData;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.apache.geronimo.core.service.InvocationResult;
import org.openejb.ContainerNotFoundException;
import org.openejb.EJBComponentType;
import org.openejb.EJBContainer;
import org.openejb.EJBInterfaceType;
import org.openejb.EJBInvocation;
import org.openejb.EJBInvocationImpl;

public class EJBMethodInterceptor implements MethodInterceptor, Serializable {
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
            // @todo REMOVE: this is a dirty dirty dirty hack to make the old openejb code work
            // this lets really stupid clients get access to the primary key of the proxy, which is readily
            // available from several other sources
            this.proxyInfo = new ProxyInfo(container.getProxyInfo(), primaryKey);
        }

        shouldCopy = !interfaceType.isLocal();
    }

    public EJBProxyFactory getProxyFactory() {
        return proxyFactory;
    }

    public ProxyInfo getProxyInfo() throws ContainerNotFoundException {
        if (proxyInfo == null) {
            loadContainerInfo();
        }
        return proxyInfo;
    }

    public Object getPrimaryKey() {
        return primaryKey;
    }

    public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        EJBInvocation invocation = createEJBInvocation(method, methodProxy, args);
        if (invocation == null) {
            return null;
        }

        // copy the arguments into the target classloader
        if (shouldCopy) {
            args = invocation.getArguments();
            copyArgs(args);
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
            returnObj = copyObject(returnObj);
        }

        if (normal) {
            return returnObj;
        } else {
            throw (Exception) returnObj;
        }
    }

    private EJBInvocation createEJBInvocation(Method method, MethodProxy methodProxy, Object[] args) throws Throwable {
        // fault in the operation map if we don't have it yet
        if (operationMap == null) {
            try {
                loadContainerInfo();
            } catch (ContainerNotFoundException e) {
                if (!interfaceType.isLocal()) {
                    throw new NoSuchObjectException(e.getMessage());
                } else {
                    throw new NoSuchObjectLocalException(e.getMessage());
                }
            }
        }

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

    private void copyArgs(Object[] args) throws IOException, ClassNotFoundException {
        if (args != null && args.length > 0) {
            try {
                SerializationHanlder.setStrategy(ReplacementStrategy.COPY);
                SerializationHanlder.copyArgs(args);
            } finally {
                SerializationHanlder.setStrategy(null);
            }
        }
    }

    private Object copyObject(Object returnObj) throws IOException, ClassNotFoundException {
        if (returnObj == null) {
            return null;
        }

        // copy the result into the current classloader
        try {
            SerializationHanlder.setStrategy(ReplacementStrategy.COPY);
            return SerializationHanlder.copyObj(returnObj);
        } finally {
            SerializationHanlder.setStrategy(null);
        }
    }

    private void loadContainerInfo() throws ContainerNotFoundException {
        container = proxyFactory.getContainer();
        operationMap = proxyFactory.getOperationMap(interfaceType);

        // @todo REMOVE: this is a dirty dirty dirty hack to make the old openejb code work
        // this lets really stupid clients get access to the primary key of the proxy, which is readily
        // available from several other sources
        this.proxyInfo = new ProxyInfo(container.getProxyInfo(), primaryKey);
    }

//    private static final class ClassLoaderCopy implements ReplacementStrategy {
//        public Object writeReplace(Object object, ProxyInfo proxyInfo) throws ObjectStreamException {
//            new EJBProxyFactory(proxyInfo);
//            if (object instanceof EJBObject){
//                return org.openejb.OpenEJB.getApplicationServer().getEJBObject(proxyInfo);
//            } else if (object instanceof EJBHome){
//                return org.openejb.OpenEJB.getApplicationServer().getEJBHome(proxyInfo);
//            } else if (object instanceof EJBMetaData){
//                return org.openejb.OpenEJB.getApplicationServer().getEJBMetaData(proxyInfo);
//            } else if (object instanceof HandleImpl){
//                HandleImpl handle = (HandleImpl)object;
//
//                if (handle.type == HandleImpl.HANDLE){
//                    return org.openejb.OpenEJB.getApplicationServer().getHandle(proxyInfo);
//                } else {
//                    return org.openejb.OpenEJB.getApplicationServer().getHomeHandle(proxyInfo);
//                }
//            } else /*should never happen */ {
//                return object;
//            }
//        }
//    };
//
}
