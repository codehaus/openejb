package org.openejb.core.stateful;

import javax.ejb.SessionBean;
import javax.transaction.Transaction;

public class BeanEntry implements java.io.Serializable {
    protected final SessionBean bean;
    protected Object primaryKey;
    protected Object ancillaryState;
    protected transient Transaction transaction;
    protected long timeStamp;
    protected long timeOutInterval;
    protected boolean inQue = false;

    protected BeanEntry(SessionBean beanInstance, Object primKey, Object ancillary, long timeOut) {
        bean = beanInstance;
        primaryKey = primKey;
        ancillaryState = ancillary;
        transaction = null;
        timeStamp = System.currentTimeMillis();
        timeOutInterval = timeOut;
    }

    protected boolean isTimedOut() {
        if (timeOutInterval == 0)
            return false;
        long now = System.currentTimeMillis();
        return (now - timeStamp) > timeOutInterval;
    }

    protected void resetTimeOut() {
        if (timeOutInterval > 0) {
            timeStamp = System.currentTimeMillis();
        }
    }
}         
