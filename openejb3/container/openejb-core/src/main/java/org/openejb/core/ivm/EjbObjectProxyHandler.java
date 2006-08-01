package org.openejb.core.ivm;

import java.io.ObjectStreamException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;

import org.openejb.RpcContainer;
import org.openejb.loader.SystemInstance;
import org.openejb.spi.ApplicationServer;

public abstract class EjbObjectProxyHandler extends BaseEjbProxyHandler {

    protected final static org.apache.log4j.Category logger = org.apache.log4j.Category.getInstance("OpenEJB");
    static final java.util.HashMap dispatchTable;

    static {
        dispatchTable = new java.util.HashMap();
        dispatchTable.put("getHandle", new Integer(1));
        dispatchTable.put("getPrimaryKey", new Integer(2));
        dispatchTable.put("isIdentical", new Integer(3));
        dispatchTable.put("remove", new Integer(4));
        dispatchTable.put("getEJBHome", new Integer(5));
    }

    public EjbObjectProxyHandler(RpcContainer container, Object pk, Object depID) {
        super(container, pk, depID);
    }

    public abstract Object getRegistryId();

    public Object _invoke(Object p, Method m, Object[] a) throws Throwable {
        java.lang.Object retValue = null;
        java.lang.Throwable exc = null;

        try {
            if (logger.isInfoEnabled()) {
                logger.info("invoking method " + m.getName() + " on " + deploymentID + " with identity " + primaryKey);
            }
            Integer operation = (Integer) dispatchTable.get(m.getName());

            if (operation == null) {
                retValue = businessMethod(m, a, p);
            } else {
                switch (operation.intValue()) {
                    case 1:
                        retValue = getHandle(m, a, p);
                        break;
                    case 2:
                        retValue = getPrimaryKey(m, a, p);
                        break;
                    case 3:
                        retValue = isIdentical(m, a, p);
                        break;
                    case 4:
                        retValue = remove(m, a, p);
                        break;
                    case 5:
                        retValue = getEJBHome(m, a, p);
                        break;
                    default:
                        throw new RuntimeException("Inconsistent internal state");
                }
            }

            return retValue;

            /*
            * The ire is thrown by the container system and propagated by
            * the server to the stub.
            */
        } catch (org.openejb.InvalidateReferenceException ire) {
            invalidateAllHandlers(getRegistryId());
            exc = (ire.getRootCause() != null) ? ire.getRootCause() : ire;
            throw exc;
            /*
            * Application exceptions must be reported dirctly to the client. They
            * do not impact the viability of the proxy.
            */
        } catch (org.openejb.ApplicationException ae) {
            exc = (ae.getRootCause() != null) ? ae.getRootCause() : ae;
            throw exc;

            /*
            * A system exception would be highly unusual and would indicate a sever
            * problem with the container system.
            */
        } catch (org.openejb.SystemException se) {
            invalidateReference();
            exc = (se.getRootCause() != null) ? se.getRootCause() : se;
            logger.error("The container received an unexpected exception: ", exc);
            throw new RemoteException("Container has suffered a SystemException", exc);
        } catch (org.openejb.OpenEJBException oe) {
            exc = (oe.getRootCause() != null) ? oe.getRootCause() : oe;
            logger.warn("The container received an unexpected exception: ", exc);
            throw new RemoteException("Unknown Container Exception", oe.getRootCause());
        } finally {
            if (logger.isDebugEnabled()) {
                if (exc == null) {
                    logger.debug("finished invoking method " + m.getName() + ". Return value:" + retValue);
                } else {
                    logger.debug("finished invoking method " + m.getName() + " with exception " + exc);
                }
            } else if (logger.isInfoEnabled()) {
                if (exc == null) {
                    logger.debug("finished invoking method " + m.getName());
                } else {
                    logger.debug("finished invoking method " + m.getName() + " with exception " + exc);
                }
            }
        }
    }

    protected Object getEJBHome(Method method, Object[] args, Object proxy) throws Throwable {
        checkAuthorization(method);
        return deploymentInfo.getEJBHome();
    }

    protected Object getHandle(Method method, Object[] args, Object proxy) throws Throwable {
        checkAuthorization(method);
        return new IntraVmHandle(proxy);
    }

    public org.openejb.ProxyInfo getProxyInfo() {
        return new org.openejb.ProxyInfo(deploymentInfo, primaryKey, isLocal(), container);
    }

    protected Object _writeReplace(Object proxy) throws ObjectStreamException {
        /*
         * If the proxy is being  copied between bean instances in a RPC
         * call we use the IntraVmArtifact
         */
        if (IntraVmCopyMonitor.isIntraVmCopyOperation()) {
            return new IntraVmArtifact(proxy);
            /*
            * If the proxy is referenced by a stateful bean that is  being
            * passivated by the container we allow this object to be serialized.
            */
        } else if (IntraVmCopyMonitor.isStatefulPassivationOperation()) {
            return proxy;
            /*
            * If the proxy is serialized outside the core container system,
            * we allow the application server to handle it.
            */
        } else {
            return ((ApplicationServer) SystemInstance.get().getComponent(ApplicationServer.class)).getEJBObject(this.getProxyInfo());
        }
    }

    protected abstract Object getPrimaryKey(Method method, Object[] args, Object proxy) throws Throwable;

    protected abstract Object isIdentical(Method method, Object[] args, Object proxy) throws Throwable;

    protected abstract Object remove(Method method, Object[] args, Object proxy) throws Throwable;

    protected Object businessMethod(Method method, Object[] args, Object proxy) throws Throwable {
        checkAuthorization(method);
        return container.invoke(deploymentID, method, args, primaryKey, getThreadSpecificSecurityIdentity());
    }
}
