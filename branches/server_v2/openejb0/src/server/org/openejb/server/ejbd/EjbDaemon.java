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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Properties;

import org.openejb.server.ServiceException;

/**
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @since 11/25/2001
 */
public class EjbDaemon implements org.openejb.server.ServerService {
    
    public void init(Properties props) throws Exception {
    }
    
    boolean stop;
    
    public void service(Socket socket) throws ServiceException,IOException {
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

            while ( !stop ) {
                
                // Read the request
                byte requestType = (byte)in.read();
                
                if (requestType == -1) {continue;}
                
                ois = new ObjectInputStream( in );
                oos = new ObjectOutputStream( out );

                // Process the request
                switch (requestType) {
                    case EJB_REQUEST:  processEjbRequest(ois, oos); break;
                    case JNDI_REQUEST: processJndiRequest(ois, oos);break;
                    case AUTH_REQUEST: processAuthRequest(ois, oos);break;
                    default: logger.error("Unknown request type "+requestType);
                }
            }
            try {
                if ( oos != null ) {
                    oos.flush();
                }
            } catch ( Throwable t ){
                logger.error("Encountered problem while communicating with client: "+t.getMessage());
            }
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
            } catch ( Throwable t ){
                logger.error("Encountered problem while closing connection with client: "+t.getMessage());
            }
        }
    }
    
    public void start() throws ServiceException {
    }
    
    public void stop() throws ServiceException {
    }
    
    public String getName() {
        return "ejbd";
    }
    
    public int getPort() {
        return 0;
    }

    public String getIP() {
        return "";
    }

}
