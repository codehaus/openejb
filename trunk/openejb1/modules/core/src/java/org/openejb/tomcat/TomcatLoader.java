/**
 *
 * Copyright 2005 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.openejb.tomcat;

import org.openejb.OpenEJB;
import org.openejb.loader.Loader;
import org.openejb.loader.SystemInstance;
import org.openejb.server.ServerFederation;
import org.openejb.server.ServiceException;
import org.openejb.server.ejbd.EjbServer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

/**
 * @version $Revision$ $Date$
 */
public class TomcatLoader implements Loader {

    private EjbServer ejbServer;

    public void init(ServletConfig config) throws ServletException {
        // Not thread safe
        if (OpenEJB.isInitialized()) {
            ejbServer = (EjbServer) SystemInstance.get().getObject(this.ejbServer.getClass().getName());
            return;
        }

        Properties p = new Properties();
        p.setProperty("openejb.loader", "tomcat");

        Enumeration enum = config.getInitParameterNames();
        System.out.println("OpenEJB init-params:");
        while (enum.hasMoreElements()) {
            String name = (String) enum.nextElement();
            String value = config.getInitParameter(name);
            p.put(name, value);
            System.out.println("\tparam-name: " + name + ", param-value: " + value);
        }

        String loader = p.getProperty("openejb.loader"); // Default loader set above
        if (loader.endsWith("tomcat-webapp")) {
            ServletContext ctx = config.getServletContext();
            File webInf = new File(ctx.getRealPath("WEB-INF"));
            File webapp = webInf.getParentFile();
            String webappPath = webapp.getAbsolutePath();

            setPropertyIfNUll(p, "openejb.base", webappPath);
            setPropertyIfNUll(p, "openejb.configuration", "META-INF/openejb.xml");
            setPropertyIfNUll(p, "openejb.container.decorators", TomcatJndiSupport.class.getName());
            setPropertyIfNUll(p, "log4j.configuration", "META-INF/log4j.properties");
        }

        try {
            init(p);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void init(Properties props) throws Exception {
        SystemInstance.init(props);
        ejbServer = new EjbServer();
        SystemInstance.get().setObject(ejbServer.getClass().getName(), ejbServer);
        OpenEJB.init(props, new ServerFederation());
        ejbServer.init(props);
    }

    private Object setPropertyIfNUll(Properties properties, String key, String value) {
        String currentValue = properties.getProperty(key);
        if (currentValue == null) {
            properties.setProperty(key, value);
        }
        return currentValue;
    }

    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ServletInputStream in = request.getInputStream();
        ServletOutputStream out = response.getOutputStream();
        try {
            ejbServer.service(in, out);
        } catch (ServiceException e) {
            throw new ServletException("ServerService error: " + ejbServer.getClass().getName() + " -- " + e.getMessage(), e);
        }
    }
}
