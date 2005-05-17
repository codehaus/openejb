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
 *    please contact openejb@openejb.org.
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
 * Copyright 2001 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.server.xfire;

import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.wsdl.xml.WSDLWriter;

import org.apache.geronimo.core.service.InvocationResult;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.webservices.MessageContextInvocationKey;
import org.apache.geronimo.webservices.SoapHandler;
import org.apache.geronimo.webservices.WebServiceContainer;
import org.codehaus.xfire.MessageContext;
import org.codehaus.xfire.XFireRuntimeException;
import org.codehaus.xfire.fault.Soap11FaultHandler;
import org.codehaus.xfire.fault.XFireFault;
import org.codehaus.xfire.java.DefaultJavaService;
import org.codehaus.xfire.java.Invoker;
import org.codehaus.xfire.java.JavaServiceHandler;
import org.codehaus.xfire.soap.Soap11;
import org.openejb.EJBContainer;
import org.openejb.EJBInterfaceType;
import org.openejb.EJBInvocation;
import org.openejb.EJBInvocationImpl;
import org.openejb.proxy.ProxyInfo;

public class WSContainer implements Invoker, WebServiceContainer, GBeanLifecycle {

    private final EJBContainer ejbContainer;
    private final URI location;
    private final URL wsdlURL;
    private final DefaultJavaService service;
    private final SoapHandler soapHandler;
    private final Byte wsdlMutext = new Byte((byte)0);

    private transient WSDLWriter wsdlWriter;
    private transient Definition definition;

    protected WSContainer() {
        this.ejbContainer = null;
        this.location = null;
        this.wsdlURL = null;
        this.service = null;
        this.soapHandler = null;
    }

    public WSContainer(EJBContainer ejbContainer,
                       Definition definition,
                       URI location,
                       URL wsdlURL,
                       String namespace,
                       String encoding,
                       String style,
                       SoapHandler soapHandler,
                       String securityRealmName,
                       String realmName,
                       String transportGuarantee,
                       String authMethod) throws Exception {
        this.ejbContainer = ejbContainer;
        this.location = location;
        this.wsdlURL = wsdlURL;

        ProxyInfo proxyInfo = ejbContainer.getProxyInfo();
        Class serviceEndpointInterface = proxyInfo.getServiceEndpointInterface();

        service = new DefaultJavaService();
        service.setName(ejbContainer.getEjbName());
        service.setDefaultNamespace(namespace);
        service.setServiceClass(serviceEndpointInterface);
        service.setSoapVersion(Soap11.getInstance());
        service.setStyle(style);
        service.setUse(encoding);
        service.setWSDLURL(wsdlURL);
        service.setServiceHandler(new org.codehaus.xfire.handler.SoapHandler(new JavaServiceHandler(this)));
        service.setFaultHandler(new Soap11FaultHandler());

        LightWeightServiceConfigurator configurator = new LightWeightServiceConfigurator(definition, service);
        configurator.configure();
        this.soapHandler = soapHandler;
        if (soapHandler != null) {
            ClassLoader classLoader = ejbContainer.getClassLoader();
            soapHandler.addWebService(location.getPath(), this, securityRealmName, realmName, transportGuarantee, authMethod, classLoader);
        }
    }

    public void invoke(Request request, Response response) throws Exception {
        //  We have to set the context classloader or the StAX API
        //  won't be able to find it's implementation.
        Thread thread = Thread.currentThread();
        ClassLoader originalClassLoader = thread.getContextClassLoader();

        try {
            thread.setContextClassLoader(getClass().getClassLoader());
            MessageContext context = new MessageContext("not-used", null, response.getOutputStream(), null, request.getURI().getPath());
            context.setRequestStream(request.getInputStream());
            org.codehaus.xfire.handler.SoapHandler handler = null;
            try {
                context.setService(service);

                handler = (org.codehaus.xfire.handler.SoapHandler) service.getServiceHandler();
                Thread.currentThread().setContextClassLoader(ejbContainer.getClassLoader());
                handler.invoke(context);
            } catch (Exception e) {
                e.printStackTrace();
                if (e instanceof XFireRuntimeException) {
                    throw (XFireRuntimeException) e;
                } else if (handler != null) {
                    XFireFault fault = XFireFault.createFault(e);
                    handler.handleFault(fault, context);
                } else {
                    throw new XFireRuntimeException("Couldn't process message.", e);
                }
            }
        } finally {
            thread.setContextClassLoader(originalClassLoader);
        }
    }

