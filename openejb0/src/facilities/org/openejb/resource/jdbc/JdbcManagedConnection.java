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
package org.openejb.resource.jdbc;

import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionRequestInfo;
import java.util.HashSet;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionMetaData;
import java.util.Set;

public class JdbcManagedConnection implements ManagedConnection {
    
    private JdbcManagedConnectionFactory managedFactory;
    private java.sql.Connection sqlConn;
    private JdbcConnectionRequestInfo requestInfo;
    private JdbcManagedConnectionMetaData metaData;
    
    // there may be many conneciton handles active at any one time
    private java.util.Vector jdbcConnections = new java.util.Vector();
    private Set listeners;
    private java.io.PrintWriter logWriter;
    private JdbcLocalTransaction localTransaction;    
    
    public JdbcManagedConnection(JdbcManagedConnectionFactory managedFactory, java.sql.Connection sqlConn, JdbcConnectionRequestInfo rxInfo)
    throws javax.resource.spi.ResourceAdapterInternalException {
        listeners = java.util.Collections.synchronizedSet(new HashSet());
        this.managedFactory = managedFactory;
        this.requestInfo = rxInfo;
        this.sqlConn = sqlConn;
        logWriter = managedFactory.getLogWriter();
        try{ 
        metaData = new JdbcManagedConnectionMetaData(sqlConn.getMetaData());
        }catch(java.sql.SQLException sqlE){
            throw new javax.resource.spi.ResourceAdapterInternalException("Problem while attempting to access meta data from physical connection", ErrorCode.JDBC_0004);  
        }
        localTransaction = new JdbcLocalTransaction(this);
    }
    
    protected java.sql.Connection getSQLConnection(){
        return sqlConn;
    }
    
    protected JdbcConnectionRequestInfo getRequestInfo(){
        return requestInfo;
    }
    
    public void addConnectionEventListener(ConnectionEventListener listener)  {
        listeners.add(listener);
    }
    
    public void associateConnection(java.lang.Object connection)  throws javax.resource.ResourceException {
        if(connection instanceof JdbcConnection){
            JdbcConnection jdbcConn = (JdbcConnection)connection;
            jdbcConn.associate(this);
        }else{
            throw new javax.resource.ResourceException("Connection object is the wrong type. It must be an instance of JdbcConnection");
        }
    }
    
    /**
    * This method will invalidate any JdbcConnection handles that have not already been invalidated (they self invalidate when they are explicitly closed).
    */
    public void cleanup()  throws javax.resource.ResourceException {
        Object [] connectionHandles = jdbcConnections.toArray();
        for(int i = 0; i < connectionHandles.length; i++){
            JdbcConnection handle = (JdbcConnection)connectionHandles[i];
            handle.invalidate();
        }
        jdbcConnections.clear();
        localTransaction.cleanup();
    }
    
    public void destroy()  throws javax.resource.ResourceException {
        cleanup();
        try{
        sqlConn.close();
        }catch(java.sql.SQLException sqlE){
            throw new javax.resource.spi.ResourceAdapterInternalException("Problem attempting to close physical JDBC connection", ErrorCode.JDBC_0003);
        }
        managedFactory = null;
        sqlConn = null;
        listeners.clear();
    } 
    
    /*
    * Returns an application level connection handle in the form of a JdbcConnection object
    * which implements the java.sql.Connection interface and wrappers the physical JDBC connection.
    *
    */
    public java.lang.Object getConnection(javax.security.auth.Subject subject,ConnectionRequestInfo cxRequestInfo)  throws javax.resource.ResourceException  {
        JdbcConnection jdbcCon = new JdbcConnection(this,sqlConn);
        jdbcConnections.add(jdbcCon);
        return jdbcCon;
    }
    
    public javax.resource.spi.LocalTransaction getLocalTransaction()  throws javax.resource.ResourceException  {
        return localTransaction;
    }
    
    public java.io.PrintWriter getLogWriter()  throws javax.resource.ResourceException  {
        return logWriter;
    }
    
    public ManagedConnectionMetaData getMetaData()  throws javax.resource.ResourceException  {
        return metaData;
    }
    
    public javax.transaction.xa.XAResource getXAResource()  throws javax.resource.ResourceException  {
        throw new javax.resource.NotSupportedException("Method not implemented");
    }
    
    public void removeConnectionEventListener(ConnectionEventListener listener)    {
        listeners.remove(listener);
    }
    
    public void setLogWriter(java.io.PrintWriter out)  throws javax.resource.ResourceException {
        logWriter = out;
    }
    
    protected void localTransactionCommitted(){
        ConnectionEvent event = new ConnectionEvent(this, ConnectionEvent.LOCAL_TRANSACTION_COMMITTED);
        Object [] elements = listeners.toArray();
        for(int i = 0; i < elements.length; i++){
            ConnectionEventListener eventListener = (ConnectionEventListener)elements[i];
            eventListener.localTransactionCommitted(event);
        }
    }
    
    protected void localTransactionRolledback(){
        ConnectionEvent event = new ConnectionEvent(this, ConnectionEvent.LOCAL_TRANSACTION_ROLLEDBACK);
        Object [] elements = listeners.toArray();
        for(int i = 0; i < elements.length; i++){
            ConnectionEventListener eventListener = (ConnectionEventListener)elements[i];
            eventListener.localTransactionRolledback(event);
        }
    }
    
    protected void localTransactionStarted(){
        ConnectionEvent event = new ConnectionEvent(this, ConnectionEvent.LOCAL_TRANSACTION_STARTED);
        Object [] elements = listeners.toArray();
        for(int i = 0; i < elements.length; i++){
            ConnectionEventListener eventListener = (ConnectionEventListener)elements[i];
            eventListener.localTransactionStarted(event);
        }
    }
    
    protected void connectionErrorOccurred(JdbcConnection jdbcConn, java.sql.SQLException sqlE){
        
        if(logWriter !=null){
            logWriter.print("\nJdbcConnection Error: On java.sql.Connection (");
            logWriter.print(jdbcConn);
            logWriter.println(")");
            logWriter.println("Exception Stack trace follows:");
            sqlE.printStackTrace(logWriter);
            java.sql.SQLException temp = sqlE;
            while((temp = sqlE.getNextException())!= null){
                temp.printStackTrace(logWriter);
            }
        }
        
        ConnectionEvent event = new ConnectionEvent(this, ConnectionEvent.CONNECTION_ERROR_OCCURRED , sqlE);
        Object [] elements = listeners.toArray();
        for(int i = 0; i < elements.length; i++){
            ConnectionEventListener eventListener = (ConnectionEventListener)elements[i];
            eventListener.connectionErrorOccurred(event);
        }
    }
    
    /**
    * Invoked by the JdbcConneciton when its close() method is called.
    * This method invalidates the JdbcConnection handle, removes it from
    * the list of active handles and notifies all the ConnectionEventListeners.
    */
    protected void connectionClose(JdbcConnection jdbcConn){
        jdbcConn.invalidate();
        jdbcConnections.remove(jdbcConn);
        ConnectionEvent event = new ConnectionEvent(this, ConnectionEvent.CONNECTION_CLOSED);
        Object [] elements = listeners.toArray();
        for(int i = 0; i < elements.length; i++){
            ConnectionEventListener eventListener = (ConnectionEventListener)elements[i];
            eventListener.connectionClosed(event);
        }
    }
    
    public String toString( ){
        return "JdbcManagedConnection ("+sqlConn.toString()+")";
    }
}
