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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.openejb.DeploymentInfo;
import org.openejb.OpenEJB;
import org.openejb.ProxyInfo;
import org.openejb.RpcContainer;
import org.openejb.client.AuthenticationRequest;
import org.openejb.client.AuthenticationResponse;
import org.openejb.client.ClientMetaData;
import org.openejb.client.EJBHomeHandle;
import org.openejb.client.EJBHomeHandler;
import org.openejb.client.EJBMetaDataImpl;
import org.openejb.client.EJBObjectHandle;
import org.openejb.client.EJBObjectHandler;
import org.openejb.client.EJBRequest;
import org.openejb.client.EJBResponse;
import org.openejb.client.JNDIRequest;
import org.openejb.client.JNDIResponse;
import org.openejb.client.RequestMethods;
import org.openejb.client.ResponseCodes;
import org.openejb.client.ServerMetaData;
import org.openejb.spi.SecurityService;
import org.openejb.util.JarUtils;
import org.openejb.util.Logger;
import org.openejb.util.Messages;
import org.openejb.util.SafeProperties;
import org.openejb.util.SafeToolkit;

/**
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @since 11/25/2001
 */
public class EjbDaemon implements org.openejb.spi.ApplicationServer, ResponseCodes, RequestMethods {

    private SafeToolkit toolkit = SafeToolkit.getToolkit("OpenEJB EJB Server");

    Messages _messages = new Messages( "org.openejb.server.util.resources" );
    Logger logger = Logger.getInstance( "OpenEJB.server.remote", "org.openejb.server.util.resources" );

    Vector           clientSockets  = new Vector();
    ServerSocket     serverSocket   = null;
    ServerMetaData   sMetaData      = null;

    // The EJB Server Port
    int    port = 4201;
    String ip   = "127.0.0.1";
    Properties props;
    ClientObjectFactory clientObjectFactory;
    DeploymentIndex deploymentIndex;
    EjbRequestHandler ejbHandler;

    static InetAddress[] admins;
    boolean stop = false;

    private EjbDaemon() {
    }
    static EjbDaemon thiss;

    public static EjbDaemon getEjbDaemon() {
        if ( thiss == null ) {
            thiss = new EjbDaemon();
        }

        return thiss;
    }

    public void init(Properties props) throws Exception{

        this.props = props;
        //printVersion();

        System.out.println( _messages.message( "ejbdaemon.startup" ) );

        //TODO: Pass this class to the ServerManager before calls
        //OpenEJB.init(props, this);

        System.out.println("[init] OpenEJB Remote Server");


        clientJndi = (javax.naming.Context)OpenEJB.getJNDIContext().lookup("openejb/ejb");

        deploymentIndex = new DeploymentIndex();

        sMetaData = new ServerMetaData("127.0.0.1", 4201);
        clientObjectFactory = new ClientObjectFactory();
        ejbHandler = new EjbRequestHandler();
    }

    public void service(Socket socket) throws IOException{
        InputStream  in  = socket.getInputStream();
        OutputStream out = socket.getOutputStream();

        /**
         * The ObjectInputStream used to receive incoming messages from the client.
         */
        ObjectInputStream ois = null;
        /**
         * The ObjectOutputStream used to send outgoing response messages to the client.
         */
        ObjectOutputStream oos = null;


        try {

            //while ( !stop ) {

            // Read the request
            byte requestType = (byte)in.read();

            //if (requestType == -1) {continue;}
            if (requestType == -1) {
                return;
            }

            ois = new ObjectInputStream( in );
            oos = new ObjectOutputStream( out );

            // Process the request
            switch (requestType) {
                case EJB_REQUEST:  processEjbRequest(ois, oos); break;
                case JNDI_REQUEST: processJndiRequest(ois, oos);break;
                case AUTH_REQUEST: processAuthRequest(ois, oos);break;
                default: logger.error("Unknown request type "+requestType);
            }
            try {
                if ( oos != null ) {
                    oos.flush();
                }
            } catch ( Throwable t ) {
                logger.error("Encountered problem while communicating with client: "+t.getMessage());
            }
            //}

            // Exceptions should not be thrown from these methods
            // They should handle their own exceptions and clean
            // things up with the client accordingly.
        } catch ( SecurityException e ) {
            logger.error( "Security error: "+ e.getMessage() );
        } catch ( Throwable e ) {
            logger.error( "Unexpected error", e );
            //System.out.println("ERROR: "+clienntIP.getHostAddress()+": " +e.getMessage());
        } finally {
            try {
                if ( oos != null ) {
                    oos.flush();
                    oos.close();
                }
                if ( ois    != null ) ois.close();
                if ( in     != null ) in.close();
                if ( socket != null ) socket.close();
            } catch ( Throwable t ) {
                logger.error("Encountered problem while closing connection with client: "+t.getMessage());
            }
        }
    }

