package org.openejb.test.stateless;

import java.util.Properties;

import javax.ejb.EJBMetaData;
import javax.ejb.Handle;
import javax.ejb.HomeHandle;
import javax.naming.InitialContext;

import org.openejb.test.TestManager;

/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public abstract class StatelessTestClient extends org.openejb.test.NamedTestCase{
    
    protected InitialContext initialContext;

    protected BasicStatelessHome   ejbHome;
    protected BasicStatelessObject ejbObject;
    protected EJBMetaData       ejbMetaData;
    protected HomeHandle        ejbHomeHandle;
    protected Handle            ejbHandle;
    protected Integer           ejbPrimaryKey;

    public StatelessTestClient(String name){
        super("Stateless."+name);
    }
    
    /**
     * Sets up the fixture, for example, open a network connection.
     * This method is called before a test is executed.
     */
    protected void setUp() throws Exception {
        
        Properties properties = TestManager.getServer().getContextEnvironment();
        //properties.put(Context.SECURITY_PRINCIPAL, "STATELESS_test00_CLIENT");
        //properties.put(Context.SECURITY_CREDENTIALS, toString() );
        
        initialContext = new InitialContext(properties);
    }
    
}
