package org.openejb.spi;

import javax.transaction.TransactionManager;

public interface TransactionService extends Service {
    TransactionManager getTransactionManager();

}