    private DeploymentInfo getDeployment(EJBRequest req) throws RemoteException {
        return deploymentIndex.getDeployment(req);
    }

    public void processEjbRequest (ObjectInputStream in, ObjectOutputStream out) {
        ejbHandler.processRequest(in,out);
    }

    static javax.naming.Context clientJndi;

    public void processJndiRequest(ObjectInputStream in, ObjectOutputStream out) throws Exception{
        JNDIRequest  req = new JNDIRequest();
        JNDIResponse res = new JNDIResponse();
        req.readExternal( in );

        // We are assuming that the request method is JNDI_LOOKUP
        // TODO: Implement the JNDI_LIST and JNDI_LIST_BINDINGS methods

        String name = req.getRequestString();
        if ( name.startsWith("/") ) name = name.substring(1);

        DeploymentInfo deployment = deploymentIndex.getDeployment(name);

        if (deployment == null) {
            try {
                Object obj = clientJndi.lookup(name);

                if ( obj instanceof Context ) {
                    res.setResponseCode( JNDI_CONTEXT );
                } else res.setResponseCode( JNDI_NOT_FOUND );

            } catch (NameNotFoundException e) {
                res.setResponseCode(JNDI_NOT_FOUND);
            } catch (NamingException e) {
                res.setResponseCode(JNDI_NAMING_EXCEPTION);
                res.setResult( e );
            }
        } else {
            res.setResponseCode( JNDI_EJBHOME );
            EJBMetaDataImpl metaData = new EJBMetaDataImpl(deployment.getHomeInterface(),
                                                           deployment.getRemoteInterface(),
                                                           deployment.getPrimaryKeyClass(),
                                                           deployment.getComponentType(),
                                                           deployment.getDeploymentID().toString(),
                                                           deploymentIndex.getDeploymentIndex(name));
            res.setResult( metaData );
        }

        res.writeExternal( out );
    }

    public void processAuthRequest(ObjectInputStream in, ObjectOutputStream out) throws Exception{
        AuthenticationRequest req = new AuthenticationRequest();
        AuthenticationResponse res = new AuthenticationResponse();

        try {
            req.readExternal( in );

            // TODO: perform some real authentication here

            ClientMetaData client = new ClientMetaData();

            client.setClientIdentity( new String( (String)req.getPrinciple() ) );

            res.setIdentity( client );
            res.setResponseCode( AUTH_GRANTED );

            res.writeExternal( out );
        } catch (Throwable t) {
            //replyWithFatalError
            //(out, t, "Error caught during request processing");
            return;
        }
    }



    //=============================================================
    //  ApplicationServer interface methods
    //=============================================================
    public javax.ejb.EJBMetaData getEJBMetaData(ProxyInfo info) {
        return clientObjectFactory.getEJBMetaData(info);
    }

    public javax.ejb.Handle getHandle(ProxyInfo info) {
        return clientObjectFactory.getHandle(info);
    }

    public javax.ejb.HomeHandle getHomeHandle(ProxyInfo info) {
        return clientObjectFactory.getHomeHandle(info);
    }

    public javax.ejb.EJBObject getEJBObject(ProxyInfo info) {
        return clientObjectFactory.getEJBObject(info);
    }

