/* ====================================================================
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce this list of
 *    conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
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
 *    (http://openejb.org/).
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
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the OpenEJB Project.  For more information
 * please see <http://openejb.org/>.
 *
 * ====================================================================
 */
package org.openejb.corba.security.config.tss;

import java.security.Principal;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.security.auth.Subject;
import javax.security.cert.X509Certificate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.omg.CORBA.Any;
import org.omg.CORBA.NO_PERMISSION;
import org.omg.CORBA.ORB;
import org.omg.CORBA.UserException;
import org.omg.CSIIOP.TAG_NULL_TAG;
import org.omg.CSIIOP.TAG_TLS_SEC_TRANS;
import org.omg.CSIIOP.TLS_SEC_TRANS;
import org.omg.CSIIOP.TLS_SEC_TRANSHelper;
import org.omg.CSIIOP.TransportAddress;
import org.omg.IOP.Codec;
import org.omg.IOP.TaggedComponent;

import org.apache.geronimo.security.RealmPrincipal;


/**
 * At the moment, this config class can only handle a single address.
 *
 * @version $Rev: $ $Date$
 */
public class TSSSSLTransportConfig extends TSSTransportMechConfig {

    private final transient Log log = LogFactory.getLog(TSSSSLTransportConfig.class);

    private short port;
    private String hostname;
    private short handshakeTimeout = -1;
    private short supports;
    private short requires;

    public TSSSSLTransportConfig() {
    }

    public TSSSSLTransportConfig(TaggedComponent component, Codec codec) throws UserException {
        Any any = codec.decode_value(component.component_data, TLS_SEC_TRANSHelper.type());
        TLS_SEC_TRANS tst = TLS_SEC_TRANSHelper.extract(any);

        supports = tst.target_supports;
        requires = tst.target_requires;
        port = tst.addresses[0].port;
        hostname = tst.addresses[0].host_name;
    }

    public short getPort() {
        return port;
    }

    public void setPort(short port) {
        this.port = port;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public short getHandshakeTimeout() {
        return handshakeTimeout;
    }

    public void setHandshakeTimeout(short handshakeTimeout) {
        this.handshakeTimeout = handshakeTimeout;
    }

    public short getSupports() {
        return supports;
    }

    public void setSupports(short supports) {
        this.supports = supports;
    }

    public short getRequires() {
        return requires;
    }

    public void setRequires(short requires) {
        this.requires = requires;
    }

    public TaggedComponent encodeIOR(ORB orb, Codec codec) {
        TaggedComponent result = new TaggedComponent();

        TLS_SEC_TRANS tst = new TLS_SEC_TRANS();

        tst.target_supports = supports;
        tst.target_requires = requires;
        tst.addresses = new TransportAddress[1];
        tst.addresses[0] = new TransportAddress(hostname, port);

        try {
            Any any = orb.create_any();
            TLS_SEC_TRANSHelper.insert(any, tst);

            result.tag = TAG_TLS_SEC_TRANS.value;
            result.component_data = codec.encode_value(any);
        } catch (Exception ex) {
            log.error("Error enncoding transport tagged component, defaulting encoding to NULL");

            result.tag = TAG_NULL_TAG.value;
            result.component_data = new byte[0];
        }

        return result;
    }

    public Subject check(SSLSession session) throws NO_PERMISSION {
        if (session == null && requires != 0) throw new NO_PERMISSION("Missing required SSL session");

        try {
            X509Certificate link = session.getPeerCertificateChain()[0];
            Subject subject = new Subject();
            Principal p = link.getSubjectDN();

            subject.getPrincipals().add(p);
            subject.getPrincipals().add(new RealmPrincipal(link.getIssuerDN().toString(), p));

            return subject;
        } catch (SSLPeerUnverifiedException e) {
            return new Subject();
        }
    }

}
