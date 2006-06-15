package org.openejb.test.entity.bmp;


/**
 * [5] Should be run as the fifth test suite of the BasicBmpTestClients
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class BmpRemoteIntfcTests extends BasicBmpTestClient{

    public BmpRemoteIntfcTests(){
        super("RemoteIntfc.");
    }

    protected void setUp() throws Exception{
        super.setUp();
        Object obj = initialContext.lookup("client/tests/entity/bmp/BasicBmpHome");
        ejbHome = (BasicBmpHome)javax.rmi.PortableRemoteObject.narrow( obj, BasicBmpHome.class);
        ejbObject = ejbHome.create("Forth Bean");
    }
    
    //=================================
    // Test remote interface methods
    //
    public void test01_businessMethod(){
        try{
        String expected = "Success";
        String actual = ejbObject.businessMethod("sseccuS");
        assertEquals(expected, actual);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }
    /**
     * Throw an application exception and make sure the exception
     * reaches the bean nicely.
     */
    public void test02_throwApplicationException(){
        try{
            ejbObject.throwApplicationException();
        } catch (org.openejb.test.ApplicationException e){
            //Good.  This is the correct behaviour
            return;
        } catch (Throwable e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
        fail("An ApplicationException should have been thrown.");
    }
    
    /**
     * After an application exception we should still be able to 
     * use our bean
     */
    public void test03_invokeAfterApplicationException(){
        try{
        String expected = "Success";
        String actual   = ejbObject.businessMethod("sseccuS");
        assertEquals(expected, actual);
        } catch (Throwable e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }

    public void test04_throwSystemException(){
        try{
            ejbObject.throwSystemException_NullPointer();
        } catch (java.rmi.RemoteException e){
            //Good, so far.
            Throwable n = e.detail;
            assertNotNull("Nested exception should not be is null", n );
            assertTrue("Nested exception should be an instance of NullPointerException, but exception is "+n.getClass().getName(), (n instanceof NullPointerException));
            return;
        } catch (Throwable e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
        fail("A NullPointerException should have been thrown.");
    }
    
    /**
     * After a system exception the intance should be garbage collected
     * and the remote reference should be invalidated.
     * 
     * The Remote Server fails this one, that should be fixed.
     */
    public void BUG_test05_invokeAfterSystemException(){
        try{
        ejbObject.businessMethod("This refernce is invalid");
        fail("A java.rmi.NoSuchObjectException should have been thrown.");
        } catch (java.rmi.NoSuchObjectException e){
            // Good.
        } catch (Throwable e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }
    //
    // Test remote interface methods
    //=================================

    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