    public javax.ejb.EJBHome getEJBHome(ProxyInfo info) {
        return clientObjectFactory.getEJBHome(info);
    }

    /**
     * The implementation of ApplicationServer used to create all client-side
     * implementations of the javax.ejb.* interaces as
     * 
     * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
     */
    class ClientObjectFactory implements org.openejb.spi.ApplicationServer {

        /*
        protected ServerMetaData sMetaData;
        protected HashMap deploymentsMap;
    
        public ClientObjectFactory(ServerMetaData sMetaData, HashMap deploymentsMap){
            this.sMetaData = sMetaData;
        }
        */

        public javax.ejb.EJBMetaData getEJBMetaData(ProxyInfo info) {
            CallContext call = CallContext.getCallContext();
            return _getEJBMetaData(call, info);
        }

        /**
         * Creates a Handle object that can be serialized and 
         * sent to the client.
         * 
         * @param info
         * 
         * @return 
         */
        public javax.ejb.Handle getHandle(ProxyInfo info) {
            CallContext call = CallContext.getCallContext();
            return _getHandle(call, info);
        }

        /**
         * Creates a HomeHandle object that can be serialized and 
         * sent to the client.
         * 
         * @param info
         * 
         * @return 
         */
        public javax.ejb.HomeHandle getHomeHandle(ProxyInfo info) {
            CallContext call = CallContext.getCallContext();
            return _getHomeHandle(call, info);
        }

        /**
         * Creates an EJBObject object that can be serialized and 
         * sent to the client.
         * 
         * @param info
         * 
         * @return 
         */
        public javax.ejb.EJBObject getEJBObject(ProxyInfo info) {
            CallContext call = CallContext.getCallContext();
            return _getEJBObject(call, info);
        }

        /**
         * Creates an EJBHome object that can be serialized and 
         * sent to the client.
         * 
         * @param info
         * 
         * @return 
         */
        public javax.ejb.EJBHome getEJBHome(ProxyInfo info) {
            CallContext call = CallContext.getCallContext();
            return _getEJBHome(call, info);
        }

        /**
         * Creates an EJBMetaDataImpl object that can be serialized and
         * sent to the client.
         * 
         * @param call
         * @param info
         * 
         * @return 
         * @see org.openejb.client.EJBMetaDataImpl
         */
        protected javax.ejb.EJBMetaData _getEJBMetaData(CallContext call, ProxyInfo info) {

            DeploymentInfo deployment = info.getDeploymentInfo();
            int idCode = deploymentIndex.getDeploymentIndex(deployment);

            EJBMetaDataImpl metaData = new EJBMetaDataImpl(deployment.getHomeInterface(),
                                                           deployment.getRemoteInterface(),
                                                           deployment.getPrimaryKeyClass(),
                                                           deployment.getComponentType(),
                                                           deployment.getDeploymentID().toString(),
                                                           idCode);
            return metaData;
        }

        /**
         * Creates an EJBMetaDataImpl object that can be serialized and
         * sent to the client.
         * 
         * @param call
         * @param info
         * 
         * @return 
         * @see org.openejb.client.EJBObjectHandle
         */
        protected javax.ejb.Handle _getHandle(CallContext call, ProxyInfo info) {
            DeploymentInfo deployment = info.getDeploymentInfo();

            int idCode = deploymentIndex.getDeploymentIndex(deployment);

            Object securityIdentity = null;
            try {
                securityIdentity = call.getEJBRequest().getClientIdentity();
            } catch (Exception e) {
                //e.printStackTrace();  not needed
            }
            ClientMetaData  cMetaData = new ClientMetaData(securityIdentity);
            EJBMetaDataImpl eMetaData = new EJBMetaDataImpl(deployment.getHomeInterface(),
                                                            deployment.getRemoteInterface(),
                                                            deployment.getPrimaryKeyClass(),
                                                            deployment.getComponentType(),
                                                            deployment.getDeploymentID().toString(),
                                                            idCode);
            Object primKey = info.getPrimaryKey();

            EJBObjectHandler hanlder = EJBObjectHandler.createEJBObjectHandler(eMetaData,sMetaData,cMetaData,primKey);

            return new EJBObjectHandle( hanlder.createEJBObjectProxy() );
        }

