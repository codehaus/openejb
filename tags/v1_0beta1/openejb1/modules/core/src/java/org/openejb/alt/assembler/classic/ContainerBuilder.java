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
 *    please contact info@openejb.org.
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
 * Copyright 2005 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */

package org.openejb.alt.assembler.classic;

import org.openejb.Container;
import org.openejb.OpenEJBException;
import org.openejb.RpcContainer;
import org.openejb.loader.SystemInstance;
import org.openejb.core.DeploymentInfo;
import org.openejb.util.Logger;
import org.openejb.util.SafeToolkit;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

/**
 * @version $Revision$ $Date$
 */
public class ContainerBuilder {

    private static final Logger logger = Logger.getInstance("OpenEJB", "org.openejb.util.resources");

    private final Properties props;
    private final EjbJarInfo[] ejbJars;
    private final ContainerInfo[] containerInfos;
    private final String[] decorators;


    public ContainerBuilder(ContainerSystemInfo containerSystemInfo, Properties props) {
        this.props = props;
        this.ejbJars = containerSystemInfo.ejbJars;
        this.containerInfos = containerSystemInfo.containers;
        String decorators = props.getProperty("openejb.container.decorators");
        this.decorators = (decorators == null)? new String[]{}: decorators.split(":");
    }

    public Object build() throws OpenEJBException {
        HashMap deployments = new HashMap();
        URL[] jars = new URL[this.ejbJars.length];
        for (int i = 0; i < this.ejbJars.length; i++) {
            try {
                jars[i] = new File(this.ejbJars[i].jarPath).toURL();
            } catch (MalformedURLException e) {
                throw new OpenEJBException(AssemblerTool.messages.format("cl0001", ejbJars[i].jarPath, e.getMessage()));
            }
        }

        ClassLoader classLoader = new URLClassLoader(jars, org.openejb.OpenEJB.class.getClassLoader());

        for (int i = 0; i < this.ejbJars.length; i++) {
            EjbJarInfo ejbJar = this.ejbJars[i];

            EnterpriseBeanInfo[] ejbs = ejbJar.enterpriseBeans;
            for (int j = 0; j < ejbs.length; j++) {
                EnterpriseBeanInfo ejbInfo = ejbs[j];
                EnterpriseBeanBuilder deploymentBuilder = new EnterpriseBeanBuilder(classLoader, ejbInfo);
                DeploymentInfo deployment = (DeploymentInfo) deploymentBuilder.build();
                deployments.put(ejbInfo.ejbDeploymentId, deployment);
            }
        }

        List containers = new ArrayList();
        for (int i = 0; i < containerInfos.length; i++) {
            ContainerInfo containerInfo = containerInfos[i];

            HashMap deploymentsList = new HashMap();
            for (int z = 0; z < containerInfo.ejbeans.length; z++) {
                String ejbDeploymentId = containerInfo.ejbeans[z].ejbDeploymentId;
                DeploymentInfo deployment = (DeploymentInfo) deployments.get(ejbDeploymentId);
                deploymentsList.put(ejbDeploymentId, deployment);
            }

            containers.add(buildContainer(containerInfo, deploymentsList));
        }
        return containers;
    }

    private Container buildContainer(ContainerInfo containerInfo, HashMap deploymentsList) throws OpenEJBException {
        String className = containerInfo.className;
        String codebase = containerInfo.codebase;
        String containerName = containerInfo.containerName;

        try {
            Class factory = SafeToolkit.loadClass(className, codebase);
            if (!Container.class.isAssignableFrom(factory)) {
                throw new OpenEJBException(AssemblerTool.messages.format("init.0100", "Container", containerName, factory.getName(), Container.class.getName()));
            }

            Properties clonedProps = (Properties) (props.clone());
            clonedProps.putAll(containerInfo.properties);

            Container container = (Container) factory.newInstance();

            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            for (int i = 0; i < decorators.length && container instanceof RpcContainer; i++) {
                try {
                    String decoratorName = decorators[i];
                    Class decorator = contextClassLoader.loadClass(decoratorName);
                    Constructor constructor = decorator.getConstructor(new Class[]{RpcContainer.class});
                    container = (Container) constructor.newInstance(new Object[]{container});
                } catch (NoSuchMethodException e) {
                    String name = decorators[i].replaceAll(".*\\.", "");
                    logger.error("Container wrapper " + decorators[i] + " does not have the required constructor 'public " + name + "(RpcContainer container)'");
                } catch (InvocationTargetException e) {
                    logger.error("Container wrapper " + decorators[i] + " could not be constructed and will be skipped.  Received message: " + e.getCause().getMessage(), e.getCause());
                } catch (ClassNotFoundException e) {
                    logger.error("Container wrapper class " + decorators[i] + " could not be loaded and will be skipped.");
                }
            }

            Properties systemProperties = System.getProperties();
            synchronized(systemProperties) {
                String userDir = systemProperties.getProperty("user.dir");
                try{
                    File base = SystemInstance.get().getBase().getDirectory();
                    systemProperties.setProperty("user.dir", base.getAbsolutePath());
                    container.init(containerName, deploymentsList, clonedProps);
                } finally {
                    systemProperties.setProperty("user.dir",userDir);
                }
            }

            return container;
        } catch (OpenEJBException e) {
            throw new OpenEJBException(AssemblerTool.messages.format("as0002", containerName, e.getMessage()));
        } catch (InstantiationException e) {
            throw new OpenEJBException(AssemblerTool.messages.format("as0003", containerName, e.getMessage()));
        } catch (IllegalAccessException e) {
            throw new OpenEJBException(AssemblerTool.messages.format("as0003", containerName, e.getMessage()));
        }
    }
}