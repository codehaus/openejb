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
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact openejb-group@openejb.sf.net.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.sf.net/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE OPENEJB GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE OPENEJB GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2001 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.server.ejbd;


import java.rmi.RemoteException;
import java.util.Collection;

import org.openejb.ProxyInfo;
import org.openejb.RpcContainer;
import org.openejb.client.EJBRequest;
import org.openejb.client.EJBResponse;


/**
 * Processes the EjbHome_FIND method
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 */
public class EjbHome_FIND extends EjbMethod {

    /**
     * EJB 1.1 --
     * 9.1.8 Finder method return type
     *
     * 9.1.8.1 Single-object finder
     *
     * Some finder methods (such as ejbFindByPrimaryKey) are designed to return
     * at most one entity object. For these single-object finders, the result type
     * of the find<METHOD>(...)method defined in the entity bean’s home interface
     * is the entity bean’s remote interface. The result type of the corresponding
     * ejbFind<METHOD>(...) method defined in the entity’s implementation class is
     * the entity bean’s primary key type.
     *
     * 9.1.8.2 Multi-object finders
     *
     * Some finder methods are designed to return multiple entity objects. For
     * these multi-object finders, the result type of the find<METHOD>(...)method
     * defined in the entity bean’s home interface is a col-lection of objects
     * implementing the entity bean’s remote interface. The result type of the
     * corresponding ejbFind<METHOD>(...) implementation method defined in the
     * entity bean’s implementation class is a collection of objects of the entity
     * bean’s primary key type.
     *
     * The Bean Provider can choose two types to define a collection type for a finder:
     * • the JDK™ 1.1 java.util.Enumeration interface
     * • the Java™ 2 java.util.Collection interface
     *
     * A Bean Provider that wants to ensure that the entity bean is compatible
     * with containers and clients based on JDK TM 1.1 software must use the
     * java.util.Enumeration interface for the finder’s result type.
     * </P>
     *
     * @param req
     * @param in
     * @param out
     * @exception Exception
     */
    public void invoke(EJBRequest req, EJBResponse res) throws Exception{

        CallContext call = CallContext.getCallContext();
        RpcContainer c   = (RpcContainer)call.getDeploymentInfo().getContainer();

        Object result = c.invoke( req.getDeploymentId(),
                                  req.getMethodInstance(),
                                  req.getMethodParameters(),
                                  req.getPrimaryKey(),
                                  req.getClientIdentity());


        /* Multiple instances found */
        if ( result instanceof Collection ) {

            Object [] primaryKeys = ((Collection)result).toArray();

            for (int i=0; i < primaryKeys.length; i++){
                primaryKeys[i] = ((ProxyInfo)primaryKeys[i]).getPrimaryKey();
            }

            res.setResponse( EJB_OK_FOUND_MULTIPLE , primaryKeys );

        /* Single intance found */
        } else if (result instanceof ProxyInfo) {
            result = ((ProxyInfo)result).getPrimaryKey();
            res.setResponse( EJB_OK_FOUND , result );

        } else {
            // There should be no else, the entity should be found
            // or and exception should be thrown.
            //TODO:3: Localize all error messages in an separate file.
            result = new RemoteException("The bean is not EJB compliant.  The should be found or and exception should be thrown.");
            logger.error( req + "The bean is not EJB compliant.  The should be found or and exception should be thrown.");
            res.setResponse( EJB_SYS_EXCEPTION, result);
        }
    }
}
