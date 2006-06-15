package org.openejb.client;

import java.lang.reflect.Method;
import java.rmi.RemoteException;

import javax.ejb.EJBObject;

import org.openejb.client.proxy.ProxyManager;

public abstract class EJBObjectHandler extends EJBInvocationHandler {

    protected static final Method GETEJBHOME = getMethod(EJBObject.class, "getEJBHome", null);
    protected static final Method GETHANDLE = getMethod(EJBObject.class, "getHandle", null);
    protected static final Method GETPRIMARYKEY = getMethod(EJBObject.class, "getPrimaryKey", null);
    protected static final Method ISIDENTICAL = getMethod(EJBObject.class, "isIdentical", new Class []{EJBObject.class});
    protected static final Method REMOVE = getMethod(EJBObject.class, "remove", null);

    protected static final Method GETHANDLER = getMethod(EJBObjectProxy.class, "getEJBObjectHandler", null);

    /*
    * The registryId is a logical identifier that is used as a key when placing EntityEJBObjectHandler into
    * the BaseEjbProxyHanlder's liveHandleRegistry.  EntityEJBObjectHandlers that represent the same
    * bean identity (keyed by the registry id) will be stored together so that they can be removed together
    * when the EJBInvocationHandler.invalidateAllHandlers is invoked. The EntityEJBObjectHandler uses a 
    * compound key composed of the entity bean's primary key, deployment id, and
    * container id.  This uniquely identifies the bean identity that is proxied by this handler allowing it
    * to be removed with other handlers bound to the same registry id.
    */
    public Object registryId;

    EJBHomeProxy ejbHome = null;

    public EJBObjectHandler() {
    }

    public EJBObjectHandler(EJBMetaDataImpl ejb, ServerMetaData server, ClientMetaData client) {
        super(ejb, server, client);
    }

    public EJBObjectHandler(EJBMetaDataImpl ejb, ServerMetaData server, ClientMetaData client, Object primaryKey) {
        super(ejb, server, client, primaryKey);
    }

    protected void setEJBHomeProxy(EJBHomeProxy ejbHome) {
        this.ejbHome = ejbHome;
    }

    public static EJBObjectHandler createEJBObjectHandler(EJBMetaDataImpl ejb, ServerMetaData server, ClientMetaData client, Object primaryKey) {

        switch (ejb.type) {
            case EJBMetaDataImpl.BMP_ENTITY:
            case EJBMetaDataImpl.CMP_ENTITY:

                return new EntityEJBObjectHandler(ejb, server, client, primaryKey);

            case EJBMetaDataImpl.STATEFUL:

                return new StatefulEJBObjectHandler(ejb, server, client, primaryKey);

            case EJBMetaDataImpl.STATELESS:

                return new StatelessEJBObjectHandler(ejb, server, client, primaryKey);
        }
        return null;
    }

    public abstract Object getRegistryId();

    public EJBObjectProxy createEJBObjectProxy() {

        EJBObjectProxy ejbObject = null;

        try {

            Class[] interfaces = new Class[]{EJBObjectProxy.class, ejb.remoteClass};
            ejbObject = (EJBObjectProxy) ProxyManager.newProxyInstance(interfaces, this);

        } catch (IllegalAccessException e) {

            e.printStackTrace();
        }
        return ejbObject;
    }

