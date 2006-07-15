package org.openejb.core;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.SessionSynchronization;

import org.openejb.Container;
import org.openejb.RpcContainer;
import org.openejb.SystemException;
import org.openejb.alt.containers.castor_cmp11.CastorCmpEntityTxPolicy;
import org.openejb.alt.containers.castor_cmp11.KeyGenerator;
import org.openejb.core.entity.EntityEjbHomeHandler;
import org.openejb.core.ivm.BaseEjbProxyHandler;
import org.openejb.core.ivm.EjbHomeProxyHandler;
import org.openejb.core.ivm.SpecialProxyInfo;
import org.openejb.core.stateful.SessionSynchronizationTxPolicy;
import org.openejb.core.stateful.StatefulBeanManagedTxPolicy;
import org.openejb.core.stateful.StatefulContainerManagedTxPolicy;
import org.openejb.core.stateful.StatefulEjbHomeHandler;
import org.openejb.core.stateless.StatelessBeanManagedTxPolicy;
import org.openejb.core.stateless.StatelessEjbHomeHandler;
import org.openejb.core.transaction.TransactionContainer;
import org.openejb.core.transaction.TransactionPolicy;
import org.openejb.core.transaction.TxManditory;
import org.openejb.core.transaction.TxNever;
import org.openejb.core.transaction.TxNotSupported;
import org.openejb.core.transaction.TxRequired;
import org.openejb.core.transaction.TxRequiresNew;
import org.openejb.core.transaction.TxSupports;
import org.openejb.util.proxy.ProxyManager;

/**
 * @org.apache.xbean.XBean element="deployment"
 */
public class DeploymentInfo implements org.openejb.DeploymentInfo {

    private Class homeInterface;
    private Class remoteInterface;
    private Class localHomeInterface;
    private Class localInterface;
    private Class beanClass;
    private Class pkClass;

    private boolean isBeanManagedTransaction;
    private boolean isReentrant;
    private Container container;
    private URL archiveURL;
    private EJBHome ejbHomeRef;

    private final DeploymentContext context;

    private Method createMethod = null;

    private HashMap postCreateMethodMap = new HashMap();
    private byte componentType;

    private HashMap methodPermissions = new HashMap();
    private HashMap methodTransactionAttributes = new HashMap();
    private HashMap methodTransactionPolicies = new HashMap();
    private HashMap methodMap = new HashMap();
    private HashMap securityRoleReferenceMap = new HashMap();
    private HashSet methodsWithRemoteReturnTypes = null;
    private EJBLocalHome ejbLocalHomeRef;
    private String jarPath;

    public DeploymentInfo(DeploymentContext context,
            String homeInterface,
            String remoteInterface,
            String localHomeInterface,
            String localInterface,
            String beanClass,
            String pkClass,
            String ejbType,
            ClassLoader classLoader) throws SystemException {
        this(context,
                loadClass(homeInterface, classLoader),
                loadClass(remoteInterface, classLoader),
                loadClass(localHomeInterface, classLoader),
                loadClass(localInterface, classLoader),
                loadClass(beanClass, classLoader),
                loadClass(pkClass, classLoader),
                getComponentType(ejbType), null);
    }

    private static Class loadClass(String name, ClassLoader classLoader) throws SystemException {
        try {
            return classLoader.loadClass(name);
        } catch (ClassNotFoundException e) {
            throw new SystemException(e);
        }
    }

    private static byte getComponentType(String name) throws SystemException {
        if ("cmp".equalsIgnoreCase(name)) {
            return CMP_ENTITY;
        } else if ("bmp".equalsIgnoreCase(name)) {
            return BMP_ENTITY;
        } else if ("stateful".equalsIgnoreCase(name)) {
            return CMP_ENTITY;
        } else if ("stateless".equalsIgnoreCase(name)) {
            return CMP_ENTITY;
        } else {
            throw new SystemException("Unknown component type: " + name);
        }
    }

