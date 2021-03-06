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

import java.util.Properties;
import javax.ejb.EJBMetaData;
import javax.ejb.Handle;
import javax.ejb.HomeHandle;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.transaction.RollbackException;

import org.openejb.test.TestManager;
import org.openejb.test.object.Account;
import org.openejb.test.object.Transaction;

/**
 * [1] Should be run as the first test suite of the StatelessTestClients
 */
public class StatelessBeanTxTests extends org.openejb.test.NamedTestCase {

    public final static String jndiEJBHomeEntry = "client/tests/stateless/BeanManagedTransactionTests/EJBHome";

    protected BeanTxStatelessHome ejbHome;
    protected BeanTxStatelessObject ejbObject;

    protected EJBMetaData ejbMetaData;
    protected HomeHandle ejbHomeHandle;
    protected Handle ejbHandle;
    protected Integer ejbPrimaryKey;

    protected InitialContext initialContext;

    public StatelessBeanTxTests() {
        super("Stateless.BeanManagedTransaction.");
    }

    /**
     * Sets up the fixture, for example, open a network connection.
     * This method is called before a test is executed.
     */
    protected void setUp() throws Exception {

        Properties properties = TestManager.getServer().getContextEnvironment();
        properties.put(Context.SECURITY_PRINCIPAL, "STATELESS_test00_CLIENT");
        properties.put(Context.SECURITY_CREDENTIALS, "STATELESS_test00_CLIENT");

        initialContext = new InitialContext(properties);

        /*[1] Get bean */
        Object obj = initialContext.lookup(jndiEJBHomeEntry);
        ejbHome = (BeanTxStatelessHome) javax.rmi.PortableRemoteObject.narrow(obj, BeanTxStatelessHome.class);
        ejbObject = ejbHome.create();

        /*[2] Create database table */
        TestManager.getDatabase().createAccountTable();
    }

    /**
     * Tears down the fixture, for example, close a network connection.
     * This method is called after a test is executed.
     */
    protected void tearDown() throws Exception {
        /*[1] Drop database table */
        TestManager.getDatabase().dropAccountTable();
    }


