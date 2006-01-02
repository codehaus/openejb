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
 * $Id: Facilities.java,v 1.2 2005/06/19 22:40:28 jlaskowski Exp $
 */

package org.openejb.alt.assembler.classic.xml;


import org.openejb.OpenEJBException;
import org.openejb.alt.assembler.classic.FacilitiesInfo;
import org.w3c.dom.Node;

/**
 * A subclass of FacilitiesInfo filled with data from an XML file.
 * 
 * Populates the member variables of FacilitiesInfo in this classes initializeFromDOM method.
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 * @see org.openejb.alt.assembler.classic.FacilitiesInfo
 */
public class Facilities extends FacilitiesInfo implements DomObject{


    /**
     * Represents the <tt>intra-vm-server</tt> element in the XML config file.
     */
    public static final String INTRA_VM_SERVER = "intra-vm-server";
    
    /**
     * Represents the <tt>remote-jndi-contexts</tt> element in the XML config file.
     */
    public static final String REMOTE_JNDI_CONTEXTS = "remote-jndi-contexts";
    
    /**
     * Represents the <tt>jndi-context</tt> element in the XML config file.
     */
    public static final String JNDI_CONTEXT = "jndi-context";
    
    
    /**
     * Represents the <tt>connectors</tt> element in the XML config file.
     */
    public static final String CONNECTORS = "connectors";
    
    /**
     * Represents the <tt>connector</tt> element in the XML config file.
     */
    public static final String CONNECTOR = "connector";
    /**
     * Represents the <tt>connection-manager</tt> element in the XML config file.
     */
    public static final String CONNECTION_MANAGER = "connection-manager";
    /**
     * Represents the <tt>nodes</tt> element in the XML config file.
     */
    public static final String NODES = "nodes";

    /**
     * Represents the <tt>services</tt> element in the XML config file.
     */
    public static final String SERVICES = "services";


    /**
     * Represents the <tt>security-service</tt> element in the XML config file.
     */
    public static final String SECURITY_SERVICE = "security-service";

    /**
     * Represents the <tt>transaction-service</tt> element in the XML config file.
     */
    public static final String TRANSACTION_SERVICE = "transaction-service";


    /** 
     * Parses out the values needed by this DomObject from the DOM Node passed in.
     * @see org.w3c.dom.Node
     */
    public void initializeFromDOM(Node node) throws OpenEJBException{
        
        /* IntraVmServer ////////////////*/
        intraVmServer = (IntraVmServer) DomTools.collectChildElementByType(node, IntraVmServer.class, INTRA_VM_SERVER);
        /*\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*/

        /* Jndi Contexts ////////////////////*/
        Node jndiContextsElement = DomTools.getChildElement(node, REMOTE_JNDI_CONTEXTS);
        if(jndiContextsElement !=null){
        DomObject[] dos = DomTools.collectChildElementsByType(jndiContextsElement, JndiContext.class, JNDI_CONTEXT);
        remoteJndiContexts = new JndiContext[dos.length];
        for (int i=0; i < dos.length; i++) remoteJndiContexts[i] = (JndiContext)dos[i];
        }
        /*\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*/


        /* Connector ////////////////////*/
        Node connectorsElement = DomTools.getChildElement(node, CONNECTORS);
        if(connectorsElement != null){
        DomObject[] dos = DomTools.collectChildElementsByType(connectorsElement, Connector.class, CONNECTOR);
        connectors = new Connector[dos.length];
        for (int i=0; i < dos.length; i++) connectors[i] = (Connector)dos[i];
        }
        /*\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*/
                                           
        
        /* ConnectionManager ////////////*/
        if(connectorsElement != null){
        DomObject[] dos = DomTools.collectChildElementsByType(connectorsElement, ConnectionManager.class, CONNECTION_MANAGER);
        connectionManagers = new ConnectionManager[dos.length];
        for (int i=0; i < dos.length; i++) connectionManagers[i] = (ConnectionManager)dos[i];
        }
        /*\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*/
        


        /* SecurityService //////////////*/
        Node servicesElement = DomTools.getChildElement(node, SERVICES);
        securityService = (SecurityService)DomTools.collectChildElementByType(servicesElement, SecurityService.class, SECURITY_SERVICE);
        /*\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*/

        
        /* TransactionService ///////////*/
        transactionService = (TransactionService)DomTools.collectChildElementByType(servicesElement, TransactionService.class, TRANSACTION_SERVICE);
        /*\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*/

    
    }
    
    public void serializeToDOM(Node node) throws OpenEJBException{}
}