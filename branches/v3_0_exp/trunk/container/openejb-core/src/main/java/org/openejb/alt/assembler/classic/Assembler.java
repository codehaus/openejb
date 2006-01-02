package org.openejb.alt.assembler.classic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ManagedConnectionFactory;
import javax.transaction.TransactionManager;

import org.openejb.EnvProps;
import org.openejb.OpenEJBException;
import org.openejb.core.ConnectorReference;
import org.openejb.core.DeploymentInfo;
import org.openejb.spi.SecurityService;
import org.openejb.util.OpenEJBErrorHandler;
import org.openejb.util.SafeToolkit;

public class Assembler extends AssemblerTool implements org.openejb.spi.Assembler {
    private org.openejb.core.ContainerSystem containerSystem;
    private TransactionManager transactionManager;
    private org.openejb.spi.SecurityService securityService;
    private HashMap remoteJndiContexts = null;

    public org.openejb.spi.ContainerSystem getContainerSystem() {
        return containerSystem;
    }

    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    public SecurityService getSecurityService() {
        return securityService;
    }

    protected SafeToolkit toolkit = SafeToolkit.getToolkit("Assembler");
    protected OpenEjbConfiguration config;

    private String INVALID_CONNECTION_MANAGER_ERROR = "Invalid connection manager specified for connector identity = ";

    public Assembler() {
    }

    public void init(Properties props) throws OpenEJBException {
        this.props = props;

        /* Get Configuration
        String className = props.getProperty(EnvProps.CONFIGURATION_FACTORY);
        if ( className == null ) className = props.getProperty("openejb.configurator","org.openejb.alt.config.ConfigurationFactory");

        OpenEjbConfigurationFactory configFactory = (OpenEjbConfigurationFactory)toolkit.newInstance(className);
        configFactory.init(props);
        config = configFactory.getOpenEjbConfiguration();
        /*\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*/

        /* Add IntraVM JNDI service
        Properties systemProperties = System.getProperties();
        synchronized(systemProperties){
            String str = systemProperties.getProperty(javax.naming.Context.URL_PKG_PREFIXES);
            String naming = "org.openejb.core.ivm.naming";
            if (str == null) {
                str = naming;
            } else if (str.indexOf(naming) == -1) {
                str = naming + ":" + str;
            }
            systemProperties.setProperty(javax.naming.Context.URL_PKG_PREFIXES, str);
        }
        /*\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*/
    }

    public void build() throws OpenEJBException {
        try {
            containerSystem = buildContainerSystem(config);
        } catch (OpenEJBException ae) {
            /* OpenEJBExceptions contain useful information and are debbugable.
             * Let the exception pass through to the top and be logged.
             */
            throw ae;
        } catch (Exception e) {
            /* General Exceptions at this level are too generic and difficult to debug.
             * These exceptions are considered unknown bugs and are fatal.
             * If you get an error at this level, please trap and handle the error
             * where it is most relevant.
             */
            OpenEJBErrorHandler.handleUnknownError(e, "Assembler");
            throw new OpenEJBException(e);
        }
    }

