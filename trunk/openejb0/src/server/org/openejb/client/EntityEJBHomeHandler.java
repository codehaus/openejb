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
package org.openejb.client;

import java.lang.reflect.Method;
import java.rmi.RemoteException;

import javax.ejb.EJBObject;
import javax.ejb.Handle;

/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @since 11/25/2001
 */
public class EntityEJBHomeHandler extends EJBHomeHandler {
    
    public EntityEJBHomeHandler(){
    }
    
    public EntityEJBHomeHandler(EJBMetaDataImpl ejb, ServerMetaData server, ClientMetaData client){
        super(ejb, server, client);
    }
   
    /**
     * <P>
     * EJB 1.1 --
     * 9.1.8 Finder method return type
     * 
     * 9.1.8.1 Single-object finder
     * 
     * Some finder methods (such as ejbFindByPrimaryKey) are designed to return 
     * at most one entity object. For these single-object finders, the result type
     * of the find<METHOD>(...)method defined in the entity bean�s home interface 
     * is the entity bean�s remote interface. The result type of the corresponding
     * ejbFind<METHOD>(...) method defined in the entity�s implementation class is
     * the entity bean�s primary key type.
     * 
     * 9.1.8.2 Multi-object finders
     * 
     * Some finder methods are designed to return multiple entity objects. For
     * these multi-object finders, the result type of the find<METHOD>(...)method
     * defined in the entity bean�s home interface is a col-lection of objects
     * implementing the entity bean�s remote interface. The result type of the
     * corresponding ejbFind<METHOD>(...) implementation method defined in the
     * entity bean�s implementation class is a collection of objects of the entity
     * bean�s primary key type.
     * 
     * The Bean Provider can choose two types to define a collection type for a finder:
     * � the JDK� 1.1 java.util.Enumeration interface
     * � the Java� 2 java.util.Collection interface
     * 
     * A Bean Provider that wants to ensure that the entity bean is compatible
     * with containers and clients based on JDK TM 1.1 software must use the
     * java.util.Enumeration interface for the finder�s result type.
     * </P>
     * <P>
     * Locates and returns a new EJBObject or a collection
     * of EJBObjects.  The EJBObject(s) is a new proxy with
     * a new handler. This implementation should not be
     * sent outside the virtual machine.
     * </P>
     * <P>
     * This method propogates to the container
     * system.
     * </P>
     * <P>
     * The find method is required to be defined
     * by the bean's home interface of Entity beans.
     * </P>
     * 
     * @param method
     * @param args
     * @param proxy
     * @return Returns an new EJBObject proxy and handler
     * @exception Throwable
     */
    protected Object findX(Method method, Object[] args, Object proxy) throws Throwable {
        EJBRequest req = new EJBRequest( EJB_HOME_FIND ); 
        
        req.setMethodParameters( args );
        req.setMethodInstance( method );
        req.setClientIdentity( client.getClientIdentity() );
        req.setDeploymentCode( ejb.deploymentCode );
        req.setDeploymentId( ejb.deploymentID );
        req.setPrimaryKey( primaryKey );
        
        EJBResponse res = request( req );
        
        Object primKey = null;
        EJBObjectHandler handler = null;

        switch (res.getResponseCode()) {
        case EJB_ERROR:
            throw (Throwable)res.getResult();
        case EJB_SYS_EXCEPTION:
            throw (Throwable)res.getResult();
        case EJB_APP_EXCEPTION:
            throw (Throwable)res.getResult();
        
        case EJB_OK_FOUND:
            primKey = res.getResult();
            handler = EJBObjectHandler.createEJBObjectHandler(ejb,server,client,primKey);
            handler.setEJBHomeProxy((EJBHomeProxy)proxy);
            registerHandler(ejb.deploymentID+":"+primKey, handler);
            return handler.createEJBObjectProxy();
        
        case EJB_OK_FOUND_MULTIPLE:
            // The result is an array of primary keys
            // We are going to convert those primary keys into
            // EJBObject instance and reuse the same array to 
            // create the collection.
            Object[] primaryKeys = (Object[])res.getResult();

            for (int i=0; i < primaryKeys.length; i++){
                primKey = primaryKeys[i];
                handler = EJBObjectHandler.createEJBObjectHandler(ejb,server,client,primKey);
                handler.setEJBHomeProxy((EJBHomeProxy)proxy);
                registerHandler(ejb.deploymentID+":"+primKey, handler);
                primaryKeys[i] = handler.createEJBObjectProxy();
            }
            return java.util.Arrays.asList( primaryKeys );

        default:
            throw new RemoteException("Received invalid response code from server: "+res.getResponseCode());
        }
    }
    /**
     * <P>
     * Attempts to remove an EJBObject from the
     * container system.  The EJBObject to be removed
     * is represented by the primaryKey passed
     * into the remove method of the EJBHome.
     * </P>
     * <P>
     * This method propogates to the container system.
     * </P>
     * <P>
     * remove(Object primary) is a method of javax.ejb.EJBHome
     * </P>
     * <P>
     * Checks if the caller is authorized to invoke the
     * javax.ejb.EJBHome.remove on the EJBHome of the
     * deployment.
     * </P>
     *
     * @param method
     * @param args
     * @return Returns null
     * @exception Throwable
     * @see javax.ejb.EJBHome
     * @see javax.ejb.EJBHome#remove
     */
    protected Object removeByPrimaryKey(Method method, Object[] args, Object proxy) throws Throwable{
        
        Object primKey = args[0];
               
        if ( primKey == null ) throw new NullPointerException("The primary key is null.");
        //TODO: Send EJBRequest to server
            
        /* 
         * This operation takes care of invalidating all the EjbObjectProxyHanders 
         * associated with the same RegistryId. See this.createProxy().
         */
        invalidateAllHandlers(ejb.deploymentID+":"+primKey);
        return null;
    }
    /**
     * <P>
     * Attempts to remove an EJBObject from the
     * container system.  The EJBObject to be removed
     * is represented by the javax.ejb.Handle object passed
     * into the remove method in the EJBHome.
     * </P>
     * <P>
     * This method propogates to the container system.
     * </P>
     * <P>
     * remove(Handle handle) is a method of javax.ejb.EJBHome
     * </P>
     * <P>
     * Checks if the caller is authorized to invoke the
     * javax.ejb.EJBHome.remove on the EJBHome of the
     * deployment.
     * </P>
     * 
     * @param method
     * @param args
     * @return Returns null
     * @exception Throwable
     * @see javax.ejb.EJBHome
     * @see javax.ejb.EJBHome#remove
     */
    protected Object removeWithHandle(Method method, Object[] args, Object proxy) throws Throwable{
        
        if ( args[0] == null ) throw new RemoteException("Handler is null");

        //DMB: There is a better way to do this,
        //     but this will work for now.
        Handle handle = (Handle)args[0];
        EJBObject ejbObject = handle.getEJBObject();
        ejbObject.remove();
        return null;
    }
    
}
