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

import org.openejb.DeploymentInfo;
import org.openejb.client.EJBRequest;
import org.openejb.util.FastThreadLocal;

/**
 * TODO: Add comment
 *
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 */
public class CallContext {

    /**
     * Hashtable of threads executing in this server
     */
    protected static FastThreadLocal threads = new FastThreadLocal();

    /**
     * The deploymentInfo of the bean executed
     */
    protected DeploymentInfo deploymentInfo;

    /**
     * The EJBRequest object from the client
     */
    protected EJBRequest request;

    /**
     * Constructs a new CallContext
     */
    public CallContext(){
    }

    /**
     * Invalidates the data in this CallContext
     */
    public void reset() {
        deploymentInfo = null;
        request        = null;
    }
    
    /**
     * Returns the DeploymentInfo assigned to this CallContext
     * 
     * @return 
     */
    public DeploymentInfo getDeploymentInfo() {
        return deploymentInfo;
    }
    
    /**
     * Sets the DeploymentInfo assigned to this CallContext
     * 
     * @param info
     */
    public void setDeploymentInfo(DeploymentInfo info) {
        deploymentInfo = info;
    }
    
    /**
     * Returns the EJBRequest this thread is satisfying.
     * 
     * @return 
     */
    public EJBRequest getEJBRequest(){
        return request;
    }
    
    /**
     * Sets the EJBRequest this thread is satisfying.
     * 
     * @param request
     */
    public void setEJBRequest(EJBRequest request){
        this.request = request;
    }
    
    /**
     * Sets the CallContext assigned to the current thread with the CallContext
     * instance passed in
     * 
     * @param ctx
     */
    public static void setCallContext(CallContext ctx) {
        if ( ctx == null ) {
            ctx = (CallContext)threads.get();
            if ( ctx != null ) ctx.reset();
        } else {
            threads.set( ctx );
        }
    }
    
    /**
     * Gets the CallContext assigned to the current thread
     * 
     * @return 
     */
    public static CallContext getCallContext( ) {
        CallContext ctx = (CallContext)threads.get();
        if ( ctx == null ) {
            ctx = new CallContext();
            threads.set( ctx );
        }
        return ctx;
    }
}