        /**
         * Creates an EJBHomeHandle object that can be serialized and
         * sent to the client.
         * 
         * @param call
         * @param info
         * 
         * @return 
         * @see org.openejb.client.EJBHomeHandle
         */
        protected javax.ejb.HomeHandle _getHomeHandle(CallContext call, ProxyInfo info) {
            DeploymentInfo deployment = info.getDeploymentInfo();

            int idCode = deploymentIndex.getDeploymentIndex(deployment);

            Object securityIdentity = null;
            try {
                securityIdentity = call.getEJBRequest().getClientIdentity();
            } catch (Exception e) {
                e.printStackTrace();
            }
            ClientMetaData  cMetaData = new ClientMetaData(securityIdentity);
            EJBMetaDataImpl eMetaData = new EJBMetaDataImpl(deployment.getHomeInterface(),
                                                            deployment.getRemoteInterface(),
                                                            deployment.getPrimaryKeyClass(),
                                                            deployment.getComponentType(),
                                                            deployment.getDeploymentID().toString(),
                                                            idCode);

            EJBHomeHandler hanlder = EJBHomeHandler.createEJBHomeHandler(eMetaData,sMetaData,cMetaData);

            return new EJBHomeHandle( hanlder.createEJBHomeProxy() );
        }

        /**
         * Creates an EJBObjectHandler and EJBObject proxy object that can
         * be serialized and sent to the client.
         * 
         * @param call
         * @param info
         * 
         * @return 
         * @see org.openejb.client.EJBObjectHandler
         */
        protected javax.ejb.EJBObject _getEJBObject(CallContext call, ProxyInfo info) {
            DeploymentInfo deployment = info.getDeploymentInfo();

            int idCode = deploymentIndex.getDeploymentIndex(deployment);

            Object securityIdentity = null;
            try {
                securityIdentity = call.getEJBRequest().getClientIdentity();
            } catch (Exception e) {
                e.printStackTrace();
            }
            ClientMetaData  cMetaData = new ClientMetaData(securityIdentity);
            EJBMetaDataImpl eMetaData = new EJBMetaDataImpl(deployment.getHomeInterface(),
                                                            deployment.getRemoteInterface(),
                                                            deployment.getPrimaryKeyClass(),
                                                            deployment.getComponentType(),
                                                            deployment.getDeploymentID().toString(),
                                                            idCode);
            Object primKey = info.getPrimaryKey();

            EJBObjectHandler hanlder = EJBObjectHandler.createEJBObjectHandler(eMetaData,sMetaData,cMetaData,primKey);

            return hanlder.createEJBObjectProxy();
        }

        /**
         * Creates an EJBHomeHandler and EJBHome proxy object that can
         * be serialized and sent to the client.
         * 
         * @param call
         * @param info
         * 
         * @return 
         * @see org.openejb.client.EJBHomeHandler
         */
        protected javax.ejb.EJBHome _getEJBHome(CallContext call, ProxyInfo info) {
            DeploymentInfo deployment = info.getDeploymentInfo();

            int idCode = deploymentIndex.getDeploymentIndex(deployment);

            Object securityIdentity = null;
            try {
                securityIdentity = call.getEJBRequest().getClientIdentity();
            } catch (Exception e) {
                e.printStackTrace();
            }
            ClientMetaData  cMetaData = new ClientMetaData(securityIdentity);
            EJBMetaDataImpl eMetaData = new EJBMetaDataImpl(deployment.getHomeInterface(),
                                                            deployment.getRemoteInterface(),
                                                            deployment.getPrimaryKeyClass(),
                                                            deployment.getComponentType(),
                                                            deployment.getDeploymentID().toString(),
                                                            idCode);

            EJBHomeHandler hanlder = EJBHomeHandler.createEJBHomeHandler(eMetaData,sMetaData,cMetaData);

            //EJBHomeProxyHandle handle = new EJBHomeProxyHandle( hanlder );

            return hanlder.createEJBHomeProxy();
        }
    }


