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
package org.openejb.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.MDC;

/**
 *  The Server will call the following methods.
 *
 *    newInstance()
 *    init( port, properties)
 *    start()
 *    stop()
 *
 * All ServerService implementations must have a no argument
 * constructor.
 *
 */
public class ServiceLogger implements ServerService {
    private final Log log;
    private final ServerService next;

    public ServiceLogger(ServerService next) {
        this.next = next;
        log = LogFactory.getLog("OpenEJB.server.service." + getName());
    }

    /**
     * log_on_success
     * -----------------
     * Different information can be logged when a server starts:
     *
     * PID : the server's PID (if it's an internal xinetd service, the PID has then a value of 0) ;
     * HOST : the client address ;
     * USERID : the identity of the remote user, according to RFC1413 defining identification protocol;
     * EXIT : the process exit status;
     * DURATION : the session duration.
     *
     * log_on_failure
     * ------------------
     * Here again, xinetd can log a lot of information when a server can't start, either by lack of resources or because of access rules:
     * HOST, USERID : like above mentioned ;
     * ATTEMPT : logs an access attempt. This an automatic option as soon as another value is provided;
     * RECORD : logs every information available on the client.
     *
     * @param socket
     *
     * @exception ServiceException
     * @exception IOException
     */
    public void service(Socket socket) throws ServiceException, IOException {
        // Fill this in more deeply later.
        InetAddress client = socket.getInetAddress();
        MDC.put("HOST", client.getHostName());
        MDC.put("SERVER", getName());

        try {
            logIncoming();
            next.service(socket);
            logSuccess();
        } catch (Exception e) {
            logFailure(e);
            e.printStackTrace();
        }
    }

    private void logIncoming() {
        log.info("incomming request");
    }

    private void logSuccess() {
        log.info("successful request");
    }

    private void logFailure(Exception e) {
        log.error(e.getMessage());
    }


    public void init(Properties props) throws Exception {
        next.init(props);
    }

    public void start() throws ServiceException {
        next.start();
    }

    public void stop() throws ServiceException {
        next.stop();
    }

    public String getName() {
        return next.getName();
    }

    public String getIP() {
        return next.getIP();
    }

    public int getPort() {
        return next.getPort();
    }

}
