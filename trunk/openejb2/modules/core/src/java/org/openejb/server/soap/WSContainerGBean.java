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
package org.openejb.server.soap;

import java.net.URI;
import java.net.URL;
import javax.management.ObjectName;

import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.codehaus.xfire.MessageContext;
import org.openejb.EJBContainer;

public class WSContainerGBean {

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(WSContainer.class);

        infoFactory.addOperation("invoke", new Class[]{MessageContext.class});

        infoFactory.addReference("EJBContainer", EJBContainer.class);
        infoFactory.addAttribute("location", URI.class, true);
        infoFactory.addAttribute("wsdlURL", URL.class, true);
        infoFactory.addAttribute("namespace", String.class, true);
        infoFactory.addAttribute("encoding", String.class, true);
        infoFactory.addAttribute("style", String.class, true);

        infoFactory.setConstructor(new String[]{
            "EJBContainer",
            "location",
            "wsdlURL",
            "namespace",
            "encoding",
            "style"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

    public static ObjectName addGBean(Kernel kernel, String name, ObjectName ejbContainer, URI location, URL wsdlURL, String namespace, String encoding, String style) throws GBeanAlreadyExistsException, GBeanNotFoundException {
        GBeanData gbean = createGBean(name, ejbContainer, location, wsdlURL, namespace, encoding, style);
        kernel.loadGBean(gbean, WSContainer.class.getClassLoader());
        kernel.startGBean(gbean.getName());
        return gbean.getName();
    }

    public static GBeanData createGBean(String name, ObjectName ejbContainer, URI location, URL wsdlURL, String namespace, String encoding, String style) {
        assert ejbContainer != null : "EJBContainer objectname is null";

        ObjectName gbeanName = JMXUtil.getObjectName("openejb:type=WSContainer,name=" + name);

        GBeanData gbean = new GBeanData(gbeanName, WSContainerGBean.GBEAN_INFO);
        gbean.setReferencePattern("EJBContainer", ejbContainer);
        gbean.setAttribute("location", location);
        gbean.setAttribute("wsdlURL", wsdlURL);
        gbean.setAttribute("namespace", namespace);
        gbean.setAttribute("encoding", encoding);
        gbean.setAttribute("style", style);

        return gbean;
    }
}