    public DeploymentInfo(DeploymentContext context,
            Class homeInterface,
            Class remoteInterface,
            Class localHomeInterface,
            Class localInterface,
            Class beanClass,
            Class pkClass,
            byte componentType,
            URL archiveURL) throws SystemException {

        this.context = context;
        this.pkClass = pkClass;

        this.homeInterface = homeInterface;
        this.remoteInterface = remoteInterface;
        this.localInterface = localInterface;
        this.localHomeInterface = localHomeInterface;
        this.remoteInterface = remoteInterface;
        this.beanClass = beanClass;
        this.pkClass = pkClass;
        this.componentType = componentType;
        this.archiveURL = archiveURL;
        createMethodMap();

    }

    public void setContainer(Container cont) {
        container = cont;
    }

    public byte getComponentType() {
        return componentType;
    }

    public byte getTransactionAttribute(Method method) {

        Byte byteWrapper = (Byte) methodTransactionAttributes.get(method);
        if (byteWrapper == null)
            return TX_NOT_SUPPORTED;// non remote or home interface method
        else
            return byteWrapper.byteValue();
    }

    public TransactionPolicy getTransactionPolicy(Method method) {

        TransactionPolicy policy = (TransactionPolicy) methodTransactionPolicies.get(method);
        if (policy == null && !isBeanManagedTransaction) {
            org.apache.log4j.Logger.getLogger("OpenEJB").warn("The following method doesn't have a transaction policy assigned: " + method);
        }
        if (policy == null && container instanceof TransactionContainer) {
            if (isBeanManagedTransaction) {
                if (componentType == STATEFUL) {
                    policy = new StatefulBeanManagedTxPolicy((TransactionContainer) container);
                } else if (componentType == STATELESS) {
                    policy = new StatelessBeanManagedTxPolicy((TransactionContainer) container);
                }
            } else if (componentType == STATEFUL) {
                policy = new TxNotSupported((TransactionContainer) container);
                policy = new StatefulContainerManagedTxPolicy(policy);
            } else if (componentType == CMP_ENTITY) {
                policy = new TxNotSupported((TransactionContainer) container);
                policy = new CastorCmpEntityTxPolicy(policy);
            } else {
                policy = new TxNotSupported((TransactionContainer) container);
            }
            methodTransactionPolicies.put(method, policy);
        }
        return policy;
    }

    public String [] getAuthorizedRoles(Method method) {
        HashSet roleSet = (HashSet) methodPermissions.get(method);
        if (roleSet == null) return null;
        String [] roles = new String[roleSet.size()];
        return (String []) roleSet.toArray(roles);
    }

    public String [] getAuthorizedRoles(String action) {
        return null;
    }

    public Container getContainer() {
        return container;
    }

    public Object getDeploymentID() {
        return context.getId();
    }

    public boolean isBeanManagedTransaction() {
        return isBeanManagedTransaction;
    }

    public Class getHomeInterface() {
        return homeInterface;
    }

    public Class getRemoteInterface() {
        return remoteInterface;
    }

    public Class getLocalHomeInterface() {
        return localHomeInterface;
    }

    public Class getLocalInterface() {
        return localInterface;
    }

    public Class getBeanClass() {
        return beanClass;
    }

    public Class getPrimaryKeyClass() {
        return pkClass;
    }

    public EJBHome getEJBHome() {
        if (getHomeInterface() == null) {
            throw new IllegalStateException("This component has no home interface: " + getDeploymentID());
        }
        if (ejbHomeRef == null) {
            ejbHomeRef = createEJBHomeRef();
        }
        return ejbHomeRef;
    }

    public EJBLocalHome getEJBLocalHome() {
        if (getLocalHomeInterface() == null) {
            throw new IllegalStateException("This component has no local home interface: " + getDeploymentID());
        }
        if (ejbLocalHomeRef == null) {
            ejbLocalHomeRef = createEJBLocalHomeRef();
        }
        return ejbLocalHomeRef;
    }

    public void setBeanManagedTransaction(boolean value) {
        isBeanManagedTransaction = value;
    }

    public javax.naming.Context getJndiEnc() {
        return context.getJndiContext();
    }

    public boolean isReentrant() {
        return isReentrant;
    }

    public void setIsReentrant(boolean reentrant) {
        isReentrant = reentrant;
    }

