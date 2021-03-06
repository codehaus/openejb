package org.openejb.test.stateless;


/**
 * [1] Should be run as the first test suite of the BasicStatelessTestClients
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class StatelessJndiTests extends BasicStatelessTestClient{

    public StatelessJndiTests(){
        super("JNDI.");
    }

    public void test01_initialContext(){
        try{
            assertNotNull("The InitialContext reference is null.", initialContext);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }
    
    public void test02_Jndi_lookupHome(){
        try{
            Object obj = initialContext.lookup("client/tests/stateless/BasicStatelessHome");
            ejbHome = (BasicStatelessHome)javax.rmi.PortableRemoteObject.narrow( obj, BasicStatelessHome.class);
            assertNotNull("The EJBHome is null", ejbHome);
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }
    

    /* TO DO:  
     * public void test00_enterpriseBeanAccess()       
     * public void test00_jndiAccessToJavaCompEnv()
     * public void test00_resourceManagerAccess()
     */

}
