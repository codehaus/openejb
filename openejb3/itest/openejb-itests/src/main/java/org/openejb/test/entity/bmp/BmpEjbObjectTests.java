package org.openejb.test.entity.bmp;

import javax.ejb.EJBHome;

/**
 * [4] Should be run as the fourth test suite of the BasicBmpTestClients
 *
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class BmpEjbObjectTests extends BasicBmpTestClient{

    public BmpEjbObjectTests(){
        super("EJBObject.");
    }

    protected void setUp() throws Exception{
        super.setUp();
        Object obj = initialContext.lookup("client/tests/entity/bmp/BasicBmpHome");
        ejbHome = (BasicBmpHome)javax.rmi.PortableRemoteObject.narrow( obj, BasicBmpHome.class);
        ejbObject = ejbHome.create("Third Bean");
    }

    protected void tearDown() throws Exception {
        if(ejbObject!=null){// set to null by test05_remove() method
            try{
            ejbObject.remove();
            }catch(Exception e){
                throw e;
            }
        }
        super.tearDown();
    }

    //===============================
    // Test ejb object methods
    //
    public void test01_getHandle(){
        try{
            ejbHandle = ejbObject.getHandle();
            assertNotNull( "The Handle is null", ejbHandle );
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test02_getPrimaryKey(){
        try{
            ejbPrimaryKey = (Integer)ejbObject.getPrimaryKey();
            assertNotNull( "The primary key is null", ejbPrimaryKey );
        } catch (Exception e){
            e.printStackTrace();
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test03_isIdentical(){
        try{
            assertTrue("The EJBObjects are not identical", ejbObject.isIdentical(ejbObject) );
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test04_getEjbHome(){
        try{
            EJBHome home = ejbObject.getEJBHome();
            assertNotNull( "The EJBHome is null", home );
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test05_remove(){
        try{
            ejbObject.remove();
            try{
                ejbObject.businessMethod("Should throw an exception");
                assertTrue( "Calling business method after removing the EJBObject does not throw an exception", false );
            } catch (Exception e){
                assertTrue( true );
                return;
            }
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        } finally{
            ejbObject = null;
        }
    }
    //
    // Test ejb object methods
    //===============================


}