    public void getWsdl(Request request, Response response) throws Exception {

        // Avoid concurrent modification of the WSDL dom.
        synchronized(wsdlMutext) {

            // Read in the the WSDL in once.
            if( definition == null ) {
                initWSDLDom();
            }

            // Update all the service port soap address elements.
            Map services = definition.getServices();
            for (Iterator iter1 = services.values().iterator(); iter1.hasNext();) {
                Service service = (Service) iter1.next();
                Map ports = service.getPorts();
                for (Iterator iter2 = ports.values().iterator(); iter2.hasNext();) {
                    Port port = (Port) iter2.next();
                    for (Iterator iter3 = port.getExtensibilityElements().iterator(); iter3.hasNext();) {
                        ExtensibilityElement element = (ExtensibilityElement) iter3.next();
                        if (element instanceof SOAPAddress ) {
                            SOAPAddress soapAddress = (SOAPAddress)element;
                            URI realLocation = request.getURI();
                            // We replace the host and port here.
                            URI updated = new URI(
                                    realLocation.getScheme(),
                                    realLocation.getUserInfo(),
                                    realLocation.getHost(),
                                    realLocation.getPort(),
                                    realLocation.getPath(), // Humm is this right?
                                    null,
                                    null);
                            soapAddress.setLocationURI(updated.toString());
                        }
                    }
                }
            }

            // Dump the WSDL dom to the output stream
            OutputStream out = response.getOutputStream();
            wsdlWriter.writeWSDL(definition, out);
            out.close();
        }
    }

    /**
     * @throws Exception
     */
    private void initWSDLDom() throws Exception {
        WSDLFactory wsdlFactory = WSDLFactory.newInstance();
        wsdlWriter = wsdlFactory.newWSDLWriter();
        WSDLReader wsdlReader = wsdlFactory.newWSDLReader();
        definition = wsdlReader.readWSDL(wsdlURL.toExternalForm());
    }

    public Object invoke(Method m, Object[] params, MessageContext context) throws XFireFault {
        try {
            int index = ejbContainer.getMethodIndex(m);
            EJBInvocation invocation = new EJBInvocationImpl(EJBInterfaceType.WEB_SERVICE, null, index, params);
            javax.xml.rpc.handler.MessageContext messageContext = new SimpleMessageContext(new HashMap());
            invocation.put(MessageContextInvocationKey.INSTANCE, messageContext);
            InvocationResult invocationResult = ejbContainer.invoke(invocation);
            if (invocationResult.isException()) {
                throw (Throwable) invocationResult.getException();
            }
            return invocationResult.getResult();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            throw new XFireFault("Error invoking EJB", throwable, XFireFault.RECEIVER);
        }
    }

    public URI getLocation() {
        return location;
    }

    public URL getWsdlURL() {
        return wsdlURL;
    }

    public void doStart() throws Exception {

    }

    public void doStop() throws Exception {
        if (soapHandler != null) {
            soapHandler.removeWebService(location.getPath());
        }
    }

    public void doFail() {
        if (soapHandler != null) {
            soapHandler.removeWebService(location.getPath());
        }
    }

    // TODO Move this into org.openejb.server.soap and delete duplicate from Axis WSContainer
    private static class SimpleMessageContext implements javax.xml.rpc.handler.MessageContext {

        private final Map properties;

        public SimpleMessageContext(Map properties) {
            this.properties = new HashMap(properties);
        }

        public boolean containsProperty(String name) {
            return properties.containsKey(name);
        }

        public Object getProperty(String name) {
            return properties.get(name);
        }

        public Iterator getPropertyNames() {
            return properties.keySet().iterator();
        }

        public void removeProperty(String name) {
            properties.remove(name);
        }

        public void setProperty(String name, Object value) {
            properties.put(name, value);
        }
    }
}
