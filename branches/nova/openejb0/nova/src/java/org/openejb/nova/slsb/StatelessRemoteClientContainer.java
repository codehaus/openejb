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
package org.openejb.nova.slsb;

import java.lang.reflect.Method;
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

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
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
public class StatelessRemoteClientContainer implements EJBRemoteClientContainer {
    private Interceptor firstInterceptor;
    private final int createIndex;
    private final EJBHome homeProxy;
    private final EJBObject objectProxy;
    private final EJBMetaData ejbMetadata;
    private final HomeHandle homeHandle;
    private final Handle handle;

    public StatelessRemoteClientContainer(Interceptor firstInterceptor, MethodSignature[] signatures, Class home, Class remote) {
        this.firstInterceptor = firstInterceptor;

        // Create LocalHome proxy
        EJBProxyFactory homeFactory = new EJBProxyFactory(StatelessHomeImpl.class, home);
        homeProxy = (EJBHome) homeFactory.create(new StatelessHomeInterceptor(), new Class[]{StatelessRemoteClientContainer.class}, new Object[]{this});

        // get the super index of the create method
        try {
            Class homeProxyClass = homeProxy.getClass();
            createIndex = EJBProxyInterceptor.getSuperIndex(homeProxyClass, homeProxyClass.getMethod("create", null));
        } catch (Exception e) {
            throw new AssertionError("No create method found on home interface: " + home.getName());
        }

        // Create LocalObject Proxy
        EJBProxyFactory objectFactory = new EJBProxyFactory(StatelessObjectImpl.class, remote);
        EJBProxyInterceptor methodInterceptor = new EJBProxyInterceptor(this, EJBInvocationType.REMOTE, objectFactory.getType(), signatures);
        objectProxy = (EJBObject) objectFactory.create(methodInterceptor, new Class[]{StatelessRemoteClientContainer.class}, new Object[]{this});

        this.ejbMetadata = new StatelessMetaData(homeProxy, home, remote);
        this.homeHandle = null;
        this.handle = null;
    }

    public EJBHome getEJBHome() {
        return homeProxy;
    }

    public EJBObject getEJBObject(Object primaryKey) {
        return objectProxy;
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
     * Base class for EJBLocalHome invocations. Handles operations which can
     * be performed directly by the proxy.
     */
    public static abstract class StatelessHomeImpl implements EJBHome {
        private final StatelessRemoteClientContainer container;

        public StatelessHomeImpl(StatelessRemoteClientContainer container) {
            this.container = container;
        }

        public EJBMetaData getEJBMetaData() throws RemoteException {
            return container.ejbMetadata;
        }

        public HomeHandle getHomeHandle() throws RemoteException {
            return container.homeHandle;
        }

        public void remove(Handle handle) throws RemoteException, RemoveException {
            throw new RemoteException("Cannot use remove(Handle) on a Stateless SessionBean");
        }

        public void remove(Object primaryKey) throws RemoveException {
            throw new RemoveException("Cannot use remove(Object) on a Stateless SessionBean");
        }
    }

    /**
     * Handler for EJBHome invocations that cannot be handled
     * directly by the proxy.
     */
    private class StatelessHomeInterceptor implements MethodInterceptor {
        public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
            if (methodProxy.getSuperIndex() == createIndex) {
                return objectProxy;
            }
            throw new IllegalStateException("Cannot use home method on a Stateless SessionBean");
        }
    }

    /**
     * Base class for EJBLocal invocations. Handles operations which can
     * be performed directly by the proxy.
     */
    public static abstract class StatelessObjectImpl implements EJBObject {
        private StatelessRemoteClientContainer container;

        public StatelessObjectImpl(StatelessRemoteClientContainer container) {
            this.container = container;
        }

        public EJBHome getEJBHome() {
            return container.homeProxy;
        }

        public boolean isIdentical(EJBObject obj) {
            return obj == this;
        }

        public Object getPrimaryKey() throws EJBException {
            throw new EJBException("Cannot use getPrimaryKey() on a Stateless SessionBean");
        }

        public Handle getHandle() throws RemoteException {
            return container.handle;
        }

        public void remove() throws RemoveException {
        }
    }

    private static class StatelessMetaData implements EJBMetaData {
        private final EJBHome homeProxy;
        private final Class home;
        private final Class remote;

        public StatelessMetaData(EJBHome homeProxy, Class home, Class remote) {
            this.homeProxy = homeProxy;
            this.home = home;
            this.remote = remote;
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
            throw new IllegalStateException("getPrimaryKeyClass is not supported for SessionBean");
        }

        public boolean isSession() {
            return true;
        }

        public boolean isStatelessSession() {
            return true;
        }
    }
}
