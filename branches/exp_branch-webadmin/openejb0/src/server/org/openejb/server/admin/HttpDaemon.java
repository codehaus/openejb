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
package org.openejb.server.admin;

import java.util.Collection;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.NotSerializableException;
import java.io.WriteAbortedException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.InetAddress;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;
import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import javax.naming.*;
import org.openejb.client.*;
import org.openejb.server.EjbDaemon;
import org.openejb.client.proxy.*;
import org.openejb.Container;
import org.openejb.DeploymentInfo;
import org.openejb.EnvProps;
import org.openejb.spi.SecurityService;
import org.openejb.InvalidateReferenceException;
import org.openejb.OpenEJB;
import org.openejb.OpenEJBException;
import org.openejb.ProxyInfo;
import org.openejb.RpcContainer;
import org.openejb.util.SafeProperties;
import org.openejb.util.SafeToolkit;
import org.openejb.util.FileUtils;
import org.openejb.util.JarUtils;
import org.openejb.util.Logger;
import org.openejb.admin.web.HttpObject;
import org.openejb.admin.web.HttpHome;
import javax.rmi.PortableRemoteObject;

/**
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @since 11/25/2001
 */
public class HttpDaemon implements Runnable{

    private SafeToolkit toolkit = SafeToolkit.getToolkit("OpenEJB EJB Server");

    Logger logger = Logger.getInstance( "OpenEJB", "org.openejb.server.util.resources" );

    Vector           clientSockets  = new Vector();
    ServerSocket     serverSocket   = null;

    // The EJB Server Port
    int    port = 4202;
    String ip   = "127.0.0.1";
    Properties props;
    EjbDaemon ejbd;
    InitialContext jndiContext;

    public HttpDaemon(EjbDaemon ejbd) {
        this.ejbd = ejbd;
    }

    public void init(Properties props) throws Exception{

        props.putAll(System.getProperties());

        Properties properties = new Properties();
        properties.put(Context.INITIAL_CONTEXT_FACTORY, "org.openejb.core.ivm.naming.InitContextFactory");
        jndiContext = new InitialContext(properties);
        
        SafeProperties safeProps = new SafeProperties(System.getProperties(),"HTTP Server");
        port = safeProps.getPropertyAsInt("openejb.server.port");
        port += 2;

        try{
            serverSocket = new ServerSocket(port);                                    
            //serverSocket = new ServerSocket(port, 20, InetAddress.getByName(ip));
        } catch (Exception e){
            System.out.println("Cannot bind to the ip: "+ip+" and port: "+port+".  Received exception: "+ e.getClass().getName()+":"+ e.getMessage());
        }
    }

    // This class doesn't use its own namespace, it uses the
    // jndi context of OpenEJB
    boolean stop = false;


    public void run( ) {

        Socket socket = null;

        /**
         * The ObjectInputStream used to receive incoming messages from the client.
         */
        InputStream in = null;
        /**
         * The ObjectOutputStream used to send outgoing response messages to the client.
         */
        OutputStream out = null;

        InetAddress clientIP = null;
        while ( !stop ) {
            try {
                socket = serverSocket.accept();
                
                clientIP = socket.getInetAddress();
                InetAddress serverIP = serverSocket.getInetAddress();
                
                Thread.currentThread().setName(clientIP.getHostAddress());

                in  = socket.getInputStream();
                out = socket.getOutputStream();

                try{            
                    EjbDaemon.checkHostsAdminAuthorization(clientIP, serverIP);
                    
                    // This will not get called if a SecurityException was thrown
                    processRequest(in, out); 
                } catch (SecurityException e){
                    HttpResponseImpl res = HttpResponseImpl.createForbidden(clientIP.getHostAddress());
                    try {
                        res.writeMessage( out );
                    } catch (Throwable t2) {
                        t2.printStackTrace();
                    }
                    try{
                        out.close();
                        socket.close();
                    } catch (Exception dontCare){}
                }

                // Exceptions should not be thrown from these methods
                // They should handle their own exceptions and clean
                // things up with the client accordingly.
            } catch ( Throwable e ) {
                logger.error( "Unexpected error", e );
                System.out.println("ERROR: "+clientIP.getHostAddress()+": " +e.getMessage());
            } finally {
                try {
                    if ( out != null ) {
			out.flush();
			out.close();
		    }
                    if ( in != null ) in.close();
                    if ( socket != null ) socket.close();
                } catch ( Throwable t ){
                    logger.error("Encountered problem while closing connection with client: "+t.getMessage());
                }
            }
        }
    }

