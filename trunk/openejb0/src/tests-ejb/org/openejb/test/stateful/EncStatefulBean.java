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
package org.openejb.test.stateful;

import java.rmi.RemoteException;

import javax.ejb.EJBException;
import javax.ejb.SessionContext;
import javax.ejb.SessionSynchronization;
import javax.naming.InitialContext;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;

import org.openejb.test.TestFailureException;
import org.openejb.test.entity.bmp.BasicBmpHome;
import org.openejb.test.entity.bmp.BasicBmpObject;
import org.openejb.test.stateless.BasicStatelessHome;
import org.openejb.test.stateless.BasicStatelessObject;

/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class EncStatefulBean implements javax.ejb.SessionBean, SessionSynchronization {

    
    private String name;
    private SessionContext ejbContext;
    
    
    //=============================
    // Home interface methods
    //    
    /**
     * Maps to EncStatefulHome.create
     * 
     * @param name
     * @exception javax.ejb.CreateException
     * @see EncStatefulHome.create
     */
    public void ejbCreate(String name)
    throws javax.ejb.CreateException{
        this.name = name;
    }
    //    
    // Home interface methods
    //=============================
    

    //=============================
    // Remote interface methods
    //    
    
    public void lookupEntityBean() throws TestFailureException{
        try{
            try{
            InitialContext ctx = new InitialContext();
            Assert.assertNotNull("The InitialContext is null", ctx );
            
            BasicBmpHome home = (BasicBmpHome) javax.rmi.PortableRemoteObject.narrow( ctx.lookup("java:comp/env/stateful/beanReferences/bmp_entity"), BasicBmpHome.class );
            Assert.assertNotNull("The EJBHome looked up is null",home);

            BasicBmpObject object = home.create("Enc Bean");
            Assert.assertNotNull("The EJBObject is null", object );
            } catch (Exception e){
                Assert.fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
            }
        } catch (AssertionFailedError afe){
            throw new TestFailureException(afe);
        }
    }
    
    public void lookupStatefulBean() throws TestFailureException{
        try{
            try{
            InitialContext ctx = new InitialContext();
            Assert.assertNotNull("The InitialContext is null", ctx );
            
            BasicStatefulHome home = (BasicStatefulHome) javax.rmi.PortableRemoteObject.narrow( ctx.lookup("java:comp/env/stateful/beanReferences/stateful"), BasicStatefulHome.class );
            Assert.assertNotNull("The EJBHome looked up is null",home);

            BasicStatefulObject object = home.create("Enc Bean");
            Assert.assertNotNull("The EJBObject is null", object );
            } catch (Exception e){
                Assert.fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
            }
        } catch (AssertionFailedError afe){
            throw new TestFailureException(afe);
        }
    }
    
    public void lookupStatelessBean() throws TestFailureException{
        try{
            try{
            InitialContext ctx = new InitialContext();
            Assert.assertNotNull("The InitialContext is null", ctx );
            
            BasicStatelessHome home = (BasicStatelessHome) javax.rmi.PortableRemoteObject.narrow( ctx.lookup("java:comp/env/stateful/beanReferences/stateless"), BasicStatelessHome.class );
            Assert.assertNotNull("The EJBHome looked up is null",home);

            BasicStatelessObject object = home.create();
            Assert.assertNotNull("The EJBObject is null", object );
            } catch (Exception e){
                Assert.fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
            }
        } catch (AssertionFailedError afe){
            throw new TestFailureException(afe);
        }
    }

    public void lookupStringEntry() throws TestFailureException{
        try{
            try{
            InitialContext ctx = new InitialContext();
            Assert.assertNotNull("The InitialContext is null", ctx );
            
            String expected = new String("1");
            String actual   = (String)ctx.lookup("java:comp/env/stateful/references/String");
            
            Assert.assertNotNull("The String looked up is null", actual );
            Assert.assertEquals(expected, actual );

            } catch (Exception e){
                Assert.fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
            }
        } catch (AssertionFailedError afe){
            throw new TestFailureException(afe);
        }
    }
    
    public void lookupDoubleEntry() throws TestFailureException{
        try{
            try{
            InitialContext ctx = new InitialContext();
            Assert.assertNotNull("The InitialContext is null", ctx );
            
            Double expected = new Double(1.0D);
            Double actual   = (Double)ctx.lookup("java:comp/env/stateful/references/Double");
            
            Assert.assertNotNull("The Double looked up is null", actual );
            Assert.assertEquals(expected, actual );

            } catch (Exception e){
                Assert.fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
            }
        } catch (AssertionFailedError afe){
            throw new TestFailureException(afe);
        }
    }
    
    public void lookupLongEntry() throws TestFailureException{
        try{
            try{
            InitialContext ctx = new InitialContext();
            Assert.assertNotNull("The InitialContext is null", ctx );
            
            Long expected = new Long(1L);
            Long actual   = (Long)ctx.lookup("java:comp/env/stateful/references/Long");
            
            Assert.assertNotNull("The Long looked up is null", actual );
            Assert.assertEquals(expected, actual );

            } catch (Exception e){
                Assert.fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
            }
        } catch (AssertionFailedError afe){
            throw new TestFailureException(afe);
        }
    }
    
    public void lookupFloatEntry() throws TestFailureException{
        try{
            try{
            InitialContext ctx = new InitialContext();
            Assert.assertNotNull("The InitialContext is null", ctx );
            
            Float expected = new Float(1.0F);
            Float actual   = (Float)ctx.lookup("java:comp/env/stateful/references/Float");
            
            Assert.assertNotNull("The Float looked up is null", actual );
            Assert.assertEquals(expected, actual );

            } catch (Exception e){
                Assert.fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
            }
        } catch (AssertionFailedError afe){
            throw new TestFailureException(afe);
        }
    }
    
    public void lookupIntegerEntry() throws TestFailureException{
        try{
            try{
            InitialContext ctx = new InitialContext();
            Assert.assertNotNull("The InitialContext is null", ctx );
            
            Integer expected = new Integer(1);
            Integer actual   = (Integer)ctx.lookup("java:comp/env/stateful/references/Integer");
            
            Assert.assertNotNull("The Integer looked up is null", actual );
            Assert.assertEquals(expected, actual );

            } catch (Exception e){
                Assert.fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
            }
        } catch (AssertionFailedError afe){
            throw new TestFailureException(afe);
        }
    }
    
    public void lookupShortEntry() throws TestFailureException{
        try{
            try{
            InitialContext ctx = new InitialContext();
            Assert.assertNotNull("The InitialContext is null", ctx );
            
            Short expected = new Short((short)1);
            Short actual   = (Short)ctx.lookup("java:comp/env/stateful/references/Short");
            
            Assert.assertNotNull("The Short looked up is null", actual );
            Assert.assertEquals(expected, actual );

            } catch (Exception e){
                Assert.fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
            }
        } catch (AssertionFailedError afe){
            throw new TestFailureException(afe);
        }
    }
    
    public void lookupBooleanEntry() throws TestFailureException{
        try{
            try{
            InitialContext ctx = new InitialContext();
            Assert.assertNotNull("The InitialContext is null", ctx );
            
            Boolean expected = new Boolean(true);
            Boolean actual = (Boolean)ctx.lookup("java:comp/env/stateful/references/Boolean");
            
            Assert.assertNotNull("The Boolean looked up is null", actual );
            Assert.assertEquals(expected, actual );

            } catch (Exception e){
                Assert.fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
            }
        } catch (AssertionFailedError afe){
            throw new TestFailureException(afe);
        }
    }
    
    public void lookupByteEntry() throws TestFailureException{
        try{
            try{
            InitialContext ctx = new InitialContext();
            Assert.assertNotNull("The InitialContext is null", ctx );
            
            Byte expected = new Byte((byte)1);
            Byte actual   = (Byte)ctx.lookup("java:comp/env/stateful/references/Byte");
            
            Assert.assertNotNull("The Byte looked up is null", actual );
            Assert.assertEquals(expected, actual );

            } catch (Exception e){
                Assert.fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
            }
        } catch (AssertionFailedError afe){
            throw new TestFailureException(afe);
        }
    }

    public void lookupResource() throws TestFailureException{
        try{
            try{
            InitialContext ctx = new InitialContext();
            Assert.assertNotNull("The InitialContext is null", ctx );
            } catch (Exception e){
                Assert.fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
            }
        } catch (AssertionFailedError afe){
            throw new TestFailureException(afe);
        }
    }
    //    
    // Remote interface methods
    //=============================


    //=================================
    // SessionBean interface methods
    //    
    /**
     * Set the associated session context. The container calls this method
     * after the instance creation.
     */
    public void setSessionContext(SessionContext ctx) throws EJBException,RemoteException {
        ejbContext = ctx;
    }
    /**
     * A container invokes this method before it ends the life of the session
     * object. This happens as a result of a client's invoking a remove
     * operation, or when a container decides to terminate the session object
     * after a timeout.
     */
    public void ejbRemove() throws EJBException,RemoteException {
    }
    /**
     * The activate method is called when the instance is activated
     * from its "passive" state. The instance should acquire any resource
     * that it has released earlier in the ejbPassivate() method.
     */
    public void ejbActivate() throws EJBException,RemoteException {
    }
    /**
     * The passivate method is called before the instance enters
     * the "passive" state. The instance should release any resources that
     * it can re-acquire later in the ejbActivate() method.
     */
    public void ejbPassivate() throws EJBException,RemoteException {
    }
    //    
    // SessionBean interface methods
    //==================================
    
    
    //============================================
    // SessionSynchronization interface methods
    //
    /**
     * The afterBegin method notifies a session Bean instance that a new
     * transaction has started, and that the subsequent business methods on the
     * instance will be invoked in the context of the transaction.
     */
    public void afterBegin() throws EJBException,RemoteException {
    }
    /**
     * The beforeCompletion method notifies a session Bean instance that
     * a transaction is about to be committed. The instance can use this
     * method, for example, to write any cached data to a database.
     */
    public void beforeCompletion() throws EJBException,RemoteException {
    }
    /**
     * The afterCompletion method notifies a session Bean instance that a
     * transaction commit protocol has completed, and tells the instance
     * whether the transaction has been committed or rolled back.
     */
    public void afterCompletion(boolean committed) throws EJBException,RemoteException {
    }
    //    
    // SessionSynchronization interface methods
    //============================================

}
