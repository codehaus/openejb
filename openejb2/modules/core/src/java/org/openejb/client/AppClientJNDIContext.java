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
package org.openejb.client;

import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.NamingException;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.naming.java.ReadOnlyContext;
import org.apache.geronimo.naming.java.RootContext;
import org.apache.geronimo.kernel.Kernel;
import org.openejb.client.naming.java.javaURLContextFactory;

/**
 * @version $Revision$ $Date$
 */
public class AppClientJNDIContext implements org.apache.geronimo.client.AppClientPlugin {

    private final String host;
    private final int port;

    private Context context;

    public AppClientJNDIContext(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void startClient(ObjectName appClientModuleName, Kernel kernel, ClassLoader classLoader) throws Exception {

        Context context;
        try {
            ServerMetaData serverMetaData = new ServerMetaData(host, port);
            JNDIResponse res = new JNDIResponse(serverMetaData);
            JNDIRequest req = new JNDIRequest(JNDIRequest.JNDI_LOOKUP, appClientModuleName.toString(), "");

            Client.request(req, res, serverMetaData);

            context = (Context) res.getResult();

        } catch (Exception e) {
            NamingException namingException = new NamingException("Unable to retrieve J2EE AppClient's JNDI Context");
            namingException.initCause(e);
            throw namingException;
        }

        if ( context == null ) {
            throw new IllegalStateException("Server returned a null JNDI context");
        }

        RootContext.setComponentContext((ReadOnlyContext) context);

        System.setProperty(Context.URL_PKG_PREFIXES, "org.openejb.client.naming");
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, javaURLContextFactory.class.getName());
    }

    public void stopClient(ObjectName appClientModuleName) throws Exception {
        RootContext.setComponentContext(null);
    }

    public Context getContext() {
        return context;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(AppClientJNDIContext.class);

        infoFactory.addOperation("startClient", new Class[]{ObjectName.class});
        infoFactory.addOperation("stopClient", new Class[]{ObjectName.class});
        infoFactory.addAttribute("host", String.class, true);
        infoFactory.addAttribute("port", int.class, true);
        infoFactory.addAttribute("context", Context.class, false);
        infoFactory.setConstructor(new String[]{"host", "port"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }


    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
