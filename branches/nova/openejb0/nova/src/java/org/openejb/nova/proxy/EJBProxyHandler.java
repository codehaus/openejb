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
package org.openejb.nova.proxy;

import java.io.Serializable;
import java.lang.reflect.Method;
import javax.ejb.EJBException;

import org.apache.geronimo.core.service.InvocationResult;
import org.apache.geronimo.security.ContextManager;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.openejb.nova.ClientContainer;
import org.openejb.nova.EJBInvocation;
import org.openejb.nova.EJBInvocationImpl;
import org.openejb.nova.EJBInvocationImplRemote;
import org.openejb.nova.EJBInvocationType;
import org.openejb.nova.dispatch.MethodSignature;

/**
 * @version $Revision$ $Date$
 */
public class EJBProxyHandler implements MethodInterceptor, Serializable {
    /**
     * The client container that is invoked from intercepted methods.
     */
    private final ClientContainer container;

    /**
     * The type of the ejb invocation.  This is used during construction of the EJBInvocation object.
     */
    private final EJBInvocationType ejbInvocationType;

    /**
     * Id of the object being invoked.  This is used during construction of the EJBInvocation object.
     * May be null for a home proxy.
     */
    private final Object id;

    /**
     * Map from interface method ids to vop ids.
     */
    private final int[] operationMap;

    public EJBProxyHandler(ClientContainer container, EJBInvocationType ejbInvocationType, Class proxyType, MethodSignature[] signatures) {
        this(container, ejbInvocationType, EBJProxyHelper.getOperationMap(ejbInvocationType, proxyType, signatures), null);
    }

    public EJBProxyHandler(ClientContainer container, EJBInvocationType ejbInvocationType, int[] operationMap, Object id) {
        assert container != null;
        assert operationMap != null;

        this.container = container;
        this.ejbInvocationType = ejbInvocationType;
        this.operationMap = operationMap;
        this.id = id;
    }

    public ClientContainer getContainer() {
        return container;
    }

    public Object getId() {
        return id;
    }

    /**
     * Handles an invocation on a proxy
     * @param object the proxy instance
     * @param method java method that was invoked
     * @param args arguments to the mentod
     * @param methodProxy a CGLib method proxy of the method invoked
     * @return the result of the invocation
     * @throws java.lang.Throwable if any exceptions are thrown by the implementation method
     */
    public Object intercept(Object object, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        int methodIndex = operationMap[methodProxy.getSuperIndex()];
        InvocationResult result;
        try {
            EJBInvocation invocation = null;
            if (ejbInvocationType.isLocal()) {
                invocation = new EJBInvocationImpl(ejbInvocationType, id, methodIndex, args);
            } else {
                invocation = new EJBInvocationImplRemote(ejbInvocationType, id, methodIndex, args, ContextManager.getCurrentCallerId());
            }
            result = container.invoke(invocation);
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
}