    public void processRequest(InputStream in, OutputStream out) {

        HttpRequestImpl req = new HttpRequestImpl();
        HttpResponseImpl res = new HttpResponseImpl();
        System.out.println("[] reading request");

        try {
            req.readMessage( in );
        } catch (Throwable t) {
            t.printStackTrace();
            res = HttpResponseImpl.createError("Could read the request.\n"+t.getClass().getName()+":\n"+t.getMessage(), t);
            try {
                res.writeMessage( out );
            } catch (Throwable t2) {
                t2.printStackTrace();
            }
            return;
        }

        System.out.println("[] read");
        URL uri = null;
        String file = null;

        try{
            uri = req.getURI();
            file = uri.getFile();
            int querry = file.indexOf("?");
            if (querry != -1) {
                file = file.substring(0, querry);
            }
            
            System.out.println("[] file="+file);
            
        } catch (Throwable t) {
            t.printStackTrace();
            res = HttpResponseImpl.createError("Could not determine the module "+file+"\n"+t.getClass().getName()+":\n"+t.getMessage());
            try {
                res.writeMessage( out );
            } catch (Throwable t2) {
                t2.printStackTrace();
            }
            return;
        }

        HttpObject httpObject = null;

        try{
            httpObject = getHttpObject(file);
            System.out.println("[] module="+httpObject);
        } catch (Throwable t) {
            t.printStackTrace();
            res = HttpResponseImpl.createError("Could not load the module "+file+"\n"+t.getClass().getName()+":\n"+t.getMessage(), t);
            System.out.println("[] res="+res);
            try {
                res.writeMessage( out );
            } catch (Throwable t2) {
                t2.printStackTrace();
            }
            return;
        }

        try{
            httpObject.onMessage(req, res);
        } catch (Throwable t) {
            t.printStackTrace();
            res = HttpResponseImpl.createError("Error occurred while executing the module "+file+"\n"+t.getClass().getName()+":\n"+t.getMessage(), t);
            try {
                res.writeMessage( out );
            } catch (Throwable t2) {
                t2.printStackTrace();
            }

            return;
        }

//      java.io.PrintWriter body = res.getPrintWriter();
//
//      body.println("<html>");
//      body.println("<body>");
//      body.println("<br><br><br><br>");
//      body.println("<h1>"+file+"</h1>");
//      body.println("</body>");
//      body.println("</html>");

        try {
            res.writeMessage( out );
        } catch (Throwable t) {
            t.printStackTrace();
            return;
        }
    }

    protected HttpObject getHttpObject(String beanName) throws IOException{
        Object obj = null;
        try{
            obj = jndiContext.lookup("webadmin/"+beanName);
        } catch (javax.naming.NameNotFoundException e){
            try {obj = jndiContext.lookup("webadmin/DefaultBean");} 
            catch(javax.naming.NamingException ne) {throw new IOException(ne.getMessage());}
        } catch (javax.naming.NamingException e){
            throw new IOException(e.getMessage());
        }

        HttpHome ejbHome = (HttpHome)obj;
        HttpObject httpObject = null;
        
        try {
            httpObject = ejbHome.create();
            
            // 
            obj = org.openejb.util.proxy.ProxyManager.getInvocationHandler(httpObject);
            org.openejb.core.ivm.BaseEjbProxyHandler handler = null;
            handler = (org.openejb.core.ivm.BaseEjbProxyHandler)obj;
            handler.setIntraVmCopyMode(false);
        } catch (javax.ejb.CreateException cre) {
            throw new IOException(cre.getMessage());
        }
        
        return httpObject;
    }
}