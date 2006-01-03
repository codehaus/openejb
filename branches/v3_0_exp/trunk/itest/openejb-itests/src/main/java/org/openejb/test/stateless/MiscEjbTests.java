package org.openejb.test.stateless;

import org.openejb.test.entity.bmp.EncBmpHome;
import org.openejb.test.entity.bmp.EncBmpObject;
import org.openejb.test.entity.cmp.EncCmpHome;
import org.openejb.test.entity.cmp.EncCmpObject;
import org.openejb.test.stateful.EncStatefulHome;
import org.openejb.test.stateful.EncStatefulObject;

/**
 *
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 */
public class MiscEjbTests extends BasicStatelessTestClient{

    public MiscEjbTests(){
        super("EJBObject.");
    }

    protected void setUp() throws Exception{
        super.setUp();
        Object obj = initialContext.lookup("client/tests/stateless/BasicStatelessHome");
        ejbHome = (BasicStatelessHome)javax.rmi.PortableRemoteObject.narrow( obj, BasicStatelessHome.class);
        ejbObject = ejbHome.create();
    }

    protected void tearDown() throws Exception {
        try {
            //ejbObject.remove();
        } catch (Exception e){
            throw e;
        } finally {
            super.tearDown();
        }
    }

    //===============================
    // Test ejb object methods
    //
    public void test01_isIdentical_stateless(){
        try{
            String jndiName = "client/tests/stateless/EncBean";
            EncStatelessHome ejbHome2 = null;
            EncStatelessObject ejbObject2 = null;

            Object obj = initialContext.lookup(jndiName);
            ejbHome2 = (EncStatelessHome)javax.rmi.PortableRemoteObject.narrow( obj, EncStatelessHome.class);
            ejbObject2 = ejbHome2.create();

            //System.out.println("_______________________________________________________");
            //System.out.println(" ejb1 "+ejbObject);
            //System.out.println(" ejb2 "+ejbObject2);
            assertTrue( "The EJBObjects should not be identical", !ejbObject.isIdentical(ejbObject2) );
            //System.out.println("-------------------------------------------------------");
        } catch (Exception e){
            //System.out.println("-------------------------------------------------------");
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test02_isIdentical_stateful(){
        try{
            String jndiName = "client/tests/stateful/EncBean";
            EncStatefulHome ejbHome2 = null;
            EncStatefulObject ejbObject2 = null;

            Object obj = initialContext.lookup(jndiName);
            ejbHome2 = (EncStatefulHome)javax.rmi.PortableRemoteObject.narrow( obj, EncStatefulHome.class);
            ejbObject2 = ejbHome2.create("isIdentical test");

            //System.out.println("_______________________________________________________");
            //System.out.println(" ejb1 "+ejbObject);
            //System.out.println(" ejb2 "+ejbObject2);
            assertTrue( "The EJBObjects should not be identical", !ejbObject.isIdentical(ejbObject2) );
            //System.out.println("-------------------------------------------------------");
        } catch (Exception e){
            //System.out.println("-------------------------------------------------------");
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test03_isIdentical_bmp(){
        try{
            String jndiName = "client/tests/entity/bmp/EncBean";
            EncBmpHome ejbHome2 = null;
            EncBmpObject ejbObject2 = null;

            Object obj = initialContext.lookup(jndiName);
            ejbHome2 = (EncBmpHome)javax.rmi.PortableRemoteObject.narrow( obj, EncBmpHome.class);
            ejbObject2 = ejbHome2.create("isIdentical test");

            //System.out.println("_______________________________________________________");
            assertTrue( "The EJBObjects should not be identical", !ejbObject.isIdentical(ejbObject2) );
            //System.out.println(" ejb1 "+ejbObject);
            //System.out.println(" ejb2 "+ejbObject2);
            //System.out.println("-------------------------------------------------------");
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    /**
     * DMB: Calling this now causes an error as the "entity" table doesn't exist yet
     */ 
    public void _test04_isIdentical_cmp(){
        try{
            String jndiName = "client/tests/entity/cmp/EncBean";
            EncCmpHome ejbHome2 = null;
            EncCmpObject ejbObject2 = null;

            Object obj = initialContext.lookup(jndiName);
            ejbHome2 = (EncCmpHome)javax.rmi.PortableRemoteObject.narrow( obj, EncCmpHome.class);
            ejbObject2 = ejbHome2.create("isIdentical test");

            //System.out.println("_______________________________________________________");
            //System.out.println(" ejb1 "+ejbObject);
            //System.out.println(" ejb2 "+ejbObject2);
            assertTrue( "The EJBObjects should not be identical", !ejbObject.isIdentical(ejbObject2) );
            //System.out.println("-------------------------------------------------------");
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    //
    // Test ejb object methods
    //===============================
}
