package org.openejb.test.stateful;

import javax.ejb.EJBHome;

/**
 * [4] Should be run as the fourth test suite of the BasicStatefulTestClients
 *
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class StatefulEjbObjectTests extends BasicStatefulTestClient{

    public StatefulEjbObjectTests(){
        super("EJBObject.");
    }

    protected void setUp() throws Exception{
        super.setUp();
        Object obj = initialContext.lookup("client/tests/stateful/BasicStatefulHome");
        ejbHome = (BasicStatefulHome)javax.rmi.PortableRemoteObject.narrow( obj, BasicStatefulHome.class);
        ejbObject = ejbHome.create("Second Bean");
    }

    protected void tearDown() throws Exception {
        //ejbObject.remove();
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

    public void test02_isIdentical(){
        try{
            assertTrue( "The EJBObjects are not equal", ejbObject.isIdentical(ejbObject) );
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test03_getEjbHome(){
        try{
            EJBHome home = ejbObject.getEJBHome();
            assertNotNull( "The EJBHome is null", home );
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    /**
     * 5.5 Session object identity
     *
     * Session objects are intended to be private resources used only by the
     * client that created them. For this reason, session objects, from the
     * client�s perspective, appear anonymous. In contrast to entity objects,
     * which expose their identity as a primary key, session objects hide their
     * identity. As a result, the EJBObject.getPrimaryKey() and
     * EJBHome.remove(Object primaryKey) methods result in a java.rmi.RemoteException
     * if called on a session bean. If the EJBMetaData.getPrimaryKeyClass()
     * method is invoked on a EJBMetaData object for a Session bean, the method throws
     * the java.lang.RuntimeException.
     */
    public void test04_getPrimaryKey(){
        try{
            Object key = ejbObject.getPrimaryKey();
        } catch (java.rmi.RemoteException e){
            assertTrue(true);
            return;
        } catch (Exception e){
            fail("A RuntimeException should have been thrown.  Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
        fail("A RuntimeException should have been thrown.");
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
        }
    }
    //
    // Test ejb object methods
    //===============================


}
