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
package org.openejb.test.stateless;

import javax.ejb.*;
import java.util.Properties;
import javax.naming.InitialContext;
import org.openejb.test.TestFailureException;
import org.openejb.test.beans.Database;
import org.openejb.test.beans.DatabaseHome;

/**
 * [4] Should be run as the fourth test suite of the EncStatelessTestClients
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class StatelessJndiEncTests extends StatelessTestClient{

    protected EncStatelessHome   ejbHome;
    protected EncStatelessObject ejbObject;
    
    public static final String CREATE_ENTITY_TABLE = "CREATE TABLE BasicEntities ( EntityID INT PRIMARY KEY AUTO INCREMENT, FIRSTNAME CHAR(20), LASTNAME CHAR(20) )";
    public static final String DROP_ENTITY_TABLE = "DROP TABLE BasicEntities";
    
    private Database database;
    
    public StatelessJndiEncTests(){
        super("JNDI_ENC.");
    }

    protected void setUp() throws Exception{
        super.setUp();
        Object obj = initialContext.lookup("client/tests/stateless/EncBean");
        ejbHome = (EncStatelessHome)javax.rmi.PortableRemoteObject.narrow( obj, EncStatelessHome.class);
        ejbObject = ejbHome.create();
        
        obj = initialContext.lookup("client/tools/DatabaseHome");
        DatabaseHome databaseHome = (DatabaseHome)javax.rmi.PortableRemoteObject.narrow( obj, DatabaseHome.class);
        database = databaseHome.create();
        database.executeQuery(CREATE_ENTITY_TABLE);
    }
    
    /**
     * Tears down the fixture, for example, close a network connection.
     * This method is called after a test is executed.
     */
    protected void tearDown() throws Exception {
        try {
            //ejbObject.remove();
            database.executeQuery( DROP_ENTITY_TABLE);
        } catch (Exception e){
            throw e;
        } finally {
            super.tearDown();
        }
    }
    
    public void test01_lookupStringEntry() {
        try{
            ejbObject.lookupStringEntry();
        } catch (TestFailureException e){
            throw e.error;
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }
    
    public void test02_lookupDoubleEntry() { 
        try{
            ejbObject.lookupDoubleEntry();
        } catch (TestFailureException e){
            throw e.error;
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }
    
    public void test03_lookupLongEntry() {   
        try{
            ejbObject.lookupLongEntry();
        } catch (TestFailureException e){
            throw e.error;
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }
    
    public void test04_lookupFloatEntry() {  
        try{
            ejbObject.lookupFloatEntry();
        } catch (TestFailureException e){
            throw e.error;
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }
    
    public void test05_lookupIntegerEntry() {
        try{
            ejbObject.lookupIntegerEntry();
        } catch (TestFailureException e){
            throw e.error;
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }
    
    public void test06_lookupShortEntry() {  
        try{
            ejbObject.lookupShortEntry();
        } catch (TestFailureException e){
            throw e.error;
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }
    
    public void test07_lookupBooleanEntry() {
        try{
            ejbObject.lookupBooleanEntry();
        } catch (TestFailureException e){
            throw e.error;
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }
    
    public void test08_lookupByteEntry() {   
        try{
            ejbObject.lookupByteEntry();
        } catch (TestFailureException e){
            throw e.error;
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }
    
    public void test09_lookupEntityBean() {  
        try{
            ejbObject.lookupEntityBean();
        } catch (TestFailureException e){
            throw e.error;
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }
    
    public void test10_lookupStatefulBean() {
        try{
            ejbObject.lookupStatefulBean();
        } catch (TestFailureException e){
            throw e.error;
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }
    
    public void test11_lookupStatelessBean() {
        try{
            ejbObject.lookupStatelessBean();
        } catch (TestFailureException e){
            throw e.error;
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }
    
    public void test12_lookupResource() {
        try{
            ejbObject.lookupResource();
        } catch (TestFailureException e){
            throw e.error;
        } catch (Exception e){
            fail("Received Exception "+e.getClass()+ " : "+e.getMessage());
        }
    }
    
}
