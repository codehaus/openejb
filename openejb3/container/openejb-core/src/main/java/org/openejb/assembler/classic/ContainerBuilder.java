package org.openejb.assembler.classic;

import org.apache.xbean.recipe.ConstructionException;
import org.apache.xbean.recipe.ObjectRecipe;
import org.apache.xbean.recipe.StaticRecipe;
import org.openejb.Container;
import org.openejb.OpenEJBException;
import org.openejb.RpcContainer;
import org.openejb.core.DeploymentInfo;
import org.openejb.loader.SystemInstance;
import org.openejb.spi.SecurityService;
import org.openejb.util.Logger;

import javax.transaction.TransactionManager;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

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
        this.decorators = (decorators == null) ? new String[]{} : decorators.split(":");
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
                deployment.setJarPath(ejbJar.jarPath);
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

            Container container = buildContainer(containerInfo, deploymentsList);
            container = wrapContainer(container);

            org.openejb.DeploymentInfo [] deploys = container.deployments();
            for (int x = 0; x < deploys.length; x++) {
                org.openejb.core.DeploymentInfo di = (org.openejb.core.DeploymentInfo) deploys[x];
                di.setContainer(container);
            }
            containers.add(container);
        }
        return containers;
    }

    private Container buildContainer(ContainerInfo containerInfo, HashMap deploymentsList) throws OpenEJBException {
        String containerName = containerInfo.containerName;
        ContainerInfo service = containerInfo;

        Properties systemProperties = System.getProperties();
        synchronized (systemProperties) {
            String userDir = systemProperties.getProperty("user.dir");
            try {
                File base = SystemInstance.get().getBase().getDirectory();
                systemProperties.setProperty("user.dir", base.getAbsolutePath());

                ObjectRecipe containerRecipe = new ObjectRecipe(service.className, service.constructorArgs, null);
                containerRecipe.setAllProperties(service.properties);
                containerRecipe.setProperty("id", new StaticRecipe(containerName));
                containerRecipe.setProperty("transactionManager", new StaticRecipe(props.get(TransactionManager.class.getName())));
                containerRecipe.setProperty("securityService", new StaticRecipe(props.get(SecurityService.class.getName())));
                containerRecipe.setProperty("deployments", new StaticRecipe(deploymentsList));

                return (Container) containerRecipe.create();
            } catch (Exception e) {
                Throwable cause = (e.getCause() != null) ? e.getCause() : e;
                throw new OpenEJBException(AssemblerTool.messages.format("as0002", containerName, cause.getMessage()), cause);
            } finally {
                systemProperties.setProperty("user.dir", userDir);
            }
        }
    }

    private Container wrapContainer(Container container) {
        for (int i = 0; i < decorators.length && container instanceof RpcContainer; i++) {
            try {
                String decoratorName = decorators[i];
                ObjectRecipe decoratorRecipe = new ObjectRecipe(decoratorName,new String[]{"container"}, null);
                decoratorRecipe.setProperty("container", new StaticRecipe(container));
                container = (Container) decoratorRecipe.create();
            } catch (ConstructionException e) {
                logger.error("Container wrapper class " + decorators[i] + " could not be constructed and will be skipped.", e);
            }
        }
        return container;
    }
}
