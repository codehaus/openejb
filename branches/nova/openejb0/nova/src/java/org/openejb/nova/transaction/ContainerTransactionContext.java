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
package org.openejb.nova.transaction;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

/**
 *
 *
 * @version $Revision$ $Date$
 */
public class ContainerTransactionContext extends InheritableTransactionContext {
    private final TransactionManager txnManager;
    private Transaction transaction;

    public ContainerTransactionContext(TransactionManager txnManager) {
        this.txnManager = txnManager;
    }

    public void begin() throws SystemException, NotSupportedException {
        txnManager.begin();
        transaction = txnManager.getTransaction();
    }

    public void suspend() throws SystemException {
        Transaction suspendedTransaction = txnManager.suspend();
        assert (transaction == suspendedTransaction) : "suspend did not return our transaction";
    }

    public void resume() throws SystemException, InvalidTransactionException {
        txnManager.resume(transaction);
    }

    /**
     * TODO the exceptions thrown here are not all correct.  Don't throw a RollbackException after
     * a successful commit...??
     *
     * @throws HeuristicMixedException
     * @throws HeuristicRollbackException
     * @throws RollbackException
     * @throws SystemException
     */
    public void commit() throws HeuristicMixedException, HeuristicRollbackException, RollbackException, SystemException {
        try {
            try {
                flushState();
            } catch (Throwable t) {
                try {
                    txnManager.rollback();
                } catch (Throwable t1) {
                    log.error("Unable to roll back transaction", t1);
                }
                throw (RollbackException) new RollbackException("Could not flush state before commit").initCause(t);
            }
            try {
                beforeCommit();
            } catch (Exception e) {
                try {
                    txnManager.rollback();
                } catch (Throwable t1) {
                    log.error("Unable to roll back transaction", t1);
                }
                throw (RollbackException) new RollbackException("Could not flush state before commit").initCause(e);
            }
            txnManager.commit();
            try {
                afterCommit(true);
            } catch (Exception e) {
                try {
                    txnManager.rollback();
                } catch (Throwable t1) {
                    log.error("Unable to roll back transaction", t1);
                }
                throw (RollbackException) new RollbackException("Could not flush state before commit").initCause(e);
            }
        } finally {
            connectorAfterCommit();
            transaction = null;
        }
    }

    public void rollback() throws SystemException {
        try {
            txnManager.rollback();
        } finally {
            connectorAfterCommit();
            transaction = null;
        }
    }

    //Geronimo connector framework support
    public boolean isActive() {
        try {
            return txnManager.getStatus() == Status.STATUS_ACTIVE;
        } catch (SystemException e) {
            return false;
        }
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setRollbackOnly() throws IllegalStateException, SystemException {
        if (transaction == null) {
            throw new IllegalStateException("There is no transaction in progress.");
        }
        transaction.setRollbackOnly();
    }

    public boolean getRollbackOnly() throws SystemException {
        if (transaction == null) {
            throw new IllegalStateException("There is no transaction in progress.");
        }

        int status = transaction.getStatus();
        return (status == Status.STATUS_MARKED_ROLLBACK ||
                status == Status.STATUS_ROLLEDBACK ||
                status == Status.STATUS_ROLLING_BACK );
    }
}
