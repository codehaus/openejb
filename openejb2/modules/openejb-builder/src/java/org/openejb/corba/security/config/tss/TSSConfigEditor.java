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
package org.openejb.corba.security.config.tss;

import java.util.Iterator;
import java.util.List;

import org.omg.CSIIOP.CompositeDelegation;
import org.omg.CSIIOP.Confidentiality;
import org.omg.CSIIOP.DetectMisordering;
import org.omg.CSIIOP.DetectReplay;
import org.omg.CSIIOP.EstablishTrustInClient;
import org.omg.CSIIOP.EstablishTrustInTarget;
import org.omg.CSIIOP.Integrity;
import org.omg.CSIIOP.NoDelegation;
import org.omg.CSIIOP.NoProtection;
import org.omg.CSIIOP.SimpleDelegation;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.common.propertyeditor.PropertyEditorException;
import org.apache.geronimo.deployment.service.XmlAttributeBuilder;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.geronimo.security.deploy.DefaultPrincipal;
import org.apache.geronimo.security.deployment.SecurityBuilder;
import org.apache.geronimo.xbeans.geronimo.security.GerDefaultPrincipalType;

import org.openejb.xbeans.csiv2.tss.TSSAssociationOption;
import org.openejb.xbeans.csiv2.tss.TSSCompoundSecMechType;
import org.openejb.xbeans.csiv2.tss.TSSGSSUPType;
import org.openejb.xbeans.csiv2.tss.TSSGeneralNameType;
import org.openejb.xbeans.csiv2.tss.TSSGssExportedNameType;
import org.openejb.xbeans.csiv2.tss.TSSIdentityTokenTypeList;
import org.openejb.xbeans.csiv2.tss.TSSSSLType;
import org.openejb.xbeans.csiv2.tss.TSSSasMechType;
import org.openejb.xbeans.csiv2.tss.TSSTssType;


/**
 * A property editor for {@link org.openejb.corba.security.config.tss.TSSConfig}.
 *
 * @version $Revision$ $Date$
 */
public class TSSConfigEditor implements XmlAttributeBuilder {

    private static final String NAMESPACE = "http://www.openejb.org/xml/ns/corba-tss-config_1_0";

    public String getNamespace() {
        return NAMESPACE;
    }

    /**
     * Returns a TSSConfig object initialized with the input object
     * as an XML string.
     *
     * @return a TSSConfig object
     * @throws org.apache.geronimo.common.propertyeditor.PropertyEditorException
     *          An IOException occured.
     */
    public Object getValue(XmlObject xmlObject, String type, ClassLoader cl) throws DeploymentException {
        TSSTssType tss;
        if (xmlObject instanceof TSSTssType) {
            tss = (TSSTssType) xmlObject;
        } else {
            tss = (TSSTssType) xmlObject.copy().changeType(TSSTssType.type);
        }

        try {
            SchemaConversionUtils.validateDD(tss);
        } catch (XmlException e) {
            throw new DeploymentException(e);
        }

        TSSConfig tssConfig = new TSSConfig();

        tssConfig.setInherit(tss.getInherit());

        if (tss.isSetDefaultPrincipal()) {
            DefaultPrincipal defaultPrincipal = new DefaultPrincipal();
            GerDefaultPrincipalType defaultPrincipalType = tss.getDefaultPrincipal();

            defaultPrincipal.setRealmName(defaultPrincipalType.getRealmName().trim());
            defaultPrincipal.setPrincipal(SecurityBuilder.buildPrincipal(defaultPrincipalType.getPrincipal()));

            tssConfig.setDefaultPrincipal(defaultPrincipal);
        }

        if (tss.isSetSSL()) {
            tssConfig.setTransport_mech(extractSSL(tss.getSSL()));
        } else if (tss.isSetSECIOP()) {
            throw new PropertyEditorException("SECIOP processing not implemented");
        } else {
            tssConfig.setTransport_mech(new TSSNULLTransportConfig());
        }

        if (tss.isSetCompoundSecMechTypeList()) {
            TSSCompoundSecMechListConfig mechListConfig = tssConfig.getMechListConfig();
            mechListConfig.setStateful(tss.getCompoundSecMechTypeList().getStateful());

            TSSCompoundSecMechType[] mechList = tss.getCompoundSecMechTypeList().getCompoundSecMechArray();
            for (int i = 0; i < mechList.length; i++) {
                TSSCompoundSecMechConfig cMech = extractCompoundSecMech(mechList[i]);
                cMech.setTransport_mech(tssConfig.getTransport_mech());
                mechListConfig.add(cMech);
            }
        }

        return tssConfig;
    }