    /**
     * <B>11.6.1 Bean-managed transaction demarcation</B>
     * <P>
     * The Container must make the javax.transaction.UserTransaction interface available to
     * the enterprise bean�s business method via the javax.ejb.EJBContext interface and under the
     * environment entry java:comp/UserTransaction. When an instance uses the javax.trans-action.
     * UserTransaction interface to demarcate a transaction, the Container must enlist all the
     * resource managers used by the instance between the begin() and commit()�or rollback()�
     * methods with the transaction. When the instance attempts to commit the transaction, the Container is
     * responsible for the global coordination of the transaction commit.
     * </P>
     * <P>--------------------------------------------------------</P>
     * <P>
     * Check that a javax.transaction.UserTransaction can be obtained from
     * the javax.ejb.EJBContext
     * </P>
     */
    public void test01_EJBContext_getUserTransaction() {
        try {
            Transaction t = ejbObject.getUserTransaction();
            assertNotNull("UserTransaction is null.", t);
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    /**
     * <B>11.6.1 Bean-managed transaction demarcation</B>
     * <P>
     * The Container must make the javax.transaction.UserTransaction interface available to
     * the enterprise bean�s business method via the javax.ejb.EJBContext interface and under the
     * environment entry java:comp/UserTransaction. When an instance uses the javax.trans-action.
     * UserTransaction interface to demarcate a transaction, the Container must enlist all the
     * resource managers used by the instance between the begin() and commit()�or rollback()�
     * methods with the transaction. When the instance attempts to commit the transaction, the Container is
     * responsible for the global coordination of the transaction commit.
     * </P>
     * <P>--------------------------------------------------------</P>
     * <P>
     * Check that a javax.transaction.UserTransaction can be obtained from
     * the environment entry java:comp/UserTransaction
     * </P>
     */
    public void test02_java_comp_UserTransaction() {
        try {
            Transaction t = ejbObject.jndiUserTransaction();
            assertNotNull("UserTransaction is null. Could not retreive a UserTransaction from the bean's JNDI namespace.", t);
        } catch (Exception e) {
            fail("Could not retreive a UserTransaction from the bean's JNDI namespace. Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    /**
     * <B>11.6.1 Bean-managed transaction demarcation</B>
     * <P>
     * The Container must throw the java.lang.IllegalStateException if an instance of a bean
     * with bean-managed transaction demarcation attempts to invoke the setRollbackOnly() or
     * getRollbackOnly() method of the javax.ejb.EJBContext interface.
     * </P>
     * <P>--------------------------------------------------------</P>
     * <P>
     * Test that setRollbackOnly() throws a java.lang.IllegalStateException
     * </P>
     */
    public void TODO_test03_EJBContext_setRollbackOnly() {
        try {

        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    /**
     * <B>11.6.1 Bean-managed transaction demarcation</B>
     * <P>
     * The Container must throw the java.lang.IllegalStateException if an instance of a bean
     * with bean-managed transaction demarcation attempts to invoke the setRollbackOnly() or
     * getRollbackOnly() method of the javax.ejb.EJBContext interface.
     * </P>
     * <P>--------------------------------------------------------</P>
     * <P>
     * Test that getRollbackOnly() throws a java.lang.IllegalStateException
     * </P>
     */
    public void TODO_test04_EJBContext_getRollbackOnly() {
        try {

        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    /**
     *
     */
    public void test05_singleTransactionCommit() {
        try {
            Account expected = new Account("123-45-6789", "Joe", "Cool", 40000);
            Account actual = new Account();

            ejbObject.openAccount(expected, new Boolean(false));
            actual = ejbObject.retreiveAccount(expected.getSsn());

            assertNotNull("The transaction was not commited.  The record is null", actual);
            assertEquals("The transaction was not commited cleanly.", expected, actual);
        } catch (RollbackException re) {
            fail("Transaction was rolledback.  Received Exception " + re.getClass() + " : " + re.getMessage());
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    /**
     * This test does work for the IntraVM Server, but it fails on
     * the Remote Server.  For some reason, when the RollbackException is
     * sent to the client, the server blocks.
     */
    public void BUG_test06_singleTransactionRollback() {
        Account expected = new Account("234-56-7890", "Charlie", "Brown", 20000);
        Account actual = new Account();

        // Try and add the account in a transaction.  This should fail and 
        // throw a RollbackException
        try {
            ejbObject.openAccount(expected, new Boolean(true));
            fail("A javax.transaction.RollbackException should have been thrown.");
        } catch (RollbackException re) {
            // Good.
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }

        //// Now check that the account really wasn't added.
        //try{
        //    actual = ejbObject.retreiveAccount( expected.getSsn() );
        //    //assertTrue( "The transaction was commited when it should have been rolledback.", !expected.equals(actual) );
        //} catch (Exception e){
        //    fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        //}
    }


    /**
     * <B>11.6.1 Bean-managed transaction demarcation</B>
     * <P>
     * The Container must allow the enterprise bean instance to serially perform several transactions in a
     * method.
     * </P>
     */
    public void TODO_test07_serialTransactions() {
        try {

        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    /**
     * <B>11.6.1 Bean-managed transaction demarcation</B>
     * <P>
     * When an instance attempts to start a transaction using the
     * begin() method of the javax.transaction.UserTransaction
     * interface while the instance has not committed the previous
     * transaction, the Container must throw the
     * javax.transaction.NotSupportedException in the begin() method.
     * </P>
     */
    public void TODO_test08_nestedTransactions() {
        try {

        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }


    /**
     * <B>11.6.1 Bean-managed transaction demarcation</B>
     * <P>
     * If a stateless session bean instance starts a transaction in a
     * business method, it must commit the transaction before the
     * business method returns. The Container must detect the case in
     * which a transaction was started, but not completed, in the
     * business method, and handle it as follows:
     * <UL>
     * <LI>Log this as an application error to alert the system administrator.
     * <LI>Roll back the started transaction.
     * <LI>Discard the instance of the session bean.
     * <LI>Throw the java.rmi.RemoteException to the client.
     * </UL>
     * </P>
     */
    public void TODO_test09_beginWithNoCommit() {
        try {

        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    /**
     * <B>11.6.1 Bean-managed transaction demarcation</B>
     * <P>
     * The actions performed by the Container for an instance with bean-managed transaction are summarized
     * by the following table. T1 is a transaction associated with a client request, T2 is a transaction that is cur-rently
     * associated with the instance (i.e. a transaction that was started but not completed by a previous
     * business method).
     * </P>
     * <PRE>
     * =========================================================================
     * Container�s actions for methods of beans with bean-managed transaction
     * =========================================================================
     * <p/>
     * |      IF     |          AND             |          THEN
     * scenario  |   Client�s  | Transaction currently    | Transaction associated
     * | transaction | associated with instance | with the method is
     * ___________|_____________|__________________________|________________________
     * |             |                          |
     * 1       |  none       |  none                    |  none
     * ___________|_____________|__________________________|________________________
     * |             |                          |
     * 2       |  T1         |  none                    |  none
     * ___________|_____________|__________________________|________________________
     * |             |                          |
     * 3       |  none       |  T2                      |  T2
     * ___________|_____________|__________________________|________________________
     * |             |                          |
     * 4       |  T1         |  T2                      |  T2
     * ___________|_____________|__________________________|________________________
     * </PRE>
     * <P>
     * If the client request is not associated with a transaction and the instance is not associated with a
     * transaction, the container invokes the instance with an unspecified transaction context.
     * </P>
     * <P>--------------------------------------------------------</P>
     * <P>
     * Test scenario 1: none none<BR>
     * If the client's transaction is none and the transaction currently
     * associated with instance none then the transaction associated with the method is none.
     * </P>
     */
    public void TODO_test10_scenario1_NoneNone() {
        try {

        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

    /**
     * <B>11.6.1 Bean-managed transaction demarcation</B>
     * <P>
     * The actions performed by the Container for an instance with bean-managed transaction are summarized
     * by the following table. T1 is a transaction associated with a client request, T2 is a transaction that is cur-rently
     * associated with the instance (i.e. a transaction that was started but not completed by a previous
     * business method).
     * </P>
     * <PRE>
     * =========================================================================
     * Container�s actions for methods of beans with bean-managed transaction
     * =========================================================================
     * <p/>
     * |      IF     |          AND             |          THEN
     * scenario  |   Client�s  | Transaction currently    | Transaction associated
     * | transaction | associated with instance | with the method is
     * ___________|_____________|__________________________|________________________
     * |             |                          |
     * 1       |  none       |  none                    |  none
     * ___________|_____________|__________________________|________________________
     * |             |                          |
     * 2       |  T1         |  none                    |  none
     * ___________|_____________|__________________________|________________________
     * |             |                          |
     * 3       |  none       |  T2                      |  T2
     * ___________|_____________|__________________________|________________________
     * |             |                          |
     * 4       |  T1         |  T2                      |  T2
     * ___________|_____________|__________________________|________________________
     * </PRE>
     * <P>
     * If the client is associated with a transaction T1, and the instance is not associated with a transaction,
     * the container suspends the client�s transaction association and invokes the method with
     * an unspecified transaction context. The container resumes the client�s ntransaction association
     * (T1) when the method completes.
     * </P>
     * <P>--------------------------------------------------------</P>
     * <P>
     * Test scenario 2: T1 none<BR>
     * </P>
     */
    public void TODO_test11_scenario2_T1None() {
        try {

        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        }
    }

}

