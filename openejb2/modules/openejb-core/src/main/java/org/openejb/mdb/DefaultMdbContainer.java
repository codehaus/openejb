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
package org.openejb.mdb;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.interceptor.Interceptor;
import org.apache.geronimo.interceptor.Invocation;
import org.apache.geronimo.interceptor.InvocationResult;
import org.apache.geronimo.timer.PersistentTimer;
import org.apache.geronimo.transaction.TrackedConnectionAssociator;
import org.apache.geronimo.transaction.context.TransactionContext;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.apache.geronimo.transaction.context.UserTransactionImpl;
import org.openejb.CallbackMethod;
import org.openejb.ConnectionTrackingInterceptor;
import org.openejb.EJBInstanceContext;
import org.openejb.EJBInterfaceType;
import org.openejb.EjbCallbackInvocation;
import org.openejb.EjbInvocation;
import org.openejb.EjbInvocationImpl;
import org.openejb.ExtendedEjbDeployment;
import org.openejb.MdbContainer;
import org.openejb.SystemExceptionInterceptor;
import org.openejb.dispatch.DispatchInterceptor;
import org.openejb.naming.ComponentContextInterceptor;
import org.openejb.security.EJBIdentityInterceptor;

import javax.ejb.Timer;


/**
 * @version $Revision$ $Date$
 */
public class DefaultMdbContainer implements MdbContainer {
    private static final Log log = LogFactory.getLog(DefaultMdbContainer.class);
    private final Interceptor invocationChain;
    private final Interceptor callbackChain;
    private final PersistentTimer transactedTimer;
    private final PersistentTimer nontransactionalTimer;
    private final TransactionContextManager transactionContextManager;
    private final UserTransactionImpl userTransaction;

    public DefaultMdbContainer(
            TransactionContextManager transactionContextManager,
            TrackedConnectionAssociator trackedConnectionAssociator,
            PersistentTimer transactionalTimer,
            PersistentTimer nontransactionalTimer,
            boolean doAsCurrentCaller) throws Exception {

        this.transactionContextManager = transactionContextManager;
        this.userTransaction = new UserTransactionImpl(transactionContextManager, trackedConnectionAssociator);
        this.transactedTimer = transactionalTimer;
        this.nontransactionalTimer = nontransactionalTimer;

        //
        // build the normal invocation processing chain (built in reverse order)
        //

        // last interceptor is always the dispatcher
        Interceptor invocationChain = new DispatchInterceptor();

        // JNDI ENC interceptor
        invocationChain = new ComponentContextInterceptor(invocationChain);

        // Interceptor that changes security identity to that of the caller
        if (doAsCurrentCaller) {
            invocationChain = new EJBIdentityInterceptor(invocationChain);
        }

        invocationChain = new ConnectionTrackingInterceptor(invocationChain, trackedConnectionAssociator);

        // create the user transaction if bean managed
        invocationChain = new MdbInstanceInterceptor(invocationChain);

        // logs system exceptions
        invocationChain = new SystemExceptionInterceptor(invocationChain);

        this.invocationChain = invocationChain;

        //
        // Callback chain is used for ejb state change callbacks
        //

        Interceptor callbackChain = new MdbCallbackInterceptor();

        // JNDI ENC interceptor
        callbackChain = new ComponentContextInterceptor(callbackChain);

        // Interceptor that changes security identity to that of the caller
        if (doAsCurrentCaller) {
            callbackChain = new EJBIdentityInterceptor(callbackChain);
        }
        this.callbackChain = callbackChain;
    }

    public TransactionContextManager getTransactionContextManager() {
        return transactionContextManager;
    }

    public UserTransactionImpl getUserTransaction() {
        return userTransaction;
    }

    public PersistentTimer getTransactedTimer() {
        return transactedTimer;
    }

    public PersistentTimer getNontransactedTimer() {
        return nontransactionalTimer;
    }

    public InvocationResult invoke(Invocation invocation) throws Throwable {
        return invocationChain.invoke(invocation);
    }

    public void setContext(EJBInstanceContext instanceContext, MdbContext mdbContext) throws Throwable {
        EjbCallbackInvocation invocation = new EjbCallbackInvocation(CallbackMethod.SET_CONTEXT, instanceContext, new Object[]{mdbContext});
        callbackChain.invoke(invocation);
    }

    public void ejbCreate(EJBInstanceContext instanceContext) throws Throwable {
        EjbCallbackInvocation invocation = new EjbCallbackInvocation(CallbackMethod.CREATE, instanceContext);
        callbackChain.invoke(invocation);
    }

    public void ejbRemove(EJBInstanceContext instanceContext) throws Throwable {
        EjbCallbackInvocation invocation = new EjbCallbackInvocation(CallbackMethod.REMOVE, instanceContext);
        callbackChain.invoke(invocation);
    }

    public void timeout(ExtendedEjbDeployment deployment, Object id, Timer timer, int ejbTimeoutIndex) {
        EjbInvocation invocation = new EjbInvocationImpl(EJBInterfaceType.TIMEOUT, id, ejbTimeoutIndex, new Object[]{timer});
        invocation.setEjbDeployment(deployment);

        // set the transaction context into the invocation object
        TransactionContext transactionContext = transactionContextManager.getContext();
        if (transactionContext == null) {
            throw new IllegalStateException("Transaction context has not been set");
        }
        invocation.setTransactionContext(transactionContext);

        Thread currentThread = Thread.currentThread();
        ClassLoader oldClassLoader = currentThread.getContextClassLoader();
        try {
            currentThread.setContextClassLoader(deployment.getClassLoader());
            invoke(invocation);
        } catch (Throwable throwable) {
            log.warn("Timer invocation failed", throwable);
        } finally {
            currentThread.setContextClassLoader(oldClassLoader);
        }
    }

}
