/**
 *
 * Copyright 2006 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.openejb.test.stateless;

import org.openejb.test.ApplicationException;
import org.openejb.test.object.OperationsPolicy;

import javax.ejb.SessionContext;
import javax.ejb.EJBException;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.util.Hashtable;
import java.util.Properties;
import java.rmi.RemoteException;

public class BasicStatelessPojoBean {

    /**
     * Maps to BasicStatelessObject.businessMethod
     *
     * @return
     * @see org.openejb.test.stateless.BasicStatelessObject#businessMethod
     */
    public String businessMethod(String text){
        StringBuffer b = new StringBuffer(text);
        return b.reverse().toString();
    }


    /**
     * Throws an ApplicationException when invoked
     *
     */
    public void throwApplicationException() throws ApplicationException {
        throw new ApplicationException("Testing ability to throw Application Exceptions");
    }

    /**
     * Throws a java.lang.NullPointerException when invoked
     * This is a system exception and should result in the
     * destruction of the instance and invalidation of the
     * remote reference.
     *
     */
    public void throwSystemException_NullPointer() {
        throw new NullPointerException("Testing ability to throw System Exceptions");
    }

    /**
     * Maps to BasicStatelessObject.getPermissionsReport
     *
     * Returns a report of the bean's
     * runtime permissions
     *
     * @return
     * @see org.openejb.test.stateless.BasicStatelessObject#getPermissionsReport
     */
    public Properties getPermissionsReport(){
        /* TO DO: */
        return null;
    }

    /**
     * Maps to BasicStatelessObject.getAllowedOperationsReport
     *
     * Returns a report of the allowed opperations
     * for one of the bean's methods.
     *
     * @param methodName The method for which to get the allowed opperations report
     * @return
     * @see org.openejb.test.stateless.BasicStatelessObject#getAllowedOperationsReport
     */
    public OperationsPolicy getAllowedOperationsReport(String methodName){
        return null;
    }

    public void setSessionContext(SessionContext ctx) throws EJBException, RemoteException {
    }
}
