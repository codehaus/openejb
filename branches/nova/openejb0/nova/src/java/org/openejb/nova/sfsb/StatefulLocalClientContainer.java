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
package org.openejb.nova.sfsb;

import java.lang.reflect.UndeclaredThrowableException;
import javax.ejb.EJBException;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.RemoveException;

import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.core.service.InvocationResult;

import org.openejb.nova.EJBInvocation;
import org.openejb.nova.EJBInvocationImpl;
import org.openejb.nova.EJBInvocationType;
import org.openejb.nova.EJBLocalClientContainer;
import org.openejb.nova.EJBProxyFactory;
import org.openejb.nova.EJBProxyInterceptor;
import org.openejb.nova.dispatch.MethodSignature;

/**
 * Container for the local interface to an StatefulSessionBean.
 * This container owns implementation of EJBLocalHome and EJBLocalObject
 * that can be used by a client in the same classloader as the server.
 *
 * The implementation of the interfaces is generated using cglib FastClass
 * proxies to avoid the overhead of native Java reflection.
 *
 * @version $Revision$ $Date$
 */
public class StatefulLocalClientContainer implements EJBLocalClientContainer {
    private final Interceptor firstInterceptor;

    private final EJBLocalHome homeProxy;

    private final int removeIndex;
    private final int[] operationMap;
    private final EJBProxyFactory proxyFactory;

    public StatefulLocalClientContainer(Interceptor firstInterceptor, MethodSignature[] signatures, Class localHome, Class local) {
        this.firstInterceptor = firstInterceptor;

        // Create LocalHome proxy
        EJBProxyFactory factory = new EJBProxyFactory(StatefulLocalHomeImpl.class, localHome);
        EJBProxyInterceptor methodInterceptor = new EJBProxyInterceptor(this, EJBInvocationType.LOCALHOME, factory.getType(), signatures);
        homeProxy = (EJBLocalHome) factory.create(methodInterceptor);

        // Create LocalObject Proxy
        proxyFactory = new EJBProxyFactory(StatefulLocalObjectImpl.class, local);
        operationMap = EJBProxyInterceptor.getOperationMap(EJBInvocationType.LOCAL, proxyFactory.getType(), signatures);

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
    }

    public EJBLocalHome getEJBLocalHome() {
        return homeProxy;
    }

    public EJBLocalObject getEJBLocalObject(Object primaryKey) {
        EJBProxyInterceptor methodInterceptor = new EJBProxyInterceptor(this, EJBInvocationType.LOCAL, operationMap, primaryKey);
        return (EJBLocalObject) proxyFactory.create(
                methodInterceptor,
                new Class[]{StatefulLocalClientContainer.class, Object.class},
                new Object[]{this, primaryKey});
    }

    private void remove(Object id) throws RemoveException {
        InvocationResult result;
        try {
            result = firstInterceptor.invoke(new EJBInvocationImpl(EJBInvocationType.LOCAL, id, removeIndex, null));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new EJBException(e);
        } catch (Error e) {
            throw e;
        } catch (Throwable t) {
            throw new UndeclaredThrowableException(t);
        }
        if (result.isException()) {
            throw (RemoveException) result.getException();
        }
    }

    public Object invoke(EJBInvocationType ejbInvocationType, Object id, int methodIndex, Object[] args) throws Throwable {
        InvocationResult result;
        try {
            EJBInvocation invocation = new EJBInvocationImpl(ejbInvocationType, id, methodIndex, args);
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
     * Base class for EJBLocalHome proxies.
     * Owns a reference to the container.
     */
    private abstract static class StatefulLocalHomeImpl implements EJBLocalHome {
        public void remove(Object primaryKey) throws RemoveException, EJBException {
            throw new RemoveException("Cannot use remove(Object) on a Stateful SessionBean");
        }
    }

    /**
     * Base class for EJBLocalObject proxies.
     * Owns a reference to the container and the id of the StatefulSessionBean.
     * Implements EJBLocalObject methods, such as getPrimaryKey(), that do
     * not require a trip to the server.
     */
    private abstract static class StatefulLocalObjectImpl implements EJBLocalObject {
        private final StatefulLocalClientContainer container;
        private final Object id;

        public StatefulLocalObjectImpl(StatefulLocalClientContainer container, Object id) {
            this.container = container;
            this.id = id;
        }

        public EJBLocalHome getEJBLocalHome() throws EJBException {
            return container.getEJBLocalHome();
        }

        public Object getPrimaryKey() throws EJBException {
            throw new EJBException("Cannot use getPrimaryKey() on a Stateful SessionBean");
        }

        public boolean isIdentical(EJBLocalObject obj) throws EJBException {
            if (obj instanceof StatefulLocalObjectImpl) {
                StatefulLocalObjectImpl other = (StatefulLocalObjectImpl) obj;
                return other.container == container && other.id.equals(id);
            }
            return false;
        }

        public void remove() throws RemoveException, EJBException {
            container.remove(id);
        }
    }
}
