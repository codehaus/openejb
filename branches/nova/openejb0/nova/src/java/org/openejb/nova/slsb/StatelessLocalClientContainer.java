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

import javax.ejb.EJBException;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.RemoveException;

import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.core.service.InvocationResult;

import org.openejb.nova.EJBInvocation;
import org.openejb.nova.EJBInvocationType;
import org.openejb.nova.EJBLocalClientContainer;
import org.openejb.nova.proxy.EJBProxyFactory;
import org.openejb.nova.dispatch.MethodSignature;
import org.openejb.nova.proxy.EJBProxyHandler;
import org.openejb.nova.proxy.EJBProxy;

/**
 * Container for the local interface of a Stateless SessionBean.
 * This container owns implementations of EJBLocalHome and EJBLocalObject
 * that can be used by a client in the same classloader as the server.
 *
 * The implementation of the interfaces is generated using cglib FastClass
 * proxies to avoid the overhead of native Java reflection.
 *
 * @version $Revision$ $Date$
 */
public class StatelessLocalClientContainer implements EJBLocalClientContainer {
    private final Interceptor firstInterceptor;
    private final EJBLocalHome homeProxy;
    private final EJBLocalObject objectProxy;

    /**
     * Constructor used to initialize the ClientContainer.
     * @param signatures the signatures of the virtual methods
     * @param localHome the class of the EJB's LocalHome interface
     * @param local the class of the EJB's LocalObject interface
     */
    public StatelessLocalClientContainer(Interceptor firstInterceptor, MethodSignature[] signatures, Class localHome, Class local) {
        assert firstInterceptor != null;
        this.firstInterceptor = firstInterceptor;

        // Create LocalHome proxy
        EJBProxyFactory homeFactory = new EJBProxyFactory(StatelessLocalHomeProxy.class, localHome);
        EJBProxyHandler homeHandler = new EJBProxyHandler(this, EJBInvocationType.LOCALHOME, homeFactory.getType(), signatures);
        homeProxy = (EJBLocalHome) homeFactory.create(homeHandler, new Class[] {EJBProxyHandler.class}, new Object[] {homeHandler});

        // Create LocalObject Proxy
        EJBProxyFactory objectFactory = new EJBProxyFactory(StatelessLocalObjectProxy.class, local);
        EJBProxyHandler objectHandler = new EJBProxyHandler(this, EJBInvocationType.LOCAL, objectFactory.getType(), signatures);
        objectProxy = (EJBLocalObject) objectFactory.create(objectHandler, new Class[]{EJBProxyHandler.class}, new Object[]{objectHandler});
    }

    public EJBLocalHome getEJBLocalHome() {
        return homeProxy;
    }

    public EJBLocalObject getEJBLocalObject(Object primaryKey) {
        return objectProxy;
    }

    public InvocationResult invoke(EJBInvocation ejbInvocation) throws Throwable {
        return firstInterceptor.invoke(ejbInvocation);
    }

    /**
     * Base class for the EJBLocalHome proxy. Handles operations which can
     * be performed directly by the proxy.
     */
    public static abstract class StatelessLocalHomeProxy extends EJBProxy implements EJBLocalHome {
        public StatelessLocalHomeProxy(EJBProxyHandler handler) {
            super(handler);
        }

        public void remove(Object primaryKey) throws RemoveException, EJBException {
            throw new RemoveException("Cannot use remove(Object) on a Stateless SessionBean");
        }
    }

    /**
     * Base class for the EJBLocal proxy. Handles operations which can
     * be performed directly by the proxy.
     */
    public static abstract class StatelessLocalObjectProxy extends EJBProxy implements EJBLocalObject {
        public StatelessLocalObjectProxy(EJBProxyHandler handler) {
            super(handler);
        }

        public EJBLocalHome getEJBLocalHome() throws EJBException {
            return ((StatelessLocalClientContainer)handler.getContainer()).homeProxy;
        }

        public boolean isIdentical(EJBLocalObject obj) throws EJBException {
            return obj == this;
        }

        public Object getPrimaryKey() throws EJBException {
            throw new EJBException("Cannot use getPrimaryKey() on a Stateless SessionBean");
        }

        public void remove() throws RemoveException, EJBException {
            // do nothing
        }
    }
}