    protected static TSSTransportMechConfig extractSSL(TSSSSLType sslMech) {
        TSSSSLTransportConfig sslConfig = new TSSSSLTransportConfig();

        sslConfig.setHostname(sslMech.getHostname());
        sslConfig.setPort(sslMech.getPort());
        sslConfig.setHandshakeTimeout(sslMech.getHandshakeTimeout());
        sslConfig.setSupports(extractAssociationOptions(sslMech.getSupports()));
        sslConfig.setRequires(extractAssociationOptions(sslMech.getRequires()));

        return sslConfig;
    }

    protected static TSSCompoundSecMechConfig extractCompoundSecMech(TSSCompoundSecMechType mech) {

        TSSCompoundSecMechConfig result = new TSSCompoundSecMechConfig();

        if (mech.isSetGSSUP()) {
            result.setAs_mech(extractASMech(mech.getGSSUP()));
        } else {
            result.setAs_mech(new TSSNULLASMechConfig());
        }

        if (mech.isSetSasMech()) {
            result.setSas_mech(extractSASMech(mech.getSasMech()));
        }

        return result;
    }

    protected static TSSASMechConfig extractASMech(TSSGSSUPType gssupMech) {

        TSSGSSUPMechConfig gssupConfig = new TSSGSSUPMechConfig();

        gssupConfig.setTargetName(gssupMech.getTargetName());
        gssupConfig.setRequired(gssupMech.getRequired());

        return gssupConfig;
    }

    protected static TSSSASMechConfig extractSASMech(TSSSasMechType sasMech) {

        TSSSASMechConfig sasMechConfig = new TSSSASMechConfig();

        if (sasMech.isSetServiceConfigurationList()) {
            sasMechConfig.setRequired(sasMech.getServiceConfigurationList().getRequired());

            TSSGeneralNameType[] generalNames = sasMech.getServiceConfigurationList().getGeneralNameArray();
            for (int i = 0; i < generalNames.length; i++) {
                sasMechConfig.addServiceConfigurationConfig(new TSSGeneralNameConfig(generalNames[i].getPrivilegeAuthority()));
            }

            TSSGssExportedNameType[] exportedNames = sasMech.getServiceConfigurationList().getGssExportedNameArray();
            for (int i = 0; i < exportedNames.length; i++) {
                sasMechConfig.addServiceConfigurationConfig(new TSSGSSExportedNameConfig(exportedNames[i].getPrivilegeAuthority(), exportedNames[i].getOID()));
            }
        }

        TSSIdentityTokenTypeList identityTokenTypes = sasMech.getIdentityTokenTypes();

        if (identityTokenTypes.isSetITTAbsent()) {
            sasMechConfig.addIdentityToken(new TSSITTAbsent());
        } else {
            if (identityTokenTypes.isSetITTAnonymous()) {
                sasMechConfig.addIdentityToken(new TSSITTAnonymous());
            }
            if (identityTokenTypes.isSetITTPrincipalNameGSSUP()) {
                sasMechConfig.addIdentityToken(new TSSITTPrincipalNameGSSUP());
            }
        }

        return sasMechConfig;
    }

    protected static short extractAssociationOptions(List list) {
        short result = 0;

        for (Iterator iter = list.iterator(); iter.hasNext();) {
            TSSAssociationOption.Enum obj = TSSAssociationOption.Enum.forString((String) iter.next());

            if (TSSAssociationOption.NO_PROTECTION.equals(obj)) {
                result |= NoProtection.value;
            } else if (TSSAssociationOption.INTEGRITY.equals(obj)) {
                result |= Integrity.value;
            } else if (TSSAssociationOption.CONFIDENTIALITY.equals(obj)) {
                result |= Confidentiality.value;
            } else if (TSSAssociationOption.DETECT_REPLAY.equals(obj)) {
                result |= DetectReplay.value;
            } else if (TSSAssociationOption.DETECT_MISORDERING.equals(obj)) {
                result |= DetectMisordering.value;
            } else if (TSSAssociationOption.ESTABLISH_TRUST_IN_TARGET.equals(obj)) {
                result |= EstablishTrustInTarget.value;
            } else if (TSSAssociationOption.ESTABLISH_TRUST_IN_CLIENT.equals(obj)) {
                result |= EstablishTrustInClient.value;
            } else if (TSSAssociationOption.NO_DELEGATION.equals(obj)) {
                result |= NoDelegation.value;
            } else if (TSSAssociationOption.SIMPLE_DELEGATION.equals(obj)) {
                result |= SimpleDelegation.value;
            } else if (TSSAssociationOption.COMPOSITE_DELEGATION.equals(obj)) {
                result |= CompositeDelegation.value;
            }
        }
        return result;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = new GBeanInfoBuilder(TSSConfigEditor.class, "XmlAttributeBuilder");
        infoBuilder.addInterface(XmlAttributeBuilder.class);
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
