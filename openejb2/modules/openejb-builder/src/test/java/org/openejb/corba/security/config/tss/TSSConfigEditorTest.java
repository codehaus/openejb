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

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Properties;

import junit.framework.TestCase;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.pool.ThreadPool;

import org.omg.CORBA.SystemException;
import org.openejb.corba.CORBABean;


/**
 * @version $Revision$ $Date$
 */
public class TSSConfigEditorTest extends TestCase {
    private static final String TEST_XML1 = "<foo:tss xmlns:foo=\"http://www.openejb.org/xml/ns/corba-tss-config-2.1\">\n" +
//                                            "                <foo:description>this is a foo</foo:description>" +
                                            "                <foo:SSL port=\"443\" hostname=\"corba.apache.org\">\n" +
                                            "                    <foo:supports>Integrity Confidentiality EstablishTrustInTarget EstablishTrustInClient</foo:supports>\n" +
                                            "                    <foo:requires>Integrity</foo:requires>\n" +
                                            "                </foo:SSL>\n" +
                                            "                <foo:compoundSecMechTypeList>\n" +
                                            "                    <foo:compoundSecMech>\n" +
                                            "                    </foo:compoundSecMech>\n" +
                                            "                </foo:compoundSecMechTypeList>\n" +
                                            "            </foo:tss>";
    private static final String TEST_XML2 = "<foo:tss inherit=\"true\" xmlns:foo=\"http://www.openejb.org/xml/ns/corba-tss-config-2.1\"/>";
    private static final String TEST_XML3 = "<tss xmlns=\"http://www.openejb.org/xml/ns/corba-tss-config-2.1\">\n" +
                                            "                <SSL port=\"443\">\n" +
                                            "                    <supports>BAD_ENUM Integrity Confidentiality EstablishTrustInTarget EstablishTrustInClient</supports>\n" +
                                            "                    <requires>Integrity</requires>\n" +
                                            "                </SSL>\n" +
                                            "                <compoundSecMechTypeList>\n" +
                                            "                    <compoundSecMech>\n" +
                                            "                    </compoundSecMech>\n" +
                                            "                </compoundSecMechTypeList>\n" +
                                            "            </tss>";


    private XmlObject getXmlObject(String xmlString) throws XmlException {
        XmlObject xmlObject = XmlObject.Factory.parse(xmlString);
        XmlCursor xmlCursor = xmlObject.newCursor();
        try {
            xmlCursor.toFirstChild();
            return xmlCursor.getObject();
        } finally {
            xmlCursor.dispose();
        }
    }

    public void testSimple1() throws Exception {
        XmlObject xmlObject = getXmlObject(TEST_XML1);
        TSSConfigEditor editor = new TSSConfigEditor();
        Object o = editor.getValue(xmlObject, null, null);
        TSSConfig tss = (TSSConfig) o;
        assertFalse(tss.isInherit());
        assertNotNull(tss.getTransport_mech());
    }

    public void testSimple2() throws Exception {
        XmlObject xmlObject = getXmlObject(TEST_XML2);
        TSSConfigEditor editor = new TSSConfigEditor();
        TSSConfig tss = (TSSConfig) editor.getValue(xmlObject, null, null);
        assertTrue(tss.isInherit());
        assertNotNull(tss.getTransport_mech());
        assertTrue(tss.getTransport_mech() instanceof TSSNULLTransportConfig);
    }

    public void testSimple3() throws Exception {
        try {
            XmlObject xmlObject = getXmlObject(TEST_XML3);
            TSSConfigEditor editor = new TSSConfigEditor();
            TSSConfig tss = (TSSConfig) editor.getValue(xmlObject, null, null);
            fail("Should fail");
        } catch (DeploymentException e) {
        }

    }


    private static final String propString = "\n" +
                                             "\n" +
                                             "            org.omg.PortableInterceptor.ORBInitializerClass.org.openejb.corba.transaction.TransactionInitializer\n" +
                                             "            org.omg.PortableInterceptor.ORBInitializerClass.org.openejb.corba.security.SecurityInitializer\n" +
                                             "\n" +
                                             "            Xopenorb.debug.level=HIGH\n" +
                                             "            Xopenorb.debug.trace=DEBUG\n" +
                                             "\n" +
                                             "            iiop.TransportServerInitializerClass=org.openorb.orb.ssl.SSLTransportServerInitializer\n" +
                                             "\n" +
                                             "            secure.server.allowUnsecure=false";
    private static final String TEST_XML4 = "            <tss:tss xmlns:tss=\"http://www.openejb.org/xml/ns/corba-tss-config-2.1\" xmlns:sec=\"http://geronimo.apache.org/xml/ns/security-1.2\">\n" +
                                            "                <tss:default-principal>\n" +
                                            "                    <sec:principal class=\"org.apache.geronimo.security.realm.providers.GeronimoUserPrincipal\" name=\"guest\"/>\n" +
                                            "                </tss:default-principal>\n" +
                                            "                <tss:SSL port=\"6685\" hostname=\"localhost\">\n" +
                                            "                    <tss:supports>Integrity Confidentiality EstablishTrustInTarget EstablishTrustInClient</tss:supports>\n" +
                                            "                    <tss:requires>Integrity Confidentiality EstablishTrustInClient</tss:requires>\n" +
                                            "                </tss:SSL>\n" +
                                            "                <tss:compoundSecMechTypeList>\n" +
                                            "                    <tss:compoundSecMech>\n" +
                                            "                        <tss:GSSUP targetName=\"geronimo-properties-realm\"/>\n" +
                                            "                        <tss:sasMech>\n" +
                                            "                            <tss:identityTokenTypes><tss:ITTAnonymous/><tss:ITTPrincipalNameGSSUP principal-class=\"org.apache.geronimo.security.realm.providers.GeronimoUserPrincipal\" domain=\"foo\"/><tss:ITTDistinguishedName domain=\"foo\"/><tss:ITTX509CertChain domain=\"foo\"/></tss:identityTokenTypes>\n" +
                                            "                        </tss:sasMech>\n" +
                                            "                    </tss:compoundSecMech>\n" +
                                            "                </tss:compoundSecMechTypeList>\n" +
                                            "            </tss:tss>";

    public void testCORBABean() throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        ThreadPool threadPool = new ThreadPool(10, "foo", 1000, classLoader, "test:type=ThreadPool");
        String configAdapter = "org.openejb.corba.sunorb.SunORBConfigAdapter";
        CORBABean corbaBean = new CORBABean(configAdapter, classLoader, threadPool, null, null);
        ArrayList args = new ArrayList();
        corbaBean.setArgs(args);
        Properties properties = new Properties();
        properties.load(new ByteArrayInputStream(propString.getBytes()));
        corbaBean.setProps(properties);
        XmlObject xmlObject = getXmlObject(TEST_XML4);
        TSSConfigEditor editor = new TSSConfigEditor();
        Object o = editor.getValue(xmlObject, null, classLoader);
        TSSConfig tss = (TSSConfig) o;

        corbaBean.setTssConfig(tss);

        try {
            corbaBean.doStart();
        } catch(SystemException se) {
            se.printStackTrace();
            fail(se.getCause().getMessage());
        } finally {
            try {
                corbaBean.doStop();
            } catch (Throwable e) {

            }
        }
    }

}