    public Object convertIfLocalReference(Method businessMethod, Object returnValue) {
        if (returnValue == null || methodsWithRemoteReturnTypes == null)
            return returnValue;

        try {
            if (methodsWithRemoteReturnTypes.contains(businessMethod)
                    && ProxyManager.isProxyClass(returnValue.getClass())
                    && ProxyManager.getInvocationHandler(returnValue) instanceof BaseEjbProxyHandler) {

                return new SpecialProxyInfo(returnValue);
            }
        } catch (ClassCastException e) {

        }
        return returnValue;

    }

    public Method getMatchingBeanMethod(Method interfaceMethod) {
        Method mthd = (Method) methodMap.get(interfaceMethod);
        return (mthd == null) ? interfaceMethod : mthd;
    }

    public void appendMethodPermissions(Method m, String [] roleNames) {
        HashSet hs = (HashSet) methodPermissions.get(m);
        if (hs == null) {
            hs = new HashSet();// FIXME: Set appropriate load and intial capacity
            methodPermissions.put(m, hs);
        }
        for (int i = 0; i < roleNames.length; i++) {
            hs.add(roleNames[i]);
        }
    }

    public String [] getPhysicalRole(String securityRoleReference) {
        return (String[]) securityRoleReferenceMap.get(securityRoleReference);
    }

    public void addSecurityRoleReference(String securityRoleReference, String [] physicalRoles) {
        securityRoleReferenceMap.put(securityRoleReference, physicalRoles);
    }

    public void setMethodTransactionAttribute(Method method, String transAttribute) {
        Byte byteValue = null;
        TransactionPolicy policy = null;

        if (transAttribute.equals("Supports")) {
            if (container instanceof TransactionContainer) {
                policy = new TxSupports((TransactionContainer) container);
            }
            byteValue = new Byte(TX_SUPPORTS);

        } else if (transAttribute.equals("RequiresNew")) {
            if (container instanceof TransactionContainer) {
                policy = new TxRequiresNew((TransactionContainer) container);
            }
            byteValue = new Byte(TX_REQUIRES_NEW);

        } else if (transAttribute.equals("Mandatory")) {
            if (container instanceof TransactionContainer) {
                policy = new TxManditory((TransactionContainer) container);
            }
            byteValue = new Byte(TX_MANDITORY);

        } else if (transAttribute.equals("NotSupported")) {
            if (container instanceof TransactionContainer) {
                policy = new TxNotSupported((TransactionContainer) container);
            }
            byteValue = new Byte(TX_NOT_SUPPORTED);

        } else if (transAttribute.equals("Required")) {
            if (container instanceof TransactionContainer) {
                policy = new TxRequired((TransactionContainer) container);
            }
            byteValue = new Byte(TX_REQUIRED);

        } else if (transAttribute.equals("Never")) {
            if (container instanceof TransactionContainer) {
                policy = new TxNever((TransactionContainer) container);
            }
            byteValue = new Byte(TX_NEVER);
        } else {
            throw new IllegalArgumentException("Invalid transaction attribute \"" + transAttribute + "\" declared for method " + method.getName() + ". Please check your configuration.");
        }

        /* EJB 1.1 page 55
         Only a stateful Session bean with container-managed transaction demarcation may implement the
         SessionSynchronization interface. A stateless Session bean must not implement the SessionSynchronization
         interface.
         */

        if (componentType == STATEFUL && !isBeanManagedTransaction && container instanceof TransactionContainer) {

            if (SessionSynchronization.class.isAssignableFrom(beanClass)) {
                if (!transAttribute.equals("Never") && !transAttribute.equals("NotSupported")) {

                    policy = new SessionSynchronizationTxPolicy(policy);
                }
            } else {

                policy = new StatefulContainerManagedTxPolicy(policy);
            }

        } else if (componentType == CMP_ENTITY) {
            policy = new CastorCmpEntityTxPolicy(policy);
        }
        methodTransactionAttributes.put(method, byteValue);
        methodTransactionPolicies.put(method, policy);
    }

