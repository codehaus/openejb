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
 * 3. The name "Exolab" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of Exoffice Technologies.  For written permission,
 *    please contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Exoffice Technologies. Exolab is a registered
 *    trademark of Exoffice Technologies.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.exolab.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY EXOFFICE TECHNOLOGIES AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * EXOFFICE TECHNOLOGIES OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 1999 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.ui.jedi.openejb11.server;

import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import org.xml.sax.InputSource;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.w3c.dom.Document;
import org.opentools.deployer.plugins.*;
import org.openejb.ui.jedi.openejb11.ejb.MetaDataContainer;
import org.opentools.deployer.xml.DocumentWriter;
import org.opentools.deployer.xml.DTDResolver;

/**
 * The main metadata structure for all OpenEJB server information.  This
 * holds references to all the other metadata structures in the package.
 * It knows how to invoke XMLReader and XMLWriter to save and load the
 * data.
 *
 * @see org.openejb.ui.jedi.openejb11.server.XMLReader
 * @see org.openejb.ui.jedi.openejb11.server.XMLWriter
 *
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 * @version $Revision$
 */
public class OpenEjbMetaData extends MetaData {
    private List containers;
    private List cms;
    private List connectors;
    private ServerMetaData server;
    private SecurityMetaData security;
    private TransactionMetaData trans;

    public OpenEjbMetaData() {
        containers = new LinkedList();
        cms = new LinkedList();
        connectors = new LinkedList();
        server = new ServerMetaData();
        security = new SecurityMetaData();
        trans = new TransactionMetaData();
    }

    public void addContainer(MetaDataContainer cont) {
        containers.add(cont);
    }

    public void removeContainer(MetaDataContainer cont) {
        containers.remove(cont);
    }

    public MetaDataContainer[] getContainers() {
        return (MetaDataContainer[])containers.toArray(new MetaDataContainer[containers.size()]);
    }

    public void addConnectionManager(CMMetaData cm) {
        cms.add(cm);
    }

    public void removeConnectionManager(CMMetaData cm) {
        cms.remove(cm);
    }

    public CMMetaData[] getConnectionManagers() {
        return (CMMetaData[])cms.toArray(new CMMetaData[cms.size()]);
    }

    public void addConnector(ConnectorMetaData con) {
        connectors.add(con);
    }

    public void removeConnector(ConnectorMetaData con) {
        connectors.remove(con);
    }

    public ConnectorMetaData[] getConnectors() {
        return (ConnectorMetaData[])connectors.toArray(new ConnectorMetaData[connectors.size()]);
    }

    public ServerMetaData getServerData() {
        return server;
    }

    public void setServerData(ServerMetaData data) {
        server = data;
    }

    public SecurityMetaData getSecurityData() {
        return security;
    }

    public void setSecurityData(SecurityMetaData data) {
        security = data;
    }

    public TransactionMetaData getTransactionData() {
        return trans;
    }

    public void setTransactionData(TransactionMetaData data) {
        trans = data;
    }

    public String[] loadXML(Reader in) throws LoadException, IOException {
        containers.clear();
        final List warnings = new ArrayList();
        try {
            DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
            fac.setValidating(true);
            DocumentBuilder parser = fac.newDocumentBuilder();
            parser.setEntityResolver(new DTDResolver());
            parser.setErrorHandler(new ErrorHandler() {
                boolean enabled = true;
                public void warning (SAXParseException exception) throws SAXException {
                    if(enabled) {
                        warnings.add("XML WARNING: openejb-config.xml:"+exception.getLineNumber()+","+exception.getColumnNumber()+" "+exception.getMessage());
                        if(exception.getMessage().equals("Valid documents must have a <!DOCTYPE declaration.")) {
                            warnings.add("  Cannot validate deployment descriptor without DOCTYPE");
                            warnings.add("  (you may want to save it from here to add the DOCTYPE).");
                            enabled = false;
                        }
                    }
                }
                public void error (SAXParseException exception) throws SAXException {
                    if(enabled)
                        warnings.add("XML ERROR: openejb-config.xml:"+exception.getLineNumber()+","+exception.getColumnNumber()+" "+exception.getMessage());
                }
                public void fatalError (SAXParseException exception) throws SAXException {
                }
            });
            Document doc = parser.parse(new InputSource(new BufferedReader(in)));
            new XMLReader(this, doc).load();
        } catch(SAXParseException e) {
            System.out.println("XML Exception on line: "+e.getLineNumber()+", col "+e.getColumnNumber());
            e.printStackTrace();
            throw new IOException("XML Error: "+e);
        } catch(Exception e) {
            e.printStackTrace();
            throw new IOException("XML Error: "+e);
        }
        return (String[])warnings.toArray(new String[warnings.size()]);
    }

    public void saveXML(Writer out) throws SaveException, IOException {
        XMLWriter writer = null;
        try {
            DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
            fac.setValidating(true);
            DocumentBuilder builder = fac.newDocumentBuilder();
            builder.setEntityResolver(new DTDResolver());
            Document doc = builder.newDocument();
            writer = new XMLWriter(doc, this);
            writer.save();
            DocumentWriter.writeDocument(doc, out, "-//OpenEJB//DTD OpenEJB Server 1.0//EN", "http://www.openejb.org/openejb-config.dtd");
            out.close();
        } catch(Exception e) {
            e.printStackTrace();
            throw new IOException("XML Error: "+e);
        }
        String warnings[] = writer.getWarnings();
        if(warnings.length > 0) {
            StringBuffer cat = new StringBuffer();
            for(int i=0; i<warnings.length; i++) {
                if(i>0)
                    cat.append('\n');
                cat.append(warnings[i]);
            }
            throw new SaveException(cat.toString());
        }
    }
}
