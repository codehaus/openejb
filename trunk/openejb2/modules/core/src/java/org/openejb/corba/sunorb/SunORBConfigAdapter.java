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
package org.openejb.corba.sunorb;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.omg.CSIIOP.EstablishTrustInClient;
import org.omg.Security.Confidentiality;
import org.omg.Security.EstablishTrustInTarget;
import org.omg.Security.NoProtection;

import org.apache.geronimo.security.deploy.DefaultPrincipal;

import org.openejb.corba.security.config.ConfigAdapter;
import org.openejb.corba.security.config.ConfigException;
import org.openejb.corba.security.config.css.CSSCompoundSecMechConfig;
import org.openejb.corba.security.config.css.CSSCompoundSecMechListConfig;
import org.openejb.corba.security.config.css.CSSConfig;
import org.openejb.corba.security.config.tss.TSSConfig;
import org.openejb.corba.security.config.tss.TSSSSLTransportConfig;
import org.openejb.corba.security.config.tss.TSSTransportMechConfig;


/**
 * @version $Revision$ $Date$
 */
public class SunORBConfigAdapter implements ConfigAdapter {

    public String[] translateToArgs(TSSConfig config, List args) throws ConfigException {
        ArrayList list = new ArrayList();

        list.addAll(args);

        DefaultPrincipal principal = config.getDefaultPrincipal();
        if (principal != null) {
            list.add("default-principal::" + principal.getRealmName() + ":" + principal.getPrincipal().getClassName() + ":" + principal.getPrincipal().getPrincipalName());
        }

        return (String[]) list.toArray(new String[list.size()]);
    }

    public Properties translateToProps(TSSConfig config) throws ConfigException {
        Properties props = new Properties();

        String supProp = "";
        String reqProp = "";
        if (config != null) {
            TSSTransportMechConfig transportMech = config.getTransport_mech();
            if (transportMech != null) {
                if (transportMech instanceof TSSSSLTransportConfig) {
                    TSSSSLTransportConfig sslConfig = (TSSSSLTransportConfig) transportMech;
                    short supports = sslConfig.getSupports();
                    short requires = sslConfig.getRequires();
                    supProp = "Integrity";
                    reqProp = "Integrity";

                    props.put("com.sun.CORBA.connection.ORBListenSocket", "IIOP_SSL:" + Short.toString(sslConfig.getPort()));

                    if ((supports & NoProtection.value) != 0) {
                        supProp += ",NoProtection";
                    }
                    if ((supports & Confidentiality.value) != 0) {
                        supProp += ",Confidentiality";

                        if ((requires & Confidentiality.value) != 0) {
                            reqProp += ",Confidentiality";
                        }
                    }
                    if ((supports & EstablishTrustInClient.value) != 0) {
                        supProp += ",EstablishTrustInClient";

                        if ((requires & EstablishTrustInClient.value) != 0) {
                            reqProp += ",EstablishTrustInClient";
                        }
                    }

                }
            }
        }
        System.setProperty("org.openejb.corba.ssl.SocketProperties.supports", supProp);
        System.setProperty("org.openejb.corba.ssl.SocketProperties.requires", reqProp);

        props.put("com.sun.CORBA.connection.ORBSocketFactoryClass", "org.openejb.corba.sunorb.OpenEJBSocketFactory");
        props.put("org.omg.PortableInterceptor.ORBInitializerClass.org.openejb.corba.transaction.TransactionInitializer", "");
        props.put("org.omg.PortableInterceptor.ORBInitializerClass.org.openejb.corba.security.SecurityInitializer", "");
        props.put("org.omg.PortableInterceptor.ORBInitializerClass.org.openejb.corba.sunorb.SunORBInitializer", "");

        return props;
    }

    public String[] translateToArgs(CSSConfig config, List args) throws ConfigException {
        return (String[]) args.toArray(new String[args.size()]);
    }

    public Properties translateToProps(CSSConfig config) throws ConfigException {
        Properties props = new Properties();

        String supProp = "";
        String reqProp = "";
        if (config != null) {
            short supports = 0;
            short requires = 0;
            CSSCompoundSecMechListConfig mechList = config.getMechList();
            for (int i = 0; i < mechList.size(); i++) {
                CSSCompoundSecMechConfig mech = mechList.mechAt(i);

                supports |= mech.getTransport_mech().getSupports();
                requires |= mech.getTransport_mech().getRequires();
            }

            supProp = "Integrity";
            reqProp = "Integrity";
            if ((supports & NoProtection.value) != 0) {
                supProp += ",NoProtection";
            }
            if ((supports & Confidentiality.value) != 0) {
                supProp += ",Confidentiality";

                if ((requires & Confidentiality.value) != 0) {
                    reqProp += ",Confidentiality";
                }
            }
            if ((supports & EstablishTrustInTarget.value) != 0) {
                supProp += ",EstablishTrustInTarget";

                if ((requires & EstablishTrustInTarget.value) != 0) {
                    reqProp += ",EstablishTrustInTarget";
                }
            }

        } else {
            supProp = "NoProtection";
            reqProp = "NoProtection";
        }
        System.setProperty("org.openejb.corba.ssl.SocketProperties.supports", supProp);
        System.setProperty("org.openejb.corba.ssl.SocketProperties.requires", reqProp);

        props.put("com.sun.CORBA.connection.ORBSocketFactoryClass", "org.openejb.corba.sunorb.OpenEJBSocketFactory");
        props.put("org.omg.PortableInterceptor.ORBInitializerClass.org.openejb.corba.transaction.TransactionInitializer", "");
        props.put("org.omg.PortableInterceptor.ORBInitializerClass.org.openejb.corba.security.SecurityInitializer", "");
        props.put("org.omg.PortableInterceptor.ORBInitializerClass.org.openejb.corba.sunorb.SunORBInitializer", "");

        return props;
    }
}