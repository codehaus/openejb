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
package org.openejb.corba.security;

import java.util.Set;
import javax.security.auth.Subject;
import javax.security.auth.DestroyFailedException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.omg.CORBA.Any;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.INV_POLICY;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.MARSHAL;
import org.omg.CORBA.ORB;
import org.omg.CORBA.BAD_PARAM;
import org.omg.IOP.CodecPackage.FormatMismatch;
import org.omg.IOP.CodecPackage.InvalidTypeForEncoding;
import org.omg.IOP.CodecPackage.TypeMismatch;
import org.omg.IOP.SecurityAttributeService;
import org.omg.IOP.ServiceContext;
import org.omg.PortableInterceptor.ServerRequestInfo;
import org.omg.PortableInterceptor.ServerRequestInterceptor;

import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.interop.CSI.SASContextBodyHelper;
import org.apache.geronimo.interop.CSI.SASContextBody;
import org.apache.geronimo.interop.CSI.MTEstablishContext;
import org.apache.geronimo.interop.CSI.ContextError;
import org.apache.geronimo.interop.CSI.CompleteEstablishContext;
import org.apache.geronimo.interop.CSI.MTCompleteEstablishContext;
import org.apache.geronimo.interop.CSI.MTContextError;
import org.apache.geronimo.interop.CSI.MTMessageInContext;

import org.openejb.corba.security.config.tss.TSSConfig;
import org.openejb.corba.util.Util;


/**
 * @version $Revision$ $Date$
 */
final class ServerSecurityInterceptor extends LocalObject implements ServerRequestInterceptor {

    private final Log log = LogFactory.getLog(ServerSecurityInterceptor.class);

    private final int subjectSlot;
    private final int replySlot;
    private final Subject defaultSubject;

    public ServerSecurityInterceptor(int subjectSlot, int replySlot, Subject defaultSubject) {
        this.subjectSlot = subjectSlot;
        this.replySlot = replySlot;
        this.defaultSubject = defaultSubject;

        if (defaultSubject != null) ContextManager.registerSubject(defaultSubject);
    }

    public void receive_request(ServerRequestInfo ri) {

        Subject identity = null;
        long contextId = 0;

        try {
            ServerPolicy serverPolicy = (ServerPolicy) ri.get_server_policy(ServerPolicyFactory.POLICY_TYPE);
            if (serverPolicy == null) return;
            TSSConfig tssPolicy = serverPolicy.getConfig();
            if (tssPolicy == null) return;

            ServiceContext serviceContext = ri.get_request_service_context(SecurityAttributeService.value);
            if (serviceContext == null) return;

            Any any = Util.getCodec().decode_value(serviceContext.context_data, SASContextBodyHelper.type());
            SASContextBody contextBody = SASContextBodyHelper.extract(any);

            short msgType = contextBody.discriminator();
            switch (msgType) {
                case MTEstablishContext.value:
                    contextId = contextBody.establish_msg().client_context_id;

                    identity = tssPolicy.check(SSLSessionManager.getSSLSession(ri.request_id()), contextBody.establish_msg());

                    ContextManager.registerSubject(identity);

                    SASReplyManager.setSASReply(ri.request_id(), generateContextEstablished(identity, contextId, false));

                    break;

                case MTCompleteEstablishContext.value:
                    log.error("The CSIv2 TSS is not supposed to receive a CompleteEstablishContext message.");
                    throw new INTERNAL("The CSIv2 TSS is not supposed to receive a CompleteEstablishContext message.");

                case MTContextError.value:
                    log.error("The CSIv2 TSS is not supposed to receive a CompleteEstablishContext message.");
                    throw new INTERNAL("The CSIv2 TSS is not supposed to receive a ContextError message.");

                case MTMessageInContext.value:
                    log.error("The CSIv2 TSS is not supposed to receive a CompleteEstablishContext message.");

                    contextId = contextBody.in_context_msg().client_context_id;
                    throw new SASNoContextException();
            }
        } catch (BAD_PARAM e) {
            identity = defaultSubject;
        } catch (INV_POLICY e) {
            identity = defaultSubject;
        } catch (TypeMismatch tm) {
            log.error("TypeMismatch thrown", tm);
            throw new MARSHAL("TypeMismatch thrown: " + tm);
        } catch (FormatMismatch fm) {
            log.error("FormatMismatch thrown", fm);
            throw new MARSHAL("FormatMismatch thrown: " + fm);
        } catch (SASException e) {
            log.error("SASException", e);
            SASReplyManager.setSASReply(ri.request_id(), generateContextError(e, contextId));
            throw (RuntimeException) e.getCause();
        } catch (Exception e) {
            log.error("Exception", e);
            throw (RuntimeException) e.getCause();
        }

        if (identity != null) {
            ContextManager.setCurrentCaller(identity);
            ContextManager.setNextCaller(identity);

            SubjectManager.setSubject(ri.request_id(), identity);
        }
    }

    public void receive_request_service_contexts(ServerRequestInfo ri) {
    }

    public void send_exception(ServerRequestInfo ri) {
        Subject identity = SubjectManager.clearSubject(ri.request_id());
        if (identity != null && identity != defaultSubject) ContextManager.unregisterSubject(identity);

        insertServiceContext(ri);
    }

    public void send_other(ServerRequestInfo ri) {
    }

    public void send_reply(ServerRequestInfo ri) {
        Subject identity = SubjectManager.clearSubject(ri.request_id());
        if (identity != null && identity != defaultSubject) ContextManager.unregisterSubject(identity);

        insertServiceContext(ri);
    }

    public void destroy() {
        if (defaultSubject != null) ContextManager.unregisterSubject(defaultSubject);
    }

    public String name() {
        return "org.openejb.corba.security.ServerSecurityInterceptor";
    }

    protected SASContextBody generateContextError(SASException e, long contextId) {
        SASContextBody reply = new SASContextBody();

        reply.error_msg(new ContextError(contextId, e.getMajor(), e.getMinor(), e.getErrorToken()));

        return reply;
    }

    protected SASContextBody generateContextEstablished(Subject identity, long contextId, boolean stateful) {
        SASContextBody reply = new SASContextBody();

        byte[] finalContextToken = null;
        Set credentials = identity.getPrivateCredentials(FinalContextToken.class);
        if (!credentials.isEmpty()) {
            try {
                FinalContextToken token = (FinalContextToken) credentials.iterator().next();
                finalContextToken = token.getToken();
                token.destroy();
            } catch (DestroyFailedException e) {
                // do nothing
            }
        }
        if (finalContextToken == null) finalContextToken = new byte[0];
        reply.complete_msg(new CompleteEstablishContext(contextId, stateful, finalContextToken));

        return reply;
    }

    protected void insertServiceContext(ServerRequestInfo ri) {
        try {
            SASContextBody sasContextBody = SASReplyManager.clearSASReply(ri.request_id());
            if (sasContextBody != null) {
                Any any = ORB.init().create_any();
                SASContextBodyHelper.insert(any, sasContextBody);
                ri.add_reply_service_context(new ServiceContext(SecurityAttributeService.value, Util.getCodec().encode_value(any)), true);
            }
        } catch (InvalidTypeForEncoding itfe) {
            log.error("InvalidTypeForEncoding thrown", itfe);
            throw new INTERNAL("InvalidTypeForEncoding thrown: " + itfe);
        }
    }
}