    private javax.ejb.EJBHome createEJBHomeRef() {

        EjbHomeProxyHandler handler = null;

        switch (getComponentType()) {
            case STATEFUL:
                handler = new StatefulEjbHomeHandler((RpcContainer) container, null, getDeploymentID());
                break;

            case STATELESS:
                handler = new StatelessEjbHomeHandler((RpcContainer) container, null, getDeploymentID());
                break;
            case CMP_ENTITY:
            case BMP_ENTITY:
                handler = new EntityEjbHomeHandler((RpcContainer) container, null, getDeploymentID());
                break;
        }

        Object proxy = null;
        try {
            Class[] interfaces = new Class[]{this.getHomeInterface(), org.openejb.core.ivm.IntraVmProxy.class};
            proxy = ProxyManager.newProxyInstance(interfaces, handler);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Can't create EJBHome stub" + e.getMessage());
        }

        return (javax.ejb.EJBHome) proxy;

    }

    private javax.ejb.EJBLocalHome createEJBLocalHomeRef() {

        EjbHomeProxyHandler handler = null;

        switch (getComponentType()) {
            case STATEFUL:
                handler = new StatefulEjbHomeHandler((RpcContainer) container, null, getDeploymentID());
                break;

            case STATELESS:
                handler = new StatelessEjbHomeHandler((RpcContainer) container, null, getDeploymentID());
                break;
            case CMP_ENTITY:
            case BMP_ENTITY:
                handler = new EntityEjbHomeHandler((RpcContainer) container, null, getDeploymentID());
                break;
        }
        handler.setLocal(true);
        Object proxy = null;
        try {
            Class[] interfaces = new Class[]{this.getLocalHomeInterface(), org.openejb.core.ivm.IntraVmProxy.class};
            proxy = ProxyManager.newProxyInstance(interfaces, handler);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Can't create EJBLocalHome stub" + e.getMessage());
        }

        return (javax.ejb.EJBLocalHome) proxy;

    }

    private void createMethodMap() throws org.openejb.SystemException {
        if (homeInterface != null) {
            mapObjectInterface(remoteInterface, false);
            mapHomeInterface(homeInterface);
        }
        if (localHomeInterface != null) {
            mapObjectInterface(localInterface, true);
            mapHomeInterface(localHomeInterface);
        }


        try {

            if (componentType == STATEFUL || componentType == STATELESS) {
                Method beanMethod = javax.ejb.SessionBean.class.getDeclaredMethod("ejbRemove", new Class []{});
                Method clientMethod = EJBHome.class.getDeclaredMethod("remove", new Class []{javax.ejb.Handle.class});
                methodMap.put(clientMethod, beanMethod);
                clientMethod = EJBHome.class.getDeclaredMethod("remove", new Class []{java.lang.Object.class});
                methodMap.put(clientMethod, beanMethod);
                clientMethod = javax.ejb.EJBObject.class.getDeclaredMethod("remove", null);
                methodMap.put(clientMethod, beanMethod);
            } else if (componentType == BMP_ENTITY || componentType == CMP_ENTITY) {
                Method beanMethod = javax.ejb.EntityBean.class.getDeclaredMethod("ejbRemove", new Class []{});
                Method clientMethod = EJBHome.class.getDeclaredMethod("remove", new Class []{javax.ejb.Handle.class});
                methodMap.put(clientMethod, beanMethod);
                clientMethod = EJBHome.class.getDeclaredMethod("remove", new Class []{java.lang.Object.class});
                methodMap.put(clientMethod, beanMethod);
                clientMethod = javax.ejb.EJBObject.class.getDeclaredMethod("remove", null);
                methodMap.put(clientMethod, beanMethod);
            }
        } catch (java.lang.NoSuchMethodException nsme) {
            throw new org.openejb.SystemException(nsme);
        }

    }