    class EjbRequestHandler {

        public void processRequest(ObjectInputStream in, ObjectOutputStream out) {
            EJBRequest req = new EJBRequest();
            EJBResponse res = new EJBResponse();

            // TODO:2: This method can throw a large number of exceptions, we should
            // be prepared to handle them all.  Look in the ObejctOutputStream code
            // for a full list of the exceptions thrown.
            // java.io.WriteAbortedException  can be thrown containing a
            //
            try {
                req.readExternal( in );

                /*
                    } catch (java.io.WriteAbortedException e){
                        if ( e.detail instanceof java.io.NotSerializableException){
                            //TODO:1: Log this warning better. Include information on what bean is to blame
                            throw new Exception("Client attempting to serialize unserializable object: "+ e.detail.getMessage());
                        } else {
                            throw e.detail;
                        }
                    } catch (java.io.EOFException e) {
                        throw new Exception("Reached the end of the stream before the full request could be read");
                    } catch (Throwable t){
                        throw new Exception("Cannot read client request: "+ t.getClass().getName()+" "+ t.getMessage());
                    }
                */

            } catch (Throwable t) {
                replyWithFatalError
                (out, t, "Error caught during request processing");
                return;
            }

            CallContext  call = null;
            DeploymentInfo di = null;
            RpcContainer    c = null;;

            try {
                di = getDeployment(req);
            } catch (RemoteException e) {
                replyWithFatalError
                (out, e, "No such deployment");
                return;
                /*
                    logger.warn( req + "No such deployment: "+e.getMessage());
                    res.setResponse( EJB_SYS_EXCEPTION, e);
                    res.writeExternal( out );
                    return;
                */
            } catch ( Throwable t ) {
                replyWithFatalError
                    (out, t, "Unkown error occured while retrieving deployment");
                return;
            }

            try {
                call = CallContext.getCallContext();
                call.setEJBRequest( req );
                call.setDeploymentInfo( di );
            } catch ( Throwable t ) {
                replyWithFatalError
                    (out, t, "Unable to set the thread context for this request");
                return;
            }

            //logger.info( "EJB REQUEST : "+req );

            try {
                switch (req.getRequestMethod()) {
                // Remote interface methods
                case EJB_OBJECT_BUSINESS_METHOD:
                    doEjbObject_BUSINESS_METHOD( req, res );
                    break;

                    // Home interface methods
                case EJB_HOME_CREATE:
                    doEjbHome_CREATE( req, res );
                    break;

                case EJB_HOME_FIND:
                    doEjbHome_FIND( req, res );
                    break;

                    // javax.ejb.EJBObject methods
                case EJB_OBJECT_GET_EJB_HOME:
                    doEjbObject_GET_EJB_HOME( req, res );
                    break;

                case EJB_OBJECT_GET_HANDLE:
                    doEjbObject_GET_HANDLE( req, res );
                    break;

                case EJB_OBJECT_GET_PRIMARY_KEY:
                    doEjbObject_GET_PRIMARY_KEY( req, res );
                    break;

                case EJB_OBJECT_IS_IDENTICAL:
                    doEjbObject_IS_IDENTICAL( req, res );
                    break;

                case EJB_OBJECT_REMOVE:
                    doEjbObject_REMOVE( req, res );
                    break;

                    // javax.ejb.EJBHome methods
                case EJB_HOME_GET_EJB_META_DATA:
                    doEjbHome_GET_EJB_META_DATA( req, res );
                    break;

                case EJB_HOME_GET_HOME_HANDLE:
                    doEjbHome_GET_HOME_HANDLE( req, res );
                    break;

                case EJB_HOME_REMOVE_BY_HANDLE:
                    doEjbHome_REMOVE_BY_HANDLE( req, res );
                    break;

                case EJB_HOME_REMOVE_BY_PKEY:
                    doEjbHome_REMOVE_BY_PKEY( req, res );
                    break;
                }


            } catch (org.openejb.InvalidateReferenceException e) {
                res.setResponse(EJB_SYS_EXCEPTION, e.getRootCause());
            } catch (org.openejb.ApplicationException e) {
                res.setResponse(EJB_APP_EXCEPTION, e.getRootCause());
            } catch (org.openejb.SystemException e) {
                res.setResponse(EJB_ERROR, e.getRootCause());
                // TODO:2: This means a severe error occured in OpenEJB
                // we should restart the container system or take other
                // aggressive actions to attempt recovery.
                logger.fatal( req+": OpenEJB encountered an unknown system error in container: ", e);
            } catch (java.lang.Throwable t) {
                //System.out.println(req+": Unkown error in container: ");
                replyWithFatalError
                (out, t, "Unknown error in container");
                return;
            } finally {
                logger.info( "EJB RESPONSE: "+res );
                try {
                    res.writeExternal( out );
                } catch (java.io.IOException ie) {
                    logger.fatal("Couldn't write EjbResponse to output stream", ie);
                }
                call.reset();
            }
        }
    
