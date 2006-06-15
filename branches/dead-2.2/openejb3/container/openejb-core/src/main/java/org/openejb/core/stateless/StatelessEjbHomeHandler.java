package org.openejb.core.stateless;

import java.lang.reflect.Method;
import java.rmi.RemoteException;

import org.openejb.RpcContainer;
import org.openejb.core.ThreadContext;
import org.openejb.core.ivm.EjbHomeProxyHandler;
import org.openejb.core.ivm.EjbObjectProxyHandler;
import org.openejb.core.ivm.IntraVmHandle;
import org.openejb.util.proxy.ProxyManager;

public class StatelessEjbHomeHandler extends EjbHomeProxyHandler {

    public StatelessEjbHomeHandler(RpcContainer container, Object pk, Object depID) {
        super(container, pk, depID);
    }

    protected Object findX(Method method, Object[] args, Object proxy) throws Throwable {
        throw new UnsupportedOperationException("Stateful beans may not have find methods");
    }

    protected Object removeByPrimaryKey(Method method, Object[] args, Object proxy) throws Throwable {
        throw new RemoteException("Session objects are private resources and do not have primary keys");
    }

    /*
    * This method is different from the stateful and entity behavior because we only want the 
    * stateless session bean that created the proxy to be invalidated, not all the proxies.
    *
    * TODO: this method relies on the fact that the handle implementation is a subclass
    * of IntraVM handle, which isn't neccessarily the case for arbitrary remote protocols.
    */
    protected Object removeWithHandle(Method method, Object[] args, Object proxy) throws Throwable {

        IntraVmHandle handle = (IntraVmHandle) args[0];
        Object primKey = handle.getPrimaryKey();
        EjbObjectProxyHandler stub;
        try {
            stub = (EjbObjectProxyHandler) ProxyManager.getInvocationHandler(handle.getEJBObject());
        } catch (IllegalArgumentException e) {

            stub = null;
        }

        container.invoke(deploymentID, method, args, primKey, ThreadContext.getThreadContext().getSecurityIdentity());
        if (stub != null) {
            stub.invalidateReference();
        }
        return null;
    }

    protected EjbObjectProxyHandler newEjbObjectHandler(RpcContainer container, Object pk, Object depID) {
        return new StatelessEjbObjectHandler(container, pk, depID);
    }

}
