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


import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;

import org.openejb.client.EJBRequest;
import org.openejb.client.EJBResponse;
import org.openejb.util.Messages;
import org.openejb.util.Logger;

/**
 * 
 * TODO: Make this the super class of all the EjbMethods
 * 
 * TODO: Make process an abstract method
 * 
 * TODO: Make EjbMethod into RequestHandler and move all request
 * handlers into the same spot in EJBDaemon
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 */
public class EjbRequestHandler implements RequestHandler {
    
    Messages messages = new Messages( "org.openejb.server.ejbd" );
    
    // TODO: Get the logger from the ServerManager/ or context
    Logger logger = Logger.getInstance( "OpenEJB.server.remote", "org.openejb.server.ejbd" );

    static EjbMethod[] methods;

    static {
        methods = new EjbMethod[25];
        methods[EJB_HOME_CREATE]            = new EjbHome_CREATE();
        methods[EJB_HOME_FIND]              = new EjbHome_FIND();
        methods[EJB_HOME_GET_EJB_META_DATA] = new EjbHome_GET_EJB_META_DATA();
        methods[EJB_HOME_GET_HOME_HANDLE]   = new EjbHome_GET_HOME_HANDLE();
        methods[EJB_HOME_REMOVE_BY_HANDLE]  = new EjbHome_REMOVE_BY_HANDLE();
        methods[EJB_HOME_REMOVE_BY_PKEY]    = new EjbHome_REMOVE_BY_PKEY();
        methods[EJB_OBJECT_BUSINESS_METHOD] = new EjbObject_BUSINESS_METHOD();
        methods[EJB_OBJECT_GET_EJB_HOME]    = new EjbObject_GET_EJB_HOME();
        methods[EJB_OBJECT_GET_HANDLE]      = new EjbObject_GET_HANDLE();
        methods[EJB_OBJECT_GET_PRIMARY_KEY] = new EjbObject_GET_PRIMARY_KEY();
        methods[EJB_OBJECT_IS_IDENTICAL]    = new EjbObject_IS_IDENTICAL();
        methods[EJB_OBJECT_REMOVE]          = new EjbObject_REMOVE();
    }

    private EjbMethod getMethod(EJBRequest req) {
        int id = req.getRequestMethod();

        EjbMethod method = null;
        try {
            return method[id];
        } catch (Exception e) {
            return new UnknownRequest();
        }
    }

    public void processRequest(ObjectInputStream in, ObjectOutputStream out) {

        EJBRequest req = new EJBRequest();
        EJBResponse res = new EJBResponse();

        EjbMethod ejbMethod = null;

        // TODO:2: This method can throw a large number of exceptions, we should
        // be prepared to handle them all.  Look in the ObejctOutputStream code
        // for a full list of the exceptions thrown.
        // java.io.WriteAbortedException  can be thrown containing a
        //
        try {
            req.readExternal( in );

            ejbMethod = getEjbMethod(req);
        
        } catch (Throwable t) {
            ejbMethod = new BadRequest(t);
        }

        try {
            ejbMethod.process(req, res);
        } catch (java.lang.Throwable t) {
            String msg = messages.format("unknown.error", t.getMessage());
            RemoteException re = new RemoteException(msg,t);
            
            res.setResponse(EJB_ERROR, re);
        } finally {
            //logger.info( "EJB RESPONSE: "+res );
            writeResponse(res,out);
        }

    }

    private void writeResponse(EJBResponse res, ObjectOutputStream out){
        try {
            res.writeExternal(out);
        } catch (java.io.IOException ie) {
            String msg = messages.format("response.error",ie.getMessage());
            //logger.error(msg, ie);
        }
    }

    class UnknownRequest extends EjbMethod {
        
        public void process(EJBRequest req, EJBResponse res) {
            String msg = messages.format("unknown.request",req.getRequestMethod());
            //logger.error(msg, error);
            RemoteException re = new RemoteException(msg);
            
            res.setResponse(EJB_ERROR, re);
        }
        public void invoke(EJBRequest req, EJBResponse res) throws Exception {
        }
    }
    
    class BadRequest extends EjbMethod {
        Throwable error;

        BadRequest(Throwable err){
            this.error = error;
        }

        public void process(EJBRequest req, EJBResponse res) {
            String msg = messages.format("bad.request", error.getMessage());
            //logger.error(msg, error);
            RemoteException re = new RemoteException(msg, error);
            
            res.setResponse(EJB_ERROR, re);
        }
        
        public void invoke(EJBRequest req, EJBResponse res) throws Exception {}
    }
}
