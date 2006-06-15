package org.openejb.test.stateful;

import org.openejb.test.object.OperationsPolicy;

/**
 * 
 * [10] Should be run as the nineth test suite of the BasicStatefulTestClients
 * 
 * <PRE>
 * =========================================================================
 * Operations allowed in the methods of a stateful SessionBean with 
 * bean-managed transaction demarcation
 * =========================================================================
 * 
 * Bean method           | Bean method can perform the following operations
 * ______________________|__________________________________________________
 *                       |
 * constructor           | -
 * ______________________|__________________________________________________
 *                       |
 * setSessionContext     |  SessionContext methods:
 *                       |     - getEJBHome
 *                       |  JNDI access to java:comp/env
 * ______________________|__________________________________________________
 *                       |
 * ejbCreate             |  SessionContext methods:
 * ejbRemove             |     - getEJBHome
 * ejbActivate           |     - getCallerPrincipal
 * ejbPassivate          |     - isCallerInRole   
 *                       |     - getEJBObject    
 *                       |     - getUserTransaction
 *                       |  JNDI access to java:comp/env
 *                       |  Resource manager access
 *                       |  Enterprise bean access
 * ______________________|__________________________________________________
 *                       |
 * business method       |  SessionContext methods:
 * from remote interface |     - getEJBHome        
 *                       |     - getCallerPrincipal
 *                       |     - isCallerInRole    
 *                       |     - getEJBObject      
 *                       |     - getUserTransaction
 *                       |  JNDI access to java:comp/env
 *                       |  Resource manager access
 *                       |  Enterprise bean access
 * ______________________|__________________________________________________
 * </PRE>
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class BMTStatefulAllowedOperationsTests extends BasicStatefulTestClient{

    public BMTStatefulAllowedOperationsTests(){
        super("BMTAllowedOperations.");
    }

    protected void setUp() throws Exception{
        super.setUp();
        Object obj = initialContext.lookup("client/tests/stateful/BeanManagedBasicStatefulHome");
        ejbHome = (BasicStatefulHome)javax.rmi.PortableRemoteObject.narrow( obj, BasicStatefulHome.class);
        ejbObject = ejbHome.create("Fifth Bean");
        ejbHandle = ejbObject.getHandle();
        /* These tests will only work if the specified
         * method has already been called by the container.
         *
         * TO DO:
         * Implement a little application senario to ensure
         * that all methods tested for below have been called
         * by the container.
         */         
    }
    
    protected void tearDown() throws Exception{
        ejbObject.remove();
        super.tearDown();
    }

    //=====================================
    // Test EJBContext allowed operations       
    //
    /**
     * <PRE>
     * Bean method           | Bean method can perform the following operations
     * ______________________|__________________________________________________
     *                       |
     * setSessionContext     |  SessionContext methods:
     *                       |     - getEJBHome
     *                       |  JNDI access to java:comp/env
     * ______________________|__________________________________________________
     * </PRE>
     */
    public void test01_setSessionContext(){
        try{
        OperationsPolicy policy = new OperationsPolicy();
        policy.allow( policy.Context_getEJBHome );
        policy.allow( policy.JNDI_access_to_java_comp_env );
        
        Object expected = policy;
        Object actual = ejbObject.getAllowedOperationsReport("setSessionContext");
        
        assertNotNull("The OperationsPolicy is null", actual );
        assertEquals( expected, actual );
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }
    /**
     * <PRE>
     * Bean method           | Bean method can perform the following operations
     * ______________________|__________________________________________________
     *                       |
     * ejbCreate             |  SessionContext methods:
     * ejbRemove             |     - getEJBHome
     * ejbActivate           |     - getCallerPrincipal
     * ejbPassivate          |     - isCallerInRole   
     *                       |     - getEJBObject    
     *                       |     - getUserTransaction
     *                       |  JNDI access to java:comp/env
     *                       |  Resource manager access
     *                       |  Enterprise bean access
     * ______________________|__________________________________________________
     * </PRE>
     */
    public void test02_ejbCreate(){             
        try{
        OperationsPolicy policy = new OperationsPolicy();
        policy.allow( policy.Context_getEJBHome );
        policy.allow( policy.Context_getCallerPrincipal );
        policy.allow( policy.Context_isCallerInRole );
        policy.allow( policy.Context_getEJBObject );
        policy.allow( policy.Context_getUserTransaction );
        policy.allow( policy.JNDI_access_to_java_comp_env );
        policy.allow( policy.Resource_manager_access );
        policy.allow( policy.Enterprise_bean_access );
        
        Object expected = policy;
        Object actual = ejbObject.getAllowedOperationsReport("ejbCreate");
        
        assertNotNull("The OperationsPolicy is null", actual );
        assertEquals( expected, actual );
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }
    /**
     * <PRE>
     * Bean method           | Bean method can perform the following operations
     * ______________________|__________________________________________________
     *                       |
     * ejbCreate             |  SessionContext methods:
     * ejbRemove             |     - getEJBHome
     * ejbActivate           |     - getCallerPrincipal
     * ejbPassivate          |     - isCallerInRole   
     *                       |     - getEJBObject    
     *                       |     - getUserTransaction
     *                       |  JNDI access to java:comp/env
     *                       |  Resource manager access
     *                       |  Enterprise bean access
     * ______________________|__________________________________________________
     * </PRE>
     */
    public void test03_ejbRemove(){
        try{
        /* TO DO:  This test needs unique functionality to work */
        OperationsPolicy policy = new OperationsPolicy();
        policy.allow( policy.Context_getEJBHome );
        policy.allow( policy.Context_getCallerPrincipal );
        policy.allow( policy.Context_isCallerInRole );
        policy.allow( policy.Context_getEJBObject );
        policy.allow( policy.Context_getUserTransaction );
        policy.allow( policy.JNDI_access_to_java_comp_env );
        policy.allow( policy.Resource_manager_access );
        policy.allow( policy.Enterprise_bean_access );
    
        Object expected = policy;
        Object actual = ejbObject.getAllowedOperationsReport("ejbRemove");
    
        assertNotNull("The OperationsPolicy is null", actual );
        assertEquals( expected, actual );
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }
    /**
     * <PRE>
     * Bean method           | Bean method can perform the following operations
     * ______________________|__________________________________________________
     *                       |
     * ejbCreate             |  SessionContext methods:
     * ejbRemove             |     - getEJBHome
     * ejbActivate           |     - getCallerPrincipal
     * ejbPassivate          |     - isCallerInRole   
     *                       |     - getEJBObject    
     *                       |     - getUserTransaction
     *                       |  JNDI access to java:comp/env
     *                       |  Resource manager access
     *                       |  Enterprise bean access
     * ______________________|__________________________________________________
     * </PRE>
     */
    public void test04_ejbActivate(){             
        try{
        OperationsPolicy policy = new OperationsPolicy();
        policy.allow( policy.Context_getEJBHome );
        policy.allow( policy.Context_getCallerPrincipal );
        policy.allow( policy.Context_isCallerInRole );
        policy.allow( policy.Context_getEJBObject );
        policy.allow( policy.Context_getUserTransaction );
        policy.allow( policy.JNDI_access_to_java_comp_env );
        policy.allow( policy.Resource_manager_access );
        policy.allow( policy.Enterprise_bean_access );
        
        Object expected = policy;
        Object actual = ejbObject.getAllowedOperationsReport("ejbActivate");
        
        assertNotNull("The OperationsPolicy is null", actual );
        assertEquals( expected, actual );
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }
    /**
     * <PRE>
     * Bean method           | Bean method can perform the following operations
     * ______________________|__________________________________________________
     *                       |
     * ejbCreate             |  SessionContext methods:
     * ejbRemove             |     - getEJBHome
     * ejbActivate           |     - getCallerPrincipal
     * ejbPassivate          |     - isCallerInRole   
     *                       |     - getEJBObject    
     *                       |     - getUserTransaction
     *                       |  JNDI access to java:comp/env
     *                       |  Resource manager access
     *                       |  Enterprise bean access
     * ______________________|__________________________________________________
     * </PRE>
     */
    public void test05_ejbPassivate(){             
        try{
        OperationsPolicy policy = new OperationsPolicy();
        policy.allow( policy.Context_getEJBHome );
        policy.allow( policy.Context_getCallerPrincipal );
        policy.allow( policy.Context_isCallerInRole );
        policy.allow( policy.Context_getEJBObject );
        policy.allow( policy.Context_getUserTransaction );
        policy.allow( policy.JNDI_access_to_java_comp_env );
        policy.allow( policy.Resource_manager_access );
        policy.allow( policy.Enterprise_bean_access );
        
        Object expected = policy;
        Object actual = ejbObject.getAllowedOperationsReport("ejbPassivate");
        
        assertNotNull("The OperationsPolicy is null", actual );
        assertEquals( expected, actual );
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }
    
    /**
     * <PRE>
     * Bean method           | Bean method can perform the following operations
     * ______________________|__________________________________________________
     *                       |
     * business method       |  SessionContext methods:
     * from remote interface |     - getEJBHome
     *                       |     - getCallerPrincipal
     *                       |     - isCallerInRole
     *                       |     - getEJBObject
     *                       |     - getUserTransaction
     *                       |  JNDI access to java:comp/env
     *                       |  Resource manager access
     *                       |  Enterprise bean access
     * ______________________|__________________________________________________
     * </PRE>
     */
    public void test06_businessMethod(){
        try{
        OperationsPolicy policy = new OperationsPolicy();
        policy.allow( policy.Context_getEJBHome );
        policy.allow( policy.Context_getCallerPrincipal );
        policy.allow( policy.Context_isCallerInRole );
        policy.allow( policy.Context_getEJBObject );
        policy.allow( policy.Context_getUserTransaction );
        policy.allow( policy.JNDI_access_to_java_comp_env );
        policy.allow( policy.Resource_manager_access );
        policy.allow( policy.Enterprise_bean_access );
    
        Object expected = policy;
        Object actual = ejbObject.getAllowedOperationsReport("businessMethod");
    
        assertNotNull("The OperationsPolicy is null", actual );
        assertEquals( expected, actual );
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }
    //
    // Test EJBContext allowed operations       
    //=====================================
}


