package org.openejb.test.stateful;

import java.rmi.RemoteException;

import org.openejb.test.TestFailureException;

/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public interface EncStatefulObject extends javax.ejb.EJBObject{
    
    public void lookupEntityBean()    throws TestFailureException, RemoteException;
    public void lookupStatefulBean()  throws TestFailureException, RemoteException;
    public void lookupStatelessBean() throws TestFailureException, RemoteException;

    public void lookupResource() throws TestFailureException, RemoteException;

    public void lookupStringEntry()  throws TestFailureException, RemoteException;
    public void lookupDoubleEntry()  throws TestFailureException, RemoteException;
    public void lookupLongEntry()    throws TestFailureException, RemoteException;
    public void lookupFloatEntry()   throws TestFailureException, RemoteException;
    public void lookupIntegerEntry() throws TestFailureException, RemoteException;
    public void lookupShortEntry()   throws TestFailureException, RemoteException;
    public void lookupBooleanEntry() throws TestFailureException, RemoteException;
    public void lookupByteEntry()    throws TestFailureException, RemoteException;

}
