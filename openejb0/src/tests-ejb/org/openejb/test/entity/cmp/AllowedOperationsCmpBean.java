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
package org.openejb.test.entity.cmp;

import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.ejb.EJBException;
import javax.ejb.EntityContext;
import javax.ejb.RemoveException;

import org.openejb.test.object.OperationsPolicy;

/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class AllowedOperationsCmpBean implements javax.ejb.EntityBean{
    
    public static int key = 20;
    
    public int primaryKey;
    public String firstName;
    public String lastName;
    public EntityContext ejbContext;
    public Hashtable allowedOperationsTable = new Hashtable();
    
    
    //=============================
    // Home interface methods
    //    
    
    /**
     * Maps to BasicCmpHome.sum
     * 
     * Adds x and y and returns the result.
     * 
     * @param one
     * @param two
     * @return x + y
     * @see BasicCmpHome.sum
     */
    public int ejbHomeSum(int x, int y) {
        testAllowedOperations("ejbHome");
        return x+y;
    }
    
    /**
     * Maps to BasicCmpHome.create
     * 
     * @param name
     * @return 
     * @exception javax.ejb.CreateException
     * @see BasicCmpHome.create
     */
    public Integer ejbCreate(String name)
    throws javax.ejb.CreateException{
        testAllowedOperations("ejbCreate");
        StringTokenizer st = new StringTokenizer(name, " ");    
        firstName = st.nextToken();
        lastName = st.nextToken();
        this.primaryKey = key++;
        return new Integer(primaryKey);
    }
    
    public void ejbPostCreate(String name)
    throws javax.ejb.CreateException{
    }

    //    
    // Home interface methods
    //=============================
    

    //=============================
    // Remote interface methods
    //    
    
    /**
     * Maps to BasicCmpObject.businessMethod
     * 
     * @return 
     * @see BasicCmpObject.businessMethod
     */
    public String businessMethod(String text){
        testAllowedOperations("businessMethod");
        StringBuffer b = new StringBuffer(text);
        return b.reverse().toString();
    }

    /**
     * Throws an ApplicationException when invoked
     * 
     */
    public void throwApplicationException() throws org.openejb.test.ApplicationException{
        throw new org.openejb.test.ApplicationException("Don't Panic");
    }
    
    /**
     * Throws a java.lang.NullPointerException when invoked
     * This is a system exception and should result in the 
     * destruction of the instance and invalidation of the
     * remote reference.
     * 
     */
    public void throwSystemException_NullPointer() {
        throw new NullPointerException("Panic");
    }
    
    
    /**
     * Maps to BasicCmpObject.getPermissionsReport
     * 
     * Returns a report of the bean's
     * runtime permissions
     * 
     * @return 
     * @see BasicCmpObject.getPermissionsReport
     */
    public Properties getPermissionsReport(){
        /* TO DO: */
        return null;
    }
    
    /**
     * Maps to BasicCmpObject.getAllowedOperationsReport
     * 
     * Returns a report of the allowed opperations
     * for one of the bean's methods.
     * 
     * @param methodName The method for which to get the allowed opperations report
     * @return 
     * @see BasicCmpObject.getAllowedOperationsReport
     */
    public OperationsPolicy getAllowedOperationsReport(String methodName){
        return (OperationsPolicy) allowedOperationsTable.get(methodName);
    }
    
    //    
    // Remote interface methods
    //=============================


    //================================
    // EntityBean interface methods
    //    
    
    /**
     * A container invokes this method to instruct the
     * instance to synchronize its state by loading it state from the
     * underlying database.
     */
    public void ejbLoad() throws EJBException,RemoteException {
        testAllowedOperations("ejbLoad");
    }
    
    /**
     * Set the associated entity context. The container invokes this method
     * on an instance after the instance has been created.
     */
    public void setEntityContext(EntityContext ctx) throws EJBException,RemoteException {
        ejbContext = ctx;
        testAllowedOperations("setEntityContext");
    }
    
    /**
     * Unset the associated entity context. The container calls this method
     * before removing the instance.
     */
    public void unsetEntityContext() throws EJBException,RemoteException {
        testAllowedOperations("unsetEntityContext");
    }
    
    /**
     * A container invokes this method to instruct the
     * instance to synchronize its state by storing it to the underlying
     * database.
     */
    public void ejbStore() throws EJBException,RemoteException {
        testAllowedOperations("ejbStore");
    }
    
    /**
     * A container invokes this method before it removes the EJB object
     * that is currently associated with the instance. This method
     * is invoked when a client invokes a remove operation on the
     * enterprise Bean's home interface or the EJB object's remote interface.
     * This method transitions the instance from the ready state to the pool
     * of available instances.
     */
    public void ejbRemove() throws RemoveException,EJBException,RemoteException {
        testAllowedOperations("ejbRemove");
    }
    
    /**
     * A container invokes this method when the instance
     * is taken out of the pool of available instances to become associated
     * with a specific EJB object. This method transitions the instance to
     * the ready state.
     */
    public void ejbActivate() throws EJBException,RemoteException {
        testAllowedOperations("ejbActivate");
    }
    
    /**
     * A container invokes this method on an instance before the instance
     * becomes disassociated with a specific EJB object. After this method
     * completes, the container will place the instance into the pool of
     * available instances.
     */
    public void ejbPassivate() throws EJBException,RemoteException {

        testAllowedOperations("ejbPassivate");
    }
    //    
    // EntityBean interface methods
    //================================
    
    protected void testAllowedOperations(String methodName){
        OperationsPolicy policy = new OperationsPolicy();
        
        /*[1] Test getEJBHome /////////////////*/ 
        try{
            ejbContext.getEJBHome();
            policy.allow(policy.Context_getEJBHome);
        }catch(IllegalStateException ise){}
        
        /*[2] Test getCallerPrincipal /////////*/ 
        try{
            ejbContext.getCallerPrincipal();
            policy.allow( policy.Context_getCallerPrincipal );
        }catch(IllegalStateException ise){}
        
        /*[3] Test isCallerInRole /////////////*/ 
        try{
            ejbContext.isCallerInRole("ROLE");
            policy.allow( policy.Context_isCallerInRole );
        }catch(IllegalStateException ise){}
        
        /*[4] Test getRollbackOnly ////////////*/ 
        try{
            ejbContext.getRollbackOnly();
            policy.allow( policy.Context_getRollbackOnly );
        }catch(IllegalStateException ise){}
        
        /*[5] Test setRollbackOnly ////////////*/ 
        try{
            ejbContext.setRollbackOnly();
            policy.allow( policy.Context_setRollbackOnly );
        }catch(IllegalStateException ise){}
        
        /*[6] Test getUserTransaction /////////*/ 
        try{
            ejbContext.getUserTransaction();
            policy.allow( policy.Context_getUserTransaction );
        }catch(Exception e){}
        
        /*[7] Test getEJBObject ///////////////*/ 
        try{
            ejbContext.getEJBObject();
            policy.allow( policy.Context_getEJBObject );
        }catch(IllegalStateException ise){}

        /*[8] Test getPrimaryKey //////////////*/ 
        try{
            ejbContext.getPrimaryKey();
            policy.allow( policy.Context_getPrimaryKey );
        }catch(IllegalStateException ise){}
         
        /* TO DO:  
         * Check for policy.Enterprise_bean_access       
         * Check for policy.JNDI_access_to_java_comp_env 
         * Check for policy.Resource_manager_access      
         */
        allowedOperationsTable.put(methodName, policy);
    }

}