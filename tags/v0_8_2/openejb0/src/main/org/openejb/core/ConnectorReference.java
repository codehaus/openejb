package org.openejb.core;

import org.openejb.core.ivm.naming.Reference;
import javax.naming.NamingException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ManagedConnectionFactory;
/*
  This reference object is used for wrappering ManagedConnectionFactory objects that
  manufacture resource specific connection factories. When the getObject( ) method is 
  invoked the factory is created and passed back as the return value.
  
  In addition, dynamic resolution and special conditions can be encapsulated
  in the implementation object.
  
*/
public class ConnectorReference implements Reference{
    private ConnectionManager conMngr;
    private ManagedConnectionFactory mngedConFactory;
    
    public ConnectorReference(ConnectionManager cm, ManagedConnectionFactory mcf){
        conMngr = cm;
        mngedConFactory = mcf;
    }
    public Object getObject( ) throws NamingException{
        try{
        return mngedConFactory.createConnectionFactory(conMngr);
        }catch(javax.resource.ResourceException re){
            throw new javax.naming.NamingException("Could not create ConnectionFactory from "+mngedConFactory.getClass());
        }
        
    }
}