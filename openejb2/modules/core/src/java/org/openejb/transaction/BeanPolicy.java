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
package org.openejb.transaction;

import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.core.service.InvocationResult;
import org.apache.geronimo.transaction.TransactionContext;
import org.apache.geronimo.transaction.UnspecifiedTransactionContext;

import org.openejb.EJBInvocation;

/**
 *
 *
 * @version $Revision$ $Date$
 */
public class BeanPolicy {
    private static final Log log = LogFactory.getLog(BeanPolicy.class);

    public static final TransactionPolicy Stateless = new TransactionPolicy() {
        public InvocationResult invoke(Interceptor interceptor, EJBInvocation ejbInvocation, TransactionManager txnManager) throws Throwable {
            TransactionContext clientContext = TransactionContext.getContext();
            if (clientContext != null) {
                clientContext.suspend();
            }
            try {
                TransactionContext beanContext = new UnspecifiedTransactionContext();
                TransactionContext.setContext(beanContext);
                beanContext.begin();
                ejbInvocation.setTransactionContext(beanContext);
                try {
                    InvocationResult result = interceptor.invoke(ejbInvocation);
                    if (beanContext != TransactionContext.getContext()) {
                        throw new UncommittedTransactionException();
                    }
                    beanContext.commit();
                    return result;
                } catch (Throwable t) {
                    try {
                        if (beanContext != TransactionContext.getContext()) {
                            TransactionContext.getContext().rollback();
                        }
                    } catch (Exception e) {
                        log.warn("Unable to roll back", e);
                    }
                    try {
                        beanContext.rollback();
                    } catch (Exception e) {
                        log.warn("Unable to roll back", e);
                    }
                    throw t;
                }
            } finally {
                ejbInvocation.setTransactionContext(clientContext);
                TransactionContext.setContext(clientContext);
                if (clientContext != null) {
                    clientContext.resume();
                }
            }
        }
        private Object readResolve() {
            return Stateless;
        }
    };

    public static final TransactionPolicy Stateful = new TransactionPolicy() {
        public InvocationResult invoke(Interceptor interceptor, EJBInvocation ejbInvocation, TransactionManager txnManager) throws Throwable {
            throw new IllegalStateException("Not yet implemented");
        }
        private Object readResolve() {
            return Stateful;
        }
    };
}
