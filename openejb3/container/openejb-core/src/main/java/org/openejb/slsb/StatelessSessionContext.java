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
package org.openejb.slsb;

import java.security.Principal;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.SessionContext;
import javax.ejb.TimerService;
import javax.security.auth.Subject;
import javax.transaction.UserTransaction;
import javax.xml.rpc.handler.MessageContext;

import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.apache.geronimo.transaction.context.UserTransactionImpl;
import org.openejb.EJBContextImpl;
import org.openejb.EJBInstanceContext;
import org.openejb.EJBOperation;
import org.openejb.timer.TimerState;

/**
 * Implementation of SessionContext using the State pattern to determine
 * which methods can be called given the current state of the Session bean.
 *
 * @version $Revision$ $Date$
 */
public class StatelessSessionContext extends EJBContextImpl implements SessionContext {
    public StatelessSessionContext(StatelessInstanceContext context, TransactionContextManager transactionContextManager, UserTransactionImpl userTransaction) {
        super(context, transactionContextManager, userTransaction);
        state = StatelessSessionContext.INACTIVE;
    }

    void setState(EJBOperation operation) {
        state = states[operation.getOrdinal()];
        assert (state != null) : "Invalid EJBOperation for Stateless SessionBean, ordinal=" + operation.getOrdinal();

        if (userTransaction != null) {
            if (operation == EJBOperation.BIZMETHOD ||
                    operation == EJBOperation.ENDPOINT ||
                    operation == EJBOperation.TIMEOUT) {
                userTransaction.setOnline(true);
            } else {
                userTransaction.setOnline(false);
            }
        }
        context.setTimerServiceAvailable(timerServiceAvailable[operation.getOrdinal()]);
    }

    public boolean setTimerState(EJBOperation operation) {
        boolean oldTimerState = TimerState.getTimerState();
        TimerState.setTimerState(timerMethodsAvailable[operation.getOrdinal()]);
        return oldTimerState;
    }

    public MessageContext getMessageContext() throws IllegalStateException {
        return ((StatelessSessionContextState) state).getMessageContext((StatelessInstanceContext) context);
    }

    public abstract static class StatelessSessionContextState extends EJBContextState {
        protected MessageContext getMessageContext(StatelessInstanceContext context) {
            return context.getMessageContext();
        }
    }

    public static final StatelessSessionContextState INACTIVE = new StatelessSessionContextState() {
        public EJBHome getEJBHome(EJBInstanceContext context) {
            throw new IllegalStateException("getEJBHome() cannot be called when inactive");
        }

        public EJBLocalHome getEJBLocalHome(EJBInstanceContext context) {
            throw new IllegalStateException("getEJBLocalHome() cannot be called when inactive");
        }

        public EJBObject getEJBObject(EJBInstanceContext context) {
            throw new IllegalStateException("getEJBObject() cannot be called when inactive");
        }

        public EJBLocalObject getEJBLocalObject(EJBInstanceContext context) {
            throw new IllegalStateException("getEJBLocalObject() cannot be called when inactive");
        }

        public Principal getCallerPrincipal(Subject callerSubject) {
            throw new IllegalStateException("getCallerPrincipal() cannot be called when inactive");
        }

        public boolean isCallerInRole(String s, EJBInstanceContext context) {
            throw new IllegalStateException("isCallerInRole(String) cannot be called when inactive");
        }

        public UserTransaction getUserTransaction(UserTransaction userTransaction) {
            throw new IllegalStateException("getUserTransaction() cannot be called when inactive");
        }

        public void setRollbackOnly(EJBInstanceContext context, TransactionContextManager transactionContextManager) {
            throw new IllegalStateException("setRollbackOnly() cannot be called when inactive");
        }

        public boolean getRollbackOnly(EJBInstanceContext context, TransactionContextManager transactionContextManager) {
            throw new IllegalStateException("getRollbackOnly() cannot be called when inactive");
        }

        public MessageContext getMessageContext(StatelessInstanceContext context) {
            throw new IllegalStateException("getMessageContext() cannot be called when inactive");
        }

        public TimerService getTimerService(EJBInstanceContext context) {
            throw new IllegalStateException("getTimerService() cannot be called when inactive");
        }
    };

