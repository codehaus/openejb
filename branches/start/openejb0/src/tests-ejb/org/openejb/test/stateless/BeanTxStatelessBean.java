/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "Exolab" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of Exoffice Technologies.  For written permission,
 *    please contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Exoffice Technologies. Exolab is a registered
 *    trademark of Exoffice Technologies.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.exolab.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY EXOFFICE TECHNOLOGIES AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * EXOFFICE TECHNOLOGIES OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 1999 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.test.stateless;

import java.rmi.RemoteException;
import java.sql.*;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;
import javax.ejb.*;
import javax.naming.InitialContext;
import javax.sql.*;
import javax.transaction.RollbackException;
import javax.transaction.UserTransaction;
import org.openejb.test.object.Account;
import org.openejb.test.object.OperationsPolicy;
import org.openejb.test.object.Transaction;

/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class BeanTxStatelessBean implements javax.ejb.SessionBean{

    
    private String name;
    private SessionContext ejbContext;
    private InitialContext jndiContext;
    public final String jndiDatabaseEntry = "jdbc/stateless/beanManagedTransaction/database";


    
    //=============================
    // Home interface methods
    //    
    
    //    
    // Home interface methods
    //=============================
    

    //=============================
    // Remote interface methods
    //    
    
    public Transaction getUserTransaction() throws RemoteException{
        
        UserTransaction ut = null;
        try{
            ut = ejbContext.getUserTransaction();
        } catch (IllegalStateException ise){
            throw new RemoteException(ise.getMessage());
        }
        if (ut == null) return null;
        return new Transaction(ut);
    }
    
    public Transaction jndiUserTransaction() throws RemoteException{
        UserTransaction ut = null;
        try{
            ut = (UserTransaction)jndiContext.lookup("java:comp/UserTransaction");
        } catch (Exception e){
            throw new RemoteException(e.getMessage());
        }
        if (ut == null) return null;
        return new Transaction(ut);
    }

    public void openAccount(Account acct, Boolean rollback) throws RemoteException, RollbackException{
        
        try{
            
            DataSource ds = (DataSource)jndiContext.lookup("java:comp/env/database");
            Connection con = ds.getConnection();
            
            UserTransaction ut = ejbContext.getUserTransaction();
            /*[1] Begin the transaction */
            ut.begin();


            /*[2] Update the table */
            PreparedStatement stmt = con.prepareStatement("insert into Account (SSN, FirstName, LastName, Balance) values (?,?,?,?)");
            stmt.setString(1, acct.ssn);
            stmt.setString(2, acct.firstName);
            stmt.setString(3, acct.lastName);
            stmt.setInt(4, acct.balance);
            stmt.executeUpdate();

            /*[3] Commit or Rollback the transaction */
            if (rollback.booleanValue()) ut.setRollbackOnly();
            
            /*[4] Commit or Rollback the transaction */
            ut.commit();
            

            /*[4] Clean up */
            stmt.close();
            con.close();
        } catch (RollbackException re){
            throw re;
        } catch (Exception e){
            e.printStackTrace();
            throw new RemoteException("[Bean] "+e.getClass().getName()+" : "+e.getMessage());
        }
    }

    public Account retreiveAccount(String ssn) throws RemoteException {
        Account acct = new Account();
        try{
            DataSource ds = (DataSource)jndiContext.lookup("java:comp/env/database");
            Connection con = ds.getConnection();

            PreparedStatement stmt = con.prepareStatement("select * from Account where SSN = ?");
            stmt.setString(1, ssn);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) return null;
            
            acct.ssn = rs.getString(2);
            acct.firstName = rs.getString(3);
            acct.lastName = rs.getString(4);
            acct.balance = rs.getInt(5);

            stmt.close();
            con.close();
        } catch (Exception e){
            e.printStackTrace();
            throw new RemoteException("[Bean] "+e.getClass().getName()+" : "+e.getMessage());
        }
        return acct;
    }


    //    
    // Remote interface methods
    //=============================


    //=================================
    // SessionBean interface methods
    //    
    /**
     * 
     * @param name
     * @exception javax.ejb.CreateException
     */
    public void ejbCreate() throws javax.ejb.CreateException{
        try {
            jndiContext = new InitialContext(); 
        } catch (Exception e){
            throw new CreateException("Can not get the initial context: "+e.getMessage());
        }
    }
    /**
     * Set the associated session context. The container calls this method
     * after the instance creation.
     */
    public void setSessionContext(SessionContext ctx) throws EJBException,RemoteException {
        ejbContext = ctx;
    }
    /**
     * A container invokes this method before it ends the life of the session
     * object. This happens as a result of a client's invoking a remove
     * operation, or when a container decides to terminate the session object
     * after a timeout.
     */
    public void ejbRemove() throws EJBException,RemoteException {
    }
    /**
     * The activate method is called when the instance is activated
     * from its "passive" state. The instance should acquire any resource
     * that it has released earlier in the ejbPassivate() method.
     */
    public void ejbActivate() throws EJBException,RemoteException {
    }
    /**
     * The passivate method is called before the instance enters
     * the "passive" state. The instance should release any resources that
     * it can re-acquire later in the ejbActivate() method.
     */
    public void ejbPassivate() throws EJBException,RemoteException {
    }
    //    
    // SessionBean interface methods
    //==================================
    
}