    public org.openejb.core.ContainerSystem buildContainerSystem(OpenEjbConfiguration configInfo) throws Exception {

        /*[1] Assemble ProxyFactory

            This operation must take place first because of interdependencies.
            As DeploymentInfo objects are registered with the ContainerSystem using the
            ContainerSystem.addDeploymentInfo() method, they are also added to the JNDI
            Naming Service for OpenEJB.  This requires that a proxy for the deployed bean's 
            EJBHome be created. The proxy requires that the default proxy factory is set.
        */

        this.applyProxyFactory(configInfo.facilities.intraVmServer);
        /*[1]\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*/

        ContainerSystemInfo containerSystemInfo = configInfo.containerSystem;

        org.openejb.core.ContainerSystem containerSystem = new org.openejb.core.ContainerSystem();

        /*[2] Assemble Containers and Deployments

        assembleContainers(containerSystem,containerSystemInfo);
        /*[2]\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*/

        /*[3] Assemble SecurityServices
        securityService = assembleSecurityService(configInfo.facilities.securityService);
        containerSystem.getJNDIContext().bind("java:openejb/SecurityService",securityService);

        /*[3]\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*/

        /*[4] Apply method permissions, role refs, and tx attributes

        AssemblerTool.RoleMapping roleMapping = new AssemblerTool.RoleMapping(configInfo.facilities.securityService.roleMappings);
        org.openejb.DeploymentInfo [] deployments = containerSystem.deployments();
        for(int i = 0; i < deployments.length; i++){
            applyMethodPermissions((org.openejb.core.DeploymentInfo)deployments[i], containerSystemInfo.methodPermissions, roleMapping);
            applyTransactionAttributes((org.openejb.core.DeploymentInfo)deployments[i],containerSystemInfo.methodTransactions);
        }

        ArrayList list = new ArrayList();
        if(containerSystemInfo.entityContainers!=null)list.addAll(Arrays.asList(containerSystemInfo.entityContainers));
        if(containerSystemInfo.statefulContainers!=null)list.addAll(Arrays.asList(containerSystemInfo.statefulContainers));
        if(containerSystemInfo.statelessContainers!=null)list.addAll(Arrays.asList(containerSystemInfo.statelessContainers));
        Iterator iterator = list.iterator();
        while(iterator.hasNext()){
            ContainerInfo container = (ContainerInfo)iterator.next();
            for(int z = 0; z < container.ejbeans.length; z++){
                DeploymentInfo deployment= (org.openejb.core.DeploymentInfo)containerSystem.getDeploymentInfo(container.ejbeans[z].ejbDeploymentId);
                applySecurityRoleReference(deployment, container.ejbeans[z], roleMapping);
            }
        }
        /*[4]\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*/
        if (configInfo.facilities.remoteJndiContexts != null) {
            for (int i = 0; i < configInfo.facilities.remoteJndiContexts.length; i++) {
                javax.naming.InitialContext cntx = assembleRemoteJndiContext(configInfo.facilities.remoteJndiContexts[i]);
                containerSystem.getJNDIContext().bind("java:openejb/remote_jndi_contexts/" + configInfo.facilities.remoteJndiContexts[i].jndiContextId, cntx);
            }

        }

        /*[5] Assemble TransactionManager
        transactionManager = assembleTransactionManager(configInfo.facilities.transactionService);
        containerSystem.getJNDIContext().bind("java:openejb/TransactionManager",transactionManager);

        /*[5]\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*/

        /*[6] Assemble Connector(s)
        HashMap connectionManagerMap = new HashMap();

        if (configInfo.facilities.connectionManagers != null) {
            for(int i = 0; i < configInfo.facilities.connectionManagers.length;i++){ 
                ConnectionManagerInfo cmInfo = configInfo.facilities.connectionManagers[i];
                ConnectionManager connectionManager = assembleConnectionManager(cmInfo);
                connectionManagerMap.put(cmInfo.connectionManagerId,connectionManager);
            }
        }

        if (configInfo.facilities.connectors != null) {
            for(int i = 0; i < configInfo.facilities.connectors.length; i++){
                ConnectorInfo conInfo = configInfo.facilities.connectors[i];

                ConnectionManager connectionManager = (ConnectionManager)connectionManagerMap.get(conInfo.connectionManagerId);
                if(connectionManager == null)
                    throw new RuntimeException(INVALID_CONNECTION_MANAGER_ERROR + conInfo.connectorId);

                ManagedConnectionFactory managedConnectionFactory = assembleManagedConnectionFactory(conInfo.managedConnectionFactory);

                ConnectorReference reference = new ConnectorReference(connectionManager, managedConnectionFactory);

                containerSystem.getJNDIContext().bind("java:openejb/connector/"+conInfo.connectorId, reference);
            }
        }
        /*[6]\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*/
        return containerSystem;
    }
}