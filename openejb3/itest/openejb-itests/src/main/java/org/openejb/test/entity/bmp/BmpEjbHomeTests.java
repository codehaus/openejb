package org.openejb.test.entity.bmp;

import javax.ejb.EJBMetaData;

/**
 * [3] Should be run as the third test suite of the BasicBmpTestClients
 *
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class BmpEjbHomeTests extends BasicBmpTestClient{

    public BmpEjbHomeTests(){
        super("EJBHome.");
    }

    protected void setUp() throws Exception{
        super.setUp();
        Object obj = initialContext.lookup("client/tests/entity/bmp/BasicBmpHome");
        ejbHome = (BasicBmpHome)javax.rmi.PortableRemoteObject.narrow( obj, BasicBmpHome.class);
        ejbObject = ejbHome.create("Second Bean");
        ejbPrimaryKey = ejbObject.getPrimaryKey();
    }

    //===============================
    // Test ejb home methods
    //
    public void test01_getEJBMetaData(){
        try{
        EJBMetaData ejbMetaData = ejbHome.getEJBMetaData();
        assertNotNull( "The EJBMetaData is null", ejbMetaData );
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test02_getHomeHandle(){
        try{
            ejbHomeHandle = ejbHome.getHomeHandle();
            assertNotNull( "The HomeHandle is null", ejbHomeHandle );
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test03_remove(){
        try{
            ejbHome.remove(ejbPrimaryKey);
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
    // Test ejb home methods
    //===============================
}
