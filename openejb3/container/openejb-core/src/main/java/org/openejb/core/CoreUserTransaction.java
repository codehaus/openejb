package org.openejb.core;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

/**
 * @org.apache.xbean.XBean element="userTransaction"
 */
public class CoreUserTransaction implements javax.transaction.UserTransaction, java.io.Serializable {

    private transient TransactionManager transactionManager;

    private transient final org.apache.log4j.Category transactionLogger;

    public CoreUserTransaction(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
        transactionLogger = org.apache.log4j.Category.getInstance("Transaction");
    }

    private TransactionManager transactionManager() {
        // DMB: taking this out is fine unless it is serialized as part of a stateful sessionbean passivation
        // when the bean is activated
//        if (transactionManager == null) {
//            transactionManager = org.openejb.OpenEJB.getTransactionManager();
//        }
        return transactionManager;
    }

    public void begin() throws NotSupportedException, SystemException {
        transactionManager().begin();
        if (transactionLogger.isInfoEnabled()) {
            transactionLogger.info("Started user transaction " + transactionManager().getTransaction());
        }
    }

    public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException,
            SecurityException, IllegalStateException, SystemException {
        if (transactionLogger.isInfoEnabled()) {
            transactionLogger.info("Committing user transaction " + transactionManager().getTransaction());
        }
        transactionManager().commit();
    }

    public void rollback() throws IllegalStateException, SecurityException, SystemException {
        if (transactionLogger.isInfoEnabled()) {
            transactionLogger.info("Rolling back user transaction " + transactionManager().getTransaction());
        }
        transactionManager().rollback();
    }

    public int getStatus() throws SystemException {
        int status = transactionManager().getStatus();
        if (transactionLogger.isInfoEnabled()) {
            transactionLogger.info("User transaction " + transactionManager().getTransaction() + " has status " + org.openejb.core.TransactionManagerWrapper.getStatus(status));
        }
        return status;
    }

    public void setRollbackOnly() throws javax.transaction.SystemException {
        if (transactionLogger.isInfoEnabled()) {
            transactionLogger.info("Marking user transaction for rollback: " + transactionManager().getTransaction());
        }
        transactionManager().setRollbackOnly();
    }

    public void setTransactionTimeout(int seconds) throws SystemException {
        transactionManager().setTransactionTimeout(seconds);
    }

}