    public synchronized Object _invoke(Object p, Method m, Object[] a) throws Throwable {

        Object retValue = null;
        /*
         * This section is to be replaced by a more appropriate solution.
         * This code is very temporary.
         */

        try {

            String methodName = m.getName();
            if (m.getDeclaringClass() == Object.class) {
                if (m.equals(TOSTRING)) {
                    return "proxy=" + this;
                } else if (m.equals(EQUALS)) {

                    return Boolean.FALSE;

                } else if (m.equals(HASHCODE)) {
                    return new Integer(this.hashCode());
                } else {
                    throw new UnsupportedOperationException("Unkown method: " + m);
                }
            } else if (m.getDeclaringClass() == EJBObjectProxy.class) {
                if (m.equals(GETHANDLER)) {
                    return this;
                } else if (methodName.equals("writeReplace")) {
                    return new EJBObjectProxyHandle(this);
                } else if (methodName.equals("readResolve")) {

                } else {
                    throw new UnsupportedOperationException("Unkown method: " + m);
                }
            } else if (m.getDeclaringClass() == javax.ejb.EJBObject.class) {
                if (m.equals(GETHANDLE)) retValue = getHandle(m, a, p);
                else if (m.equals(GETPRIMARYKEY)) retValue = getPrimaryKey(m, a, p);
                else if (m.equals(ISIDENTICAL)) retValue = isIdentical(m, a, p);
                else if (m.equals(GETEJBHOME)) retValue = getEJBHome(m, a, p);
                else if (m.equals(REMOVE)) retValue = remove(m, a, p);
                else
                    throw new UnsupportedOperationException("Unkown method: " + m);
            } else if (m.getDeclaringClass() == ejb.remoteClass) {
                retValue = businessMethod(m, a, p);
            } else {
                throw new UnsupportedOperationException("Unkown method: " + m);
            }

        } catch (SystemException e) {
            invalidateAllHandlers(getRegistryId());
            throw e.getCause();
            /*
            * Application exceptions must be reported dirctly to the client. They
            * do not impact the viability of the proxy.
            */
        } catch (ApplicationException ae) {
            throw ae.getCause();
            /*
            * A system exception would be highly unusual and would indicate a sever
            * problem with the container system.
            */
        } catch (SystemError se) {
            invalidateReference();
            throw new RemoteException("Container has suffered a SystemException", se.getCause());
        } catch (Throwable oe) {
            throw new RemoteException("Unknown Container Exception", oe.getCause());
        }
        return retValue;
    }

    protected Object getEJBHome(Method method, Object[] args, Object proxy) throws Throwable {
        if (ejbHome == null) {
            ejbHome = EJBHomeHandler.createEJBHomeHandler(ejb, server, client).createEJBHomeProxy();
        }
        return ejbHome;
    }

    protected Object getHandle(Method method, Object[] args, Object proxy) throws Throwable {
        return new EJBObjectHandle((EJBObjectProxy) proxy);
    }

    protected abstract Object getPrimaryKey(Method method, Object[] args, Object proxy) throws Throwable;

    protected abstract Object isIdentical(Method method, Object[] args, Object proxy) throws Throwable;

    protected abstract Object remove(Method method, Object[] args, Object proxy) throws Throwable;

    protected Object businessMethod(Method method, Object[] args, Object proxy) throws Throwable {
//      checkAuthorization(method);
//      return container.invoke(deploymentID, method, args, primaryKey, getThreadSpecificSecurityIdentity());

        EJBRequest req = new EJBRequest(EJB_OBJECT_BUSINESS_METHOD);

        req.setMethodParameters(args);
        req.setMethodInstance(method);
        req.setClientIdentity(client.getClientIdentity());
        req.setDeploymentCode(ejb.deploymentCode);
        req.setDeploymentId(ejb.deploymentID);
        req.setPrimaryKey(primaryKey);

        EJBResponse res = request(req);

//        if (method.getName().equals("test36_returnEJBHome2")) {
//          System.out.println("\n\n----------------------------------------------------------");
//          System.out.println(method.getName());
//          Object obj = res.getResult();
//          System.out.println("obj="+(obj==null));
//          System.out.println("obj="+(obj.getClass()));
//          System.out.println("obj="+(obj.getClass().getDeclaringClass()));
//          Class[] ifs = obj.getClass().getInterfaces();
//          for (int i=0; i < ifs.length; i++){
//              System.out.println("ifs["+i+"] "+ifs[i]);
//          }
//        }
        switch (res.getResponseCode()) {
            case EJB_ERROR:
                throw new SystemError((ThrowableArtifact) res.getResult());
            case EJB_SYS_EXCEPTION:
                throw new SystemException((ThrowableArtifact) res.getResult());
            case EJB_APP_EXCEPTION:
                throw new ApplicationException((ThrowableArtifact) res.getResult());
            case EJB_OK:
                return res.getResult();
            default:
                throw new RemoteException("Received invalid response code from server: " + res.getResponseCode());
        }
    }

}
