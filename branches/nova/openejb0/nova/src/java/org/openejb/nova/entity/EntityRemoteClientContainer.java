/* ====================================================================
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce this list of
 *    conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact openejb-group@openejb.sf.net.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE OPENEJB GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE OPENEJB GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the OpenEJB Project.  For more information
 * please see <http://openejb.org/>.
 *
 * ====================================================================
 */
package org.openejb.nova.entity;

import java.io.Serializable;
import java.rmi.RemoteException;
import javax.ejb.EJBException;
import javax.ejb.EJBHome;
import javax.ejb.EJBMetaData;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.HomeHandle;
import javax.ejb.RemoveException;

import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.core.service.InvocationResult;
import org.apache.geronimo.security.ContextManager;

import org.openejb.nova.EJBInvocationImplRemote;
import org.openejb.nova.EJBInvocationType;
import org.openejb.nova.EJBProxyFactory;
import org.openejb.nova.EJBProxyInterceptor;
import org.openejb.nova.EJBRemoteClientContainer;
import org.openejb.nova.dispatch.MethodSignature;

/**
 *
 *
 *
 * @version $Revision$ $Date$
 */
public class EntityRemoteClientContainer implements EJBRemoteClientContainer {
    private Interceptor firstInterceptor;

    private final EJBHome homeProxy;

    private final EJBProxyFactory proxyFactory;
    private final int removeIndex;
    private final int[] operationMap;

    private final EJBMetaData ejbMetadata;
    private final HomeHandle homeHandle;

    public EntityRemoteClientContainer(Interceptor firstInterceptor, MethodSignature[] signatures, Class home, Class remote, Class pkClass) {
        this.firstInterceptor = firstInterceptor;

        // Create Home proxy
        EJBProxyFactory factory = new EJBProxyFactory(EntityHomeImpl.class, home);
        EJBProxyInterceptor methodInterceptor = new EJBProxyInterceptor(this, EJBInvocationType.HOME, factory.getType(), signatures);
        homeProxy = (EJBHome) factory.create(methodInterceptor, new Class[]{EntityRemoteClientContainer.class}, new Object[]{this});

        // Create Remote Proxy
        proxyFactory = new EJBProxyFactory(EntityObjectImpl.class, remote);
        operationMap = EJBProxyInterceptor.getOperationMap(EJBInvocationType.REMOTE, proxyFactory.getType(), signatures);

        // Get VOP index for ejbRemove method
        int index = -1;
        for (int i = 0; i < signatures.length; i++) {
            MethodSignature signature = signatures[i];
            if ("ejbRemove".equals(signature.getMethodName())) {
                index = i;
                break;
            }
        }
        assert (index != -1) : "No ejbRemove VOP defined";
        removeIndex = index;

        ejbMetadata = new EntityMetaData(homeProxy, home, remote, pkClass);
        homeHandle = null;
    }

    public EJBHome getEJBHome() {
        return homeProxy;
    }

    public EJBObject getEJBObject(Object primaryKey) {
        EJBProxyInterceptor methodInterceptor = new EJBProxyInterceptor(this, EJBInvocationType.REMOTE, operationMap, primaryKey);
        return (EJBObject) proxyFactory.create(
                methodInterceptor,
                new Class[]{EntityLocalClientContainer.class, Object.class},
                new Object[]{this, primaryKey});
    }

    private void remove(Object id) throws RemoveException, RemoteException {
        InvocationResult result;
        try {
            EJBInvocationImplRemote invocation = new EJBInvocationImplRemote(EJBInvocationType.REMOTE, id, removeIndex, null, ContextManager.getCurrentCallerId());
            result = firstInterceptor.invoke(invocation);
        } catch (RemoteException e) {
            throw e;
        } catch (Throwable t) {
            throw new RemoteException(t.getMessage(), t);
        }
        if (result.isException()) {
            throw (RemoveException) result.getException();
        }
    }


    public Object invoke(EJBInvocationType ejbInvocationType, Object id, int methodIndex, Object[] args) throws Throwable {
        InvocationResult result;
        try {
            EJBInvocationImplRemote invocation = new EJBInvocationImplRemote(ejbInvocationType, id, methodIndex, args, ContextManager.getCurrentCallerId());
            result = firstInterceptor.invoke(invocation);
        } catch (Throwable t) {
            // System exception from interceptor chain - throw as is or wrapped in an EJBException
            if (t instanceof Exception && t instanceof RuntimeException == false) {
                t = new EJBException((Exception) t);
            }
            throw t;
        }
        if (result.isNormal()) {
            return result.getResult();
        } else {
            throw result.getException();
        }
    }

    /**
     * Base class for then EJBHome proxy.
     * Owns a reference to the container.
     */
    private abstract static class EntityHomeImpl implements EJBHome {
        private final EntityRemoteClientContainer container;

        public EntityHomeImpl(EntityRemoteClientContainer container) {
            this.container = container;
        }

        public EJBMetaData getEJBMetaData() throws RemoteException {
            return container.ejbMetadata;
        }

        public HomeHandle getHomeHandle() throws RemoteException {
            return container.homeHandle;
        }

        public void remove(Handle handle) throws RemoteException, RemoveException {
            // @todo We need to support this...
            throw new UnsupportedOperationException();
        }

        public void remove(Object primaryKey) throws RemoveException, RemoteException {
            container.remove(primaryKey);
        }
    }

    /**
     * Base class for EJBLocalObject proxies.
     * Owns a reference to the container and the id of the Entity.
     * Implements EJBLocalObject methods, such as getPrimaryKey(), that do
     * not require a trip to the server.
     */
    private abstract static class EntityObjectImpl implements EJBObject {
        private final EntityRemoteClientContainer container;
        private final Object id;

        public EntityObjectImpl(EntityRemoteClientContainer container, Object id) {
            this.container = container;
            this.id = id;
        }

        public EJBHome getEJBHome() throws EJBException {
            return container.getEJBHome();
        }

        public Object getPrimaryKey() throws EJBException {
            return id;
        }

        public boolean isIdentical(EJBObject obj) throws EJBException {
            if (obj instanceof EntityObjectImpl) {
                EntityObjectImpl other = (EntityObjectImpl) obj;
                return other.container == container && other.id.equals(id);
            }
            return false;
        }

        public Handle getHandle() throws RemoteException {
            throw new UnsupportedOperationException();
        }

        public void remove() throws RemoveException, RemoteException {
            container.remove(id);
        }
    }


    private static class EntityMetaData implements EJBMetaData, Serializable {
        private final EJBHome homeProxy;
        private final Class home;
        private final Class remote;
        private final Class pkClass;

        public EntityMetaData(EJBHome homeProxy, Class home, Class remote, Class pkClass) {
            this.homeProxy = homeProxy;
            this.home = home;
            this.remote = remote;
            this.pkClass = pkClass;
        }

        public EJBHome getEJBHome() {
            return homeProxy;
        }

        public Class getHomeInterfaceClass() {
            return home;
        }

        public Class getRemoteInterfaceClass() {
            return remote;
        }

        public Class getPrimaryKeyClass() {
            return pkClass;
        }

        public boolean isSession() {
            return false;
        }

        public boolean isStatelessSession() {
            return false;
        }
    }
}