    public static final StatelessSessionContextState SETSESSIONCONTEXT = new StatelessSessionContextState() {
        public EJBObject getEJBObject(EJBInstanceContext context) {
            throw new IllegalStateException("getEJBObject() cannot be called from setSessionContext(SessionContext)");
        }

        public EJBLocalObject getEJBLocalObject(EJBInstanceContext context) {
            throw new IllegalStateException("getEJBLocalObject() cannot be called from setSessionContext(SessionContext)");
        }

        public Principal getCallerPrincipal(Subject callerSubject) {
            throw new IllegalStateException("getCallerPrincipal() cannot be called from setSessionContext(SessionContext)");
        }

        public boolean isCallerInRole(String s, EJBInstanceContext context) {
            throw new IllegalStateException("isCallerInRole(String) cannot be called from setSessionContext(SessionContext)");
        }

        public UserTransaction getUserTransaction(UserTransaction userTransaction) {
            throw new IllegalStateException("getUserTransaction() cannot be called from setSessionContext(SessionContext)");
        }

        public void setRollbackOnly(EJBInstanceContext context, TransactionContextManager transactionContextManager) {
            throw new IllegalStateException("setRollbackOnly() cannot be called from setSessionContext(SessionContext)");
        }

        public boolean getRollbackOnly(EJBInstanceContext context, TransactionContextManager transactionContextManager) {
            throw new IllegalStateException("getRollbackOnly() cannot be called from setSessionContext(SessionContext)");
        }

        public MessageContext getMessageContext(StatelessInstanceContext context) {
            throw new IllegalStateException("getMessageContext() cannot be called from setSessionContext(SessionContext)");
        }

        public TimerService getTimerService(EJBInstanceContext context) {
            throw new IllegalStateException("getTimerService() cannot be called from setSessionContext(SessionContext)");
        }
    };

    public static final StatelessSessionContextState EJBCREATEREMOVE = new StatelessSessionContextState() {
        public Principal getCallerPrincipal(Subject callerSubject) {
            throw new IllegalStateException("getCallerPrincipal() cannot be called from ejbCreate/ejbRemove");
        }

        public boolean isCallerInRole(String s, EJBInstanceContext context) {
            throw new IllegalStateException("isCallerInRole(String) cannot be called from ejbCreate/ejbRemove");
        }

        public void setRollbackOnly(EJBInstanceContext context, TransactionContextManager transactionContextManager) {
            throw new IllegalStateException("setRollbackOnly() cannot be called from ejbCreate/ejbRemove");
        }

        public boolean getRollbackOnly(EJBInstanceContext context, TransactionContextManager transactionContextManager) {
            throw new IllegalStateException("getRollbackOnly() cannot be called from ejbCreate/ejbRemove");
        }

        public MessageContext getMessageContext(StatelessInstanceContext context) {
            throw new IllegalStateException("getMessageContext() cannot be called from ejbCreate/ejbRemove");
        }
    };

    public static final StatelessSessionContextState BIZ_INTERFACE = new StatelessSessionContextState() {
        public MessageContext getMessageContext(StatelessInstanceContext context) {
            throw new IllegalStateException("getMessageContext() cannot be called in a business method invocation from component interface)");
        }
    };


    public static final StatelessSessionContextState BIZ_WSENDPOINT = new StatelessSessionContextState() {
    };

    public static final StatelessSessionContextState EJBTIMEOUT = new StatelessSessionContextState() {
        public MessageContext getMessageContext(StatelessInstanceContext context) {
            throw new IllegalStateException("getMessageContext() cannot be called from ejbTimeout");
        }
    };

    private static final StatelessSessionContextState states[] = new StatelessSessionContextState[EJBOperation.MAX_ORDINAL];

    static {
        states[EJBOperation.INACTIVE.getOrdinal()] = INACTIVE;
        states[EJBOperation.SETCONTEXT.getOrdinal()] = SETSESSIONCONTEXT;
        states[EJBOperation.EJBCREATE.getOrdinal()] = EJBCREATEREMOVE;
        states[EJBOperation.EJBREMOVE.getOrdinal()] = EJBCREATEREMOVE;
        states[EJBOperation.BIZMETHOD.getOrdinal()] = BIZ_INTERFACE;
        states[EJBOperation.ENDPOINT.getOrdinal()] = BIZ_WSENDPOINT;
        states[EJBOperation.TIMEOUT.getOrdinal()] = EJBTIMEOUT;
    }

    private static final boolean timerServiceAvailable[] = new boolean[EJBOperation.MAX_ORDINAL];

    static {
        timerServiceAvailable[EJBOperation.EJBCREATE.getOrdinal()] = true;
        timerServiceAvailable[EJBOperation.EJBREMOVE.getOrdinal()] = true;
        timerServiceAvailable[EJBOperation.BIZMETHOD.getOrdinal()] = true;
        timerServiceAvailable[EJBOperation.ENDPOINT.getOrdinal()] = true;
        timerServiceAvailable[EJBOperation.TIMEOUT.getOrdinal()] = true;
    }

    private static final boolean timerMethodsAvailable[] = new boolean[EJBOperation.MAX_ORDINAL];

    static {
        timerMethodsAvailable[EJBOperation.BIZMETHOD.getOrdinal()] = true;
        timerMethodsAvailable[EJBOperation.ENDPOINT.getOrdinal()] = true;
        timerMethodsAvailable[EJBOperation.TIMEOUT.getOrdinal()] = true;
    }


    public Object lookup(String name){
        //TODO: EJB 3
        throw new UnsupportedOperationException("lookup");
    }

    public Object getBusinessObject(Class businessInterface) {
        //TODO: EJB 3
        throw new UnsupportedOperationException("not implemented");
    }

    public Class getInvokedBusinessInterface() {
        //TODO: EJB 3
        throw new UnsupportedOperationException("not implemented");
    }

}
