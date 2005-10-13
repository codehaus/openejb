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
 * Copyright 2005 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.corba.security.config.css;

import java.util.Iterator;
import java.util.Set;
import java.util.Collections;
import javax.security.auth.Subject;

import org.omg.CORBA.Any;
import org.omg.CSI.GSS_NT_ExportedNameHelper;
import org.omg.CSI.IdentityToken;
import org.omg.GSSUP.GSSUPMechOID;
import org.omg.IOP.CodecPackage.InvalidTypeForEncoding;

import org.apache.geronimo.security.PrimaryRealmPrincipal;
import org.apache.geronimo.security.RealmPrincipal;
import org.apache.geronimo.security.ContextManager;

import org.openejb.corba.util.Util;


/**
 * @version $Revision$ $Date$
 */
public class CSSSASITTPrincipalNameDynamic implements CSSSASIdentityToken {

    private final String oid;
    private final String domain;

    public CSSSASITTPrincipalNameDynamic(String domain) {
        this(GSSUPMechOID.value.substring(4), domain);
    }

    public CSSSASITTPrincipalNameDynamic(String oid, String domain) {
        this.oid = (oid == null ? GSSUPMechOID.value.substring(4) : oid);
        this.domain = domain;
    }

    /**
     * TODO should also use login domains?
     * @return IdentityToken
     */
    public IdentityToken encodeIdentityToken() {

        IdentityToken token = null;
        RealmPrincipal principal = null;
        Subject subject = ContextManager.getCurrentCaller();
        Set principals;
        if (subject == null) {
            principals = Collections.EMPTY_SET;
        } else {
            principals = subject.getPrincipals(RealmPrincipal.class);
        }

        if (principals.size() != 0) {
            for (Iterator iter = principals.iterator(); iter.hasNext();) {
                RealmPrincipal p = (RealmPrincipal) iter.next();
                if (p.getRealm().equals(domain)) {
                    principal = p;
                    if (p instanceof PrimaryRealmPrincipal) break;
                }
            }

            Any any = Util.getORB().create_any();

            GSS_NT_ExportedNameHelper.insert(any, Util.encodeGSSExportName(oid, principal.getPrincipal().getName()));

            byte[] encoding = null;
            try {
                encoding = Util.getCodec().encode_value(any);
            } catch (InvalidTypeForEncoding itfe) {
                throw new IllegalStateException("Unable to encode principal name '" + principal.getPrincipal().getName() + "' " + itfe);
            }

            token = new IdentityToken();
            token.principal_name(encoding);
        } else {
            token = new IdentityToken();
            token.anonymous(true);
        }

        return token;
    }
}
