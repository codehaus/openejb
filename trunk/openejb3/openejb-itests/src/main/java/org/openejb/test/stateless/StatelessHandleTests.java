package org.openejb.test.stateless;

import javax.ejb.EJBObject;

/**
 * [7] Should be run as the seventh test suite of the BasicStatelessTestClients
 *
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class StatelessHandleTests extends BasicStatelessTestClient{

    public StatelessHandleTests(){
        super("Handle.");
    }

    protected void setUp() throws Exception{
        super.setUp();
        Object obj = initialContext.lookup("client/tests/stateless/BasicStatelessHome");
        ejbHome = (BasicStatelessHome)javax.rmi.PortableRemoteObject.narrow( obj, BasicStatelessHome.class);
        ejbObject = ejbHome.create();
        ejbHandle = ejbObject.getHandle();
    }

    protected void tearDown() throws Exception{
        try {
            //ejbObject.remove();
        } catch (Exception e){
            throw e;
        } finally {
            super.tearDown();
        }
    }

    //=================================
    // Test handle methods
    //
    public void test01_getEJBObject(){

        try{
            EJBObject object = ejbHandle.getEJBObject();
            assertNotNull( "The EJBObject is null", object );
            // Wait until isIdentical is working.
            //assertTrue("EJBObjects are not identical", object.isIdentical(ejbObject));
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    /**
     * <B>5.6 Client view of session object�s life cycle</B>
     * <P>
     * ....It is invalid to reference a session object that does
     * not exist. Attempted invocations on a session object
     * that does not exist result in java.rmi.NoSuchObjectException.
     * </P>
     *
     * <P>
     * This remove method of the EJBHome is placed hear as it
     * is more a test on the handle then on the remove method
     * itself.
     * </P>
     */
    public void test02_EJBHome_remove(){
        try{
            ejbHome.remove(ejbHandle);
            try{
                ejbObject.businessMethod("Should throw an exception");
                assertTrue( "Calling business method after removing the EJBObject does not throw an exception", false );
            } catch (Exception e){
                assertTrue( true );
                return;
            }
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }
    //
    // Test handle methods
    //=================================

}
