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

import javax.ejb.EJBHome;
import javax.ejb.EJBMetaData;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.HomeHandle;

import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.core.service.InvocationResult;

import org.openejb.nova.EJBInvocation;
import org.openejb.nova.EJBInvocationType;
import org.openejb.nova.EJBRemoteClientContainer;
import org.openejb.nova.dispatch.MethodSignature;
import org.openejb.nova.proxy.EJBProxyHelper;
import org.openejb.nova.proxy.EJBMetaDataImpl;
import org.openejb.nova.proxy.EJBProxy;
import org.openejb.nova.proxy.EJBProxyFactory;
import org.openejb.nova.proxy.EJBProxyHandler;

/**
 * @version $Revision$ $Date$
 */
public class EntityRemoteClientContainer implements EJBRemoteClientContainer {
    private Interceptor firstInterceptor;

    private final EJBHome homeProxy;

    private final EJBProxyFactory objectFactory;
    private final int[] operationMap;

    private final EJBMetaData ejbMetadata;
    private final HomeHandle homeHandle;

    public EntityRemoteClientContainer(Interceptor firstInterceptor, MethodSignature[] signatures, Class home, Class remote, Class pkClass) {
        this.firstInterceptor = firstInterceptor;

        // Create Home proxy
        EJBProxyFactory homeFactory = new EJBProxyFactory(EntityHomeProxy.class, home);
        EJBProxyHandler homeHandler = new EJBProxyHandler(this, EJBInvocationType.HOME, homeFactory.getType(), signatures);
        homeProxy = (EJBHome) homeFactory.create(homeHandler, new Class[]{EJBProxyHandler.class}, new Object[]{homeHandler});

        // Create Remote Proxy
        objectFactory = new EJBProxyFactory(EntityObjectProxy.class, remote);
        operationMap = EJBProxyHelper.getOperationMap(EJBInvocationType.REMOTE, objectFactory.getType(), signatures);

        ejbMetadata = EJBMetaDataImpl.createEntity(homeProxy, home, remote, pkClass);
        homeHandle = null;
    }

    public EJBHome getEJBHome() {
        return homeProxy;
    }

    public EJBObject getEJBObject(Object primaryKey) {
        EJBProxyHandler objectHandler = new EJBProxyHandler(this, EJBInvocationType.REMOTE, operationMap, primaryKey);
        return (EJBObject) objectFactory.create(objectHandler, new Class[]{EJBProxyHandler.class}, new Object[]{objectHandler});
    }

    public InvocationResult invoke(EJBInvocation ejbInvocation) throws Throwable {
        return firstInterceptor.invoke(ejbInvocation);
    }

    /**
     * Base class for then EJBHome proxy.
     * Owns a reference to the container.
     */
    private abstract static class EntityHomeProxy extends EJBProxy implements EJBHome {
        public EntityHomeProxy(EJBProxyHandler handler) {
            super(handler);
        }

        public EJBMetaData getEJBMetaData() {
            return ((EntityRemoteClientContainer) handler.getContainer()).ejbMetadata;
        }

        public HomeHandle getHomeHandle() {
            return ((EntityRemoteClientContainer) handler.getContainer()).homeHandle;
        }
    }

    /**
     * Base class for EJBLocalObject proxies.
     * Owns a reference to the container and the id of the Entity.
     * Implements EJBLocalObject methods, such as getPrimaryKey(), that do
     * not require a trip to the server.
     */
    private abstract static class EntityObjectProxy extends EJBProxy implements EJBObject {
        public EntityObjectProxy(EJBProxyHandler handler) {
            super(handler);
        }

        public EJBHome getEJBHome() {
            return ((EJBRemoteClientContainer) handler.getContainer()).getEJBHome();
        }

        public Object getPrimaryKey() {
            return handler.getId();
        }

        public boolean isIdentical(EJBObject obj) {
            if (obj instanceof EntityObjectProxy) {
                EntityObjectProxy other = (EntityObjectProxy) obj;
                return other.handler.getContainer() == handler.getContainer() &&
                        other.handler.getId().equals(handler.getId());
            }
            return false;
        }

        public Handle getHandle() {
            throw new UnsupportedOperationException();
        }
    }
}