        protected void doEjbObject_BUSINESS_METHOD( EJBRequest req, EJBResponse res ) throws Exception {

            CallContext call = CallContext.getCallContext();
            RpcContainer c   = (RpcContainer)call.getDeploymentInfo().getContainer();

            Object result = c.invoke( req.getDeploymentId(),
                                      req.getMethodInstance(),
                                      req.getMethodParameters(),
                                      req.getPrimaryKey(),
                                      req.getClientIdentity());

            if (result instanceof ProxyInfo) {
                ProxyInfo info = (ProxyInfo)result;

                if ( EJBObject.class.isAssignableFrom(info.getInterface()) ) {
                    result = clientObjectFactory._getEJBObject(call, info);
                } else if ( EJBHome.class.isAssignableFrom(info.getInterface()) ) {
                    result = clientObjectFactory._getEJBHome(call, info);
                } else {
                    // Freak condition
                    //TODO:3: Localize all error messages in an separate file.
                    result = new RemoteException("The container returned a ProxyInfo object that is neither a javax.ejb.EJBObject or javax.ejb.EJBHome: "+info.getInterface());
                    logger.error( req + "The container returned a ProxyInfo object that is neither a javax.ejb.EJBObject or javax.ejb.EJBHome: "+info.getInterface());
                    res.setResponse( EJB_SYS_EXCEPTION, result);
                    return;
                }
            }

            res.setResponse( EJB_OK, result);
        }


        // Home interface methods
        protected void doEjbHome_CREATE( EJBRequest req, EJBResponse res ) throws Exception {

            CallContext call = CallContext.getCallContext();
            RpcContainer c   = (RpcContainer)call.getDeploymentInfo().getContainer();

            Object result = c.invoke( req.getDeploymentId(),
                                      req.getMethodInstance(),
                                      req.getMethodParameters(),
                                      req.getPrimaryKey(),
                                      req.getClientIdentity());

            if (result instanceof ProxyInfo) {
                ProxyInfo info = (ProxyInfo)result;
                res.setResponse(EJB_OK, info.getPrimaryKey());
            } else {
                // There should be no else, the entity should be found
                // or and exception should be thrown.
                //TODO:3: Localize all error messages in an separate file.
                result = new RemoteException("The bean is not EJB compliant.  The should be created or and exception should be thrown.");
                logger.error( req + "The bean is not EJB compliant.  The should be created or and exception should be thrown.");
                res.setResponse( EJB_SYS_EXCEPTION, result);
            }
        }

