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


import java.io.*;
import java.net.*;
import java.util.*;
import org.openejb.client.*;
import org.openejb.util.Messages;
import org.openejb.util.Logger;

import org.openejb.OpenEJB;
import org.openejb.spi.SecurityService;
import org.openejb.DeploymentInfo;
import org.openejb.RpcContainer;
import java.rmi.RemoteException;

/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 */
public abstract class EjbMethod implements ResponseCodes, RequestMethods {

    static Messages messages = new Messages( "org.openejb.server.ejbd" );
    // TODO: Get the logger from the ServerManager/ or context
    Logger logger = Logger.getInstance( "OpenEJB.server.remote", "org.openejb.server.ejbd" );

    public void process(EJBRequest req, EJBResponse res){
        CallContext  call = null;
        DeploymentInfo di = null;
        RpcContainer    c = null;;

        try {
            di = getDeployment(req);
        } catch (RemoteException e) {
            String msg = messages.format("unkown.deployment");
            RemoteException re = new RemoteException(msg, e);
            res.setResponse(EJB_ERROR, re);
            return;
        } catch ( Throwable t ) {
            String msg = messages.format("get.deployment.error");
            RemoteException re = new RemoteException(msg, t);
            res.setResponse(EJB_ERROR, re);
            return;
        }

        try {
            call = CallContext.getCallContext();
            call.setEJBRequest( req );
            call.setDeploymentInfo( di );
        } catch ( Throwable t ) {
            String msg = messages.format("set.context.error");
            RemoteException re = new RemoteException(msg, t);
            res.setResponse(EJB_ERROR, re);
            return;
        }

        //logger.info( "EJB REQUEST : "+req );

        try {
            invoke(req,res);
        } catch (org.openejb.InvalidateReferenceException e) {
            res.setResponse(EJB_SYS_EXCEPTION, e.getRootCause());
        } catch (org.openejb.ApplicationException e) {
            res.setResponse(EJB_APP_EXCEPTION, e.getRootCause());
        } catch (org.openejb.SystemException e) {
            res.setResponse(EJB_ERROR, e.getRootCause());
            // TODO:2: This means a severe error occured in OpenEJB
            // we should restart the container system or take other
            // aggressive actions to attempt recovery.
            String msg = messages.format("container.error", e,req.toString());
            logger.fatal(msg,e);
        } finally {
            call.reset();
        }
    }

    public abstract void invoke(EJBRequest req, EJBResponse res) throws Exception;

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


}
    