    private void mapHomeInterface(Class intrface) {
        Method [] homeMethods = intrface.getMethods();
        for (int i = 0; i < homeMethods.length; i++) {
            Method method = homeMethods[i];
            Class owner = method.getDeclaringClass();
            if (owner == javax.ejb.EJBHome.class || owner == EJBLocalHome.class) {
                continue;
            }

            try {
                Method beanMethod = null;
                if (method.getName().equals("create")) {
                    beanMethod = beanClass.getMethod("ejbCreate", method.getParameterTypes());
                    createMethod = beanMethod;
                    /*
                    Entity beans have a ejbCreate and ejbPostCreate methods with matching 
                    parameters. This code maps that relationship.
                    */
                    if (this.componentType == BMP_ENTITY || this.componentType == CMP_ENTITY) {
                        Method postCreateMethod = beanClass.getMethod("ejbPostCreate", method.getParameterTypes());
                        postCreateMethodMap.put(createMethod, postCreateMethod);
                    }
                    /*
                     * Stateless session beans only have one create method. The getCreateMethod is
                     * used by instance manager of the core.stateless.StatelessContainer as a convenience
                     * method for obtaining the ejbCreate method.
                    */
                } else if (method.getName().startsWith("find")) {
                    if (this.componentType == BMP_ENTITY) {

                        String beanMethodName = "ejbF" + method.getName().substring(1);
                        beanMethod = beanClass.getMethod(beanMethodName, method.getParameterTypes());
                    }
                } else {
                    String beanMethodName = "ejbHome" + method.getName().substring(0, 1).toUpperCase() + method.getName().substring(1);
                    beanMethod = beanClass.getMethod(beanMethodName, method.getParameterTypes());
                }
                if (beanMethod != null) {
                    methodMap.put(homeMethods[i], beanMethod);
                }
            } catch (NoSuchMethodException nsme) {
//                throw new RuntimeException("Invalid method [" + method + "] Not declared by " + beanClass.getName() + " class");
            }
        }
    }

    private void mapObjectInterface(Class intrface, boolean isLocal) {
        Method [] interfaceMethods = intrface.getMethods();
        for (int i = 0; i < interfaceMethods.length; i++) {
            Method method = interfaceMethods[i];
            Class declaringClass = method.getDeclaringClass();
            if (declaringClass == javax.ejb.EJBObject.class || declaringClass == EJBLocalObject.class) {
                continue;
            }
            try {
                Method beanMethod = beanClass.getMethod(method.getName(), method.getParameterTypes());
                methodMap.put(method, beanMethod);
            } catch (NoSuchMethodException nsme) {
                throw new RuntimeException("Invalid method [" + method + "]. Not declared by " + beanClass.getName() + " class");
            }
            /*
               check for return type of java.rmi.Remote. If one of the business method returns a
               java.rmi.Remote type, it may be a org.openejb.ivm.BaseEjbProxyHandler type at runtime,
               in which case it will need to be converted by the container into a ProxyInfo object.
               The container will use the convertIfLocalReference() to check.
               This block of code sets up that method.
             */
            if (!isLocal && java.rmi.Remote.class.isAssignableFrom(method.getReturnType())) {
                if (methodsWithRemoteReturnTypes == null) {
                    methodsWithRemoteReturnTypes = new HashSet();
                }
                methodsWithRemoteReturnTypes.add(method);
            }
        }
    }

    protected String extractHomeBeanMethodName(String methodName) {
        if (methodName.equals("create"))
            return "ejbCreate";
        else if (methodName.startsWith("find"))
            return "ejbF" + methodName.substring(1);
        else
            return "ejbH" + methodName.substring(1);
    }

    public Method getCreateMethod() {
        return createMethod;
    }

    public Method getMatchingPostCreateMethod(Method createMethod) {
        return (Method) this.postCreateMethodMap.get(createMethod);
    }

    private KeyGenerator keyGenerator;
    private Field primKeyField;
    private String[] cmrFields;

    private HashMap queryMethodMap = new HashMap();

    public Field getPrimaryKeyField() {
        return primKeyField;
    }

    public void setPrimKeyField(String fieldName)
            throws java.lang.NoSuchFieldException {
        if (componentType == CMP_ENTITY) {

            primKeyField = beanClass.getField(fieldName);
        }
    }

    public String [] getCmrFields() {
        return cmrFields;
    }

    public void setCmrFields(String [] cmrFields) {
        this.cmrFields = cmrFields;
    }

    public KeyGenerator getKeyGenerator() {
        return keyGenerator;
    }

    public void setKeyGenerator(KeyGenerator keyGenerator) {
        this.keyGenerator = keyGenerator;
    }

    public void addQuery(Method queryMethod, String queryString) {
        queryMethodMap.put(queryMethod, queryString);
    }

    public String getQuery(Method queryMethod) {
        return (String) queryMethodMap.get(queryMethod);
    }

    public void setJarPath(String jarPath) {
        this.jarPath = jarPath;
    }

    public String getJarPath() {
        return jarPath;
    }
}