        /**
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
         *
         * @param req
         * @param in
         * @param out
         * @exception Exception
         */
        protected void doEjbHome_FIND( EJBRequest req, EJBResponse res ) throws Exception {

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

                for (int i=0; i < primaryKeys.length; i++) {
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

        // javax.ejb.EJBObject methods
        protected void doEjbObject_GET_EJB_HOME( EJBRequest req, EJBResponse res ) throws Exception {
            checkMethodAuthorization( req, res );
        }

        protected void doEjbObject_GET_HANDLE( EJBRequest req, EJBResponse res ) throws Exception {
            checkMethodAuthorization( req, res );
        }

        protected void doEjbObject_GET_PRIMARY_KEY( EJBRequest req, EJBResponse res ) throws Exception {
            checkMethodAuthorization( req, res );
        }

        protected void doEjbObject_IS_IDENTICAL( EJBRequest req, EJBResponse res ) throws Exception {
            checkMethodAuthorization( req, res );
        }

        protected void doEjbObject_REMOVE( EJBRequest req, EJBResponse res ) throws Exception {

            CallContext call = CallContext.getCallContext();
            RpcContainer c   = (RpcContainer)call.getDeploymentInfo().getContainer();

            Object result = c.invoke( req.getDeploymentId(),
                                      req.getMethodInstance(),
                                      req.getMethodParameters(),
                                      req.getPrimaryKey(),
                                      req.getClientIdentity());

            res.setResponse( EJB_OK, null);
        }

        // javax.ejb.EJBHome methods
        protected void doEjbHome_GET_EJB_META_DATA( EJBRequest req, EJBResponse res ) throws Exception {
            checkMethodAuthorization( req, res );
        }

        protected void doEjbHome_GET_HOME_HANDLE( EJBRequest req, EJBResponse res ) throws Exception {
            checkMethodAuthorization( req, res );
        }

        protected void doEjbHome_REMOVE_BY_HANDLE( EJBRequest req, EJBResponse res ) throws Exception {

            CallContext call = CallContext.getCallContext();
            RpcContainer c   = (RpcContainer)call.getDeploymentInfo().getContainer();

            Object result = c.invoke( req.getDeploymentId(),
                                      req.getMethodInstance(),
                                      req.getMethodParameters(),
                                      req.getPrimaryKey(),
                                      req.getClientIdentity());

            res.setResponse( EJB_OK, null);
        }

        protected void doEjbHome_REMOVE_BY_PKEY( EJBRequest req, EJBResponse res ) throws Exception {

            CallContext call = CallContext.getCallContext();
            RpcContainer c   = (RpcContainer)call.getDeploymentInfo().getContainer();

            Object result = c.invoke( req.getDeploymentId(),
                                      req.getMethodInstance(),
                                      req.getMethodParameters(),
                                      req.getPrimaryKey(),
                                      req.getClientIdentity());

            res.setResponse( EJB_OK, null);
        }

        protected void checkMethodAuthorization( EJBRequest req, EJBResponse res ) throws Exception {
            // Nothing to do here other than check to see if the client
            // is authorized to call this method
            // TODO:3: Keep a cache in the client-side handler of methods it can't access

            SecurityService sec = OpenEJB.getSecurityService();
            CallContext caller  = CallContext.getCallContext();
            DeploymentInfo di   = caller.getDeploymentInfo();
            String[] authRoles  = di.getAuthorizedRoles( req.getMethodInstance() );

            if (sec.isCallerAuthorized( req.getClientIdentity(), authRoles )) {
                res.setResponse( EJB_OK, null );
            } else {
                logger.info(req + "Unauthorized Access by Principal Denied");
                res.setResponse( EJB_APP_EXCEPTION , new RemoteException("Unauthorized Access by Principal Denied") );
            }
        }

        private void replyWithFatalError(ObjectOutputStream out,Throwable error,String message) {
            logger.fatal(message, error);
            RemoteException re = new RemoteException
                                 ("The server has encountered a fatal error: "+message+" "+error);
            EJBResponse res = new EJBResponse();
            res.setResponse(EJB_ERROR, re);
            try {
                res.writeExternal(out);
            } catch (java.io.IOException ie) {
                logger.error("Failed to write to EJBResponse", ie);
            }
        }
    }
}

