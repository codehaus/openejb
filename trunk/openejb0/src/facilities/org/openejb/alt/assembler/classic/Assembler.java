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
/**
 * <pre>
 * This method performs the following actions(in order):
 * 
 * 1  Assembles Containers and Deployments
 * 1.1  Assembles StatefulContainer(s)
 * 1.1.1  Assembles Stateful SessionBean Deployments
 * 1.2  Assembles StatelessContainer(s)
 * 1.2.1  Assembles Stateless SessionBean Deployments
 * 1.3  Assembles EntityContainer(s)
 * 1.3.1  Assembles EntityBean Deployments
 * 2  Assembles SecurityService
 * 3  Assembles TransactionService
 * 4  Assembles ConnectionManager(s)
 * 5  Assembles Connector(s)
 * 7  Applies MethodPermission(s)
 * 8  Applies SecurityRole(s)
 * 8  Applies TransactionAttribute(s)
 * 10  Assembles ProxyFactory
 * 11  Assembles bean JNDI namespaces
 * </pre>
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 * @see org.openejb.spi.Assembler
 * @see OpenEjbConfigurationFactory
 */
public class Assembler extends AssemblerTool implements org.openejb.spi.Assembler{
    private org.openejb.core.ContainerSystem containerSystem;
    private TransactionManager transactionManager;
    private org.openejb.spi.SecurityService securityService;
    private HashMap remoteJndiContexts = null;
    
    public org.openejb.spi.ContainerSystem getContainerSystem(){
        return containerSystem;
    }
    public TransactionManager getTransactionManager(){
        return transactionManager;
    }
    public SecurityService getSecurityService(){
        return securityService;
    }

    protected SafeToolkit toolkit = SafeToolkit.getToolkit("Assembler");
    protected Properties props;
    protected OpenEjbConfiguration config;


    //==================================
    // Error messages
    
    private String INVALID_CONNECTION_MANAGER_ERROR = "Invalid connection manager specified for connector identity = ";
    
    // Error messages
    //==================================


    public Assembler(){
    }

    public void init(Properties props) throws OpenEJBException{
        this.props = props;

        /* Get Configuration ////////////////////////////*/
        String className = props.getProperty(EnvProps.CONFIGURATION_FACTORY);
        if ( className == null ) className = props.getProperty("openejb.configurator","org.openejb.alt.config.ConfigurationFactory");
        
        OpenEjbConfigurationFactory configFactory = (OpenEjbConfigurationFactory)toolkit.newInstance(className);
        configFactory.init(props);
        config = configFactory.getOpenEjbConfiguration();
        /*\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*/

        
        /* Add IntraVM JNDI service /////////////////////*/
        String str = System.getProperty(javax.naming.Context.URL_PKG_PREFIXES);
        if(str==null)
            str = ":org.openejb.core.ivm.naming";
        else
            str = "org.openejb.core.ivm.naming:"+str;
        System.setProperty(javax.naming.Context.URL_PKG_PREFIXES, str);
        /*\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*/
    }

    public void build() throws OpenEJBException{
        try{
        containerSystem = buildContainerSystem(config);
        }catch(OpenEJBException ae){
            /* OpenEJBExceptions contain useful information and are debbugable.
             * Let the exception pass through to the top and be logged.
             */
             throw ae;
        }catch(Exception e){
            /* General Exceptions at this level are too generic and difficult to debug.
             * These exceptions are considered unknown bugs and are fatal.
             * If you get an error at this level, please trap and handle the error
             * where it is most relevant.
             */
            OpenEJBErrorHandler.handleUnknownError(e, "Assembler");
            throw new OpenEJBException(e);
        }
    }
    
    /////////////////////////////////////////////////////////////////////
    ////
    ////    Public Methods Used for Assembly
    ////
    /////////////////////////////////////////////////////////////////////

    /**
     * When given a complete OpenEjbConfiguration graph this method,
     * will construct an entire container system and return a reference to that
     * container system, as ContainerSystem instance.
     * 
     * This method leverage the other assemble and apply methods which
     * can be used independently.
     * 
     * Assembles and returns the {@link ContainerSystem} using the
     * information from the {@link OpenEjbConfiguration} object passed in.
     * <pre>
     * This method performs the following actions(in order):
     * 
     * 1  Assembles ProxyFactory
     * 2  Assembles Containers and Deployments
     * 3  Assembles SecurityService
     * 4  Apply method permissions, role refs, and tx attributes
     * 5  Assembles TransactionService
     * 6  Assembles ConnectionManager(s)
     * 7  Assembles Connector(s)
     * </pre>
     * 
     * @param configInfo
     * @return 
     * @exception throws    Exception if there was a problem constructing the ContainerSystem.
     * @exception Exception
     * @see OpenEjbConfiguration
     * @see org.openejb.ContainerSystem
     */
    public org.openejb.core.ContainerSystem buildContainerSystem(OpenEjbConfiguration configInfo)throws Exception{

        /*[1] Assemble ProxyFactory //////////////////////////////////////////
            
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

        /*[2] Assemble Containers and Deployments ///////////////////////////////////*/
        
        assembleContainers(containerSystem,containerSystemInfo);
        /*[2]\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*/

        
        /*[3] Assemble SecurityServices ////////////////////////////////////*/
        securityService = assembleSecurityService(configInfo.facilities.securityService);
        containerSystem.getJNDIContext().bind("java:openejb/SecurityService",securityService);
        
        /*[3]\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*/

        /*[4] Apply method permissions, role refs, and tx attributes ////////////////////////////////////*/

        // roleMapping used later in buildMethodPermissions
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
        if(configInfo.facilities.remoteJndiContexts!=null){
            for(int i = 0; i < configInfo.facilities.remoteJndiContexts.length; i++){
                javax.naming.InitialContext cntx = assembleRemoteJndiContext(configInfo.facilities.remoteJndiContexts[i]);   
                containerSystem.getJNDIContext().bind("java:openejb/remote_jndi_contexts/"+configInfo.facilities.remoteJndiContexts[i].jndiContextId, cntx);
            }
            
        }
        

        /*[5] Assemble TransactionManager /////////////////////////////////*/
        transactionManager = assembleTransactionManager(configInfo.facilities.transactionService);
        containerSystem.getJNDIContext().bind("java:openejb/TransactionManager",transactionManager);
        
        /*[5]\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*/
        
        /*[6] Assemble Connector(s) //////////////////////////////////////////*/
        HashMap connectionManagerMap = new HashMap();
        // connectors are optional in the openejb_config.dtd
        if (configInfo.facilities.connectionManagers != null) {
            for(int i = 0; i < configInfo.facilities.connectionManagers.length;i++){ 
                ConnectionManagerInfo cmInfo = configInfo.facilities.connectionManagers[i];
                ConnectionManager connectionManager = assembleConnectionManager(cmInfo);
                connectionManagerMap.put(cmInfo.connectionManagerId,connectionManager);
            }
        }
        // connectors are optional in the openejb_config.dtd
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
