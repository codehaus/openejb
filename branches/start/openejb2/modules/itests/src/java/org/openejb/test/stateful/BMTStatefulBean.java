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
import java.util.Hashtable;
import java.util.Properties;

import javax.ejb.EJBException;
import javax.ejb.SessionContext;

import org.openejb.test.ApplicationException;
import org.openejb.test.object.OperationsPolicy;

/**
 * A Stateful SessionBean with bean-managed transaction demarcation
 * 
 */
public class BMTStatefulBean implements javax.ejb.SessionBean {
    
    private String name;
    private SessionContext ejbContext;
    private Hashtable allowedOperationsTable = new Hashtable();
    
    
    //=============================
    // Home interface methods
    //    
    /**
     * Maps to BasicStatefulHome.create
     * 
     * @param name
     * @exception javax.ejb.CreateException
     * @see BasicStatefulHome.create
     */
    public void ejbCreate(String name)
    throws javax.ejb.CreateException{
        testAllowedOperations("ejbCreate");
        this.name = name;
    }
    
    //    
    // Home interface methods
    //=============================
    

    //=============================
    // Remote interface methods
    //    
    
    /**
     * Maps to BasicStatefulObject.businessMethod
     * 
     * @return 
     * @see BasicStatefulObject.businessMethod
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
    public void throwApplicationException() throws ApplicationException{
        throw new ApplicationException("Don't Panic");
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
     * Maps to BasicStatefulObject.getPermissionsReport
     * 
     * Returns a report of the bean's
     * runtime permissions
     * 
     * @return 
     * @see BasicStatefulObject.getPermissionsReport
     */
    public Properties getPermissionsReport(){
        /* TO DO: */
        return null;
    }
    
    /**
     * Maps to BasicStatefulObject.getAllowedOperationsReport
     * 
     * Returns a report of the allowed opperations
     * for one of the bean's methods.
     * 
     * @param methodName The method for which to get the allowed opperations report
     * @return 
     * @see BasicStatefulObject.getAllowedOperationsReport
     */
    public OperationsPolicy getAllowedOperationsReport(String methodName){
        return (OperationsPolicy) allowedOperationsTable.get(methodName);
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
        testAllowedOperations("setSessionContext");
    }
    /**
     * A container invokes this method before it ends the life of the session
     * object. This happens as a result of a client's invoking a remove
     * operation, or when a container decides to terminate the session object
     * after a timeout.
     */
    public void ejbRemove() throws EJBException,RemoteException {
        testAllowedOperations("ejbRemove");
    }
    /**
     * The activate method is called when the instance is activated
     * from its "passive" state. The instance should acquire any resource
     * that it has released earlier in the ejbPassivate() method.
     */
    public void ejbActivate() throws EJBException,RemoteException {
        testAllowedOperations("ejbActivate");
    }
    /**
     * The passivate method is called before the instance enters
     * the "passive" state. The instance should release any resources that
     * it can re-acquire later in the ejbActivate() method.
     */
    public void ejbPassivate() throws EJBException,RemoteException {
        testAllowedOperations("ejbPassivate");
    }
    //    
    // StatefulBean interface methods
    //==================================

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
        }catch(IllegalStateException ise){}
        
        /*[7] Test getEJBObject ///////////////*/ 
        try{
            ejbContext.getEJBObject();
            policy.allow( policy.Context_getEJBObject );
        }catch(IllegalStateException ise){}
         
        /* TO DO:  
         * Check for policy.Enterprise_bean_access       
         * Check for policy.JNDI_access_to_java_comp_env 
         * Check for policy.Resource_manager_access      
         */
        allowedOperationsTable.put(methodName, policy);
    }
}
