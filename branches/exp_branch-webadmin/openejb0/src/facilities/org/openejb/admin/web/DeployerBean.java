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
 */
package org.openejb.admin.web;

import javax.ejb.SessionContext;
import org.openejb.alt.config.Bean;
import org.openejb.alt.config.ConfigUtils;
import org.openejb.alt.config.EjbJarUtils;
import org.openejb.alt.config.sys.Openejb;
import org.openejb.alt.config.sys.Container;
import org.openejb.alt.config.sys.Connector;
import org.openejb.alt.config.ejb11.EjbDeployment;
import org.openejb.alt.config.ejb11.EjbJar;
import org.openejb.alt.config.ejb11.OpenejbJar;
import org.openejb.alt.config.ejb11.ResourceLink;
import org.openejb.alt.config.ejb11.ResourceRef;
import org.openejb.OpenEJBException;
import org.openejb.util.Messages;
import org.openejb.util.FileUtils;
import org.openejb.util.JarUtils;
import java.util.Vector;
import java.rmi.RemoteException;

/**
 * This is a stateless session bean which handles the action of deployment for the
 * web administration.
 *
 * TODO:
 * 1. Finish implementing the rest of the features of the command line tool
 * 2. Add better error handling
 * 3. Add documentation
 * 4. Add check to make sure the same id is not being used twice
 *
 *
 * @author <a href="mailto:tim_urberg@yahoo.com">Tim Urberg</a>
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 */
public class DeployerBean implements javax.ejb.SessionBean {
    //private boolean values
    /*  key for boolean values:
     *  AUTO_ASSIGN              0
     *  MOVE_JAR                 1
     *  FORCE_OVERWRITE_JAR      2
     *  COPY_JAR                 3
     *  AUTO_CONFIG              4
     *  GENERATE_DEPLOYMENT_ID   5
     *  GENERATE_STUBS           6
     */
    private boolean[] options = new boolean[7];
    
    //private variables
    private SessionContext context;
    private Openejb config;
    private String configFile = null;
    private boolean configChanged;
    private boolean autoAssign;
    private Container[] containers;
    private Connector[] resources;
    private Bean[] deployerBeans;
    private String jarFile;
    private String deploymentHTML = "";
    private String containerDeployIdsHTML = "";
    private OpenejbJar openejbJar;
    private Vector beanList = new Vector();
    private boolean idsWritten = false;
    
    /** Creates a new instance of DeployerBean */
    public void ejbCreate() {
        try {
            if (configFile == null) {
                try{
                    configFile = System.getProperty("openejb.configuration");
                } catch (Exception e){}
            }
            if (configFile == null) {
                configFile = ConfigUtils.searchForConfiguration();
            }
            config = ConfigUtils.readConfig(configFile);
            
            /* Load container list */
            containers = config.getContainer();

            /* Load resource list */
            resources = config.getConnector();

        } catch ( Exception e ) {
            // TODO: Better exception handling.
            e.printStackTrace();
        }
    }
    
    public void setBooleanValues(boolean[] booleanValues) {
        options = booleanValues;
    }
    
    public boolean[] getBooleanValues() {
        return options;
    }
    
    public void setJarFile(String jar) {
        jarFile = jar;
    }
    
    public String getJarFile() {
        return jarFile;
    }
    
    public String getDeploymentHTML() {
        return deploymentHTML;
    }
    
    /** method which starts the deployment process */
    public void startDeployment() throws RemoteException {
        EjbJar jar = null;
        try { //test for invalid file
            jar = EjbJarUtils.readEjbJar(this.jarFile);
        } catch (OpenEJBException oe) {
            throw new RemoteException(this.jarFile + " is not a valid jar file. ");
        }
        
        openejbJar = new OpenejbJar();
        deployerBeans = getBeans(jar);
            
        //list the bean names
        this.listBeanNames();
    }

    /** this will automate the deployment process */
    public void automateDeployment() throws RemoteException {
        ResourceRef[] refList = null;
        boolean hasReferences = false;
        EjbDeployment deployment = new EjbDeployment();
        
        deploymentHTML += "<tr><td colspan=\"2\">Adding beans to the deployment...</td></tr>";

        try {
            //loop through the deployerBeans
            for(int i=0; i<deployerBeans.length; i++) {
                //add all the variables to the deployment
                deployment.setEjbName(deployerBeans[i].getEjbName());
                deployment.setDeploymentId(autoAssignDeploymentId(deployerBeans[i]));
                deployment.setContainerId(autoAssignContainerId(deployerBeans[i]));
                refList = deployerBeans[i].getResourceRef();
                if(refList.length > 0) {
                    //here we need to print out a list of references
                    hasReferences = true;
                }
                openejbJar.addEjbDeployment(deployment);
            }
        } catch (OpenEJBException e) {
            throw new RemoteException(e.getMessage());
        }
    }
    
    /** sets the deployment and container ids */
    public void setDeployAndContainerIds(String deploymentId, String containerId, int index) 
    throws RemoteException {
        EjbDeployment deployment = new EjbDeployment();
        
        deployment.setEjbName(deployerBeans[index].getEjbName());
        deployment.setDeploymentId(deploymentId);
        deployment.setContainerId(containerId);
        
        try {
            openejbJar.addEjbDeployment(deployment);
        } catch (IndexOutOfBoundsException e) {
            throw new RemoteException(e.getMessage());
        }
        
        deploymentHTML += "<tr><td colspan=\"2\">Your bean: <b>" + deployerBeans[index].getEjbName() + "</b> is has been given the id: <b>" +
                       deploymentId + "</b> and has been assigned to the container: <b>" + containerId + "</b></td></tr>";
    }
    
    public void finishDeployment() throws RemoteException {
        try {        
            if (options[1]) {
                jarFile = moveJar(jarFile);
            } else if (options[3]) {
                jarFile = copyJar(jarFile);
            }

            /* TODO: Automatically updating the users
            config file might not be desireable for
            some people.  We could make this a 
            configurable option. 
            */
            addDeploymentEntryToConfig(jarFile);
            saveChanges(jarFile, openejbJar);
        } catch (OpenEJBException e) {
            throw new RemoteException(e.getMessage());
        }
    }
    
    /** this will return the deployment and contianer html */
    public String promptForDeploymentAndContainerIds() throws RemoteException {
        try {
            //put a check here to make sure we don't keeping appending onto the string
            if(! idsWritten) {
                //here we want to check to see which options to set
                if(options[5]) {
                    writeDeploymentId(true);
                } else {
                    writeDeploymentId(false);
                }
                if(options[0]) {
                    writeContainerIds(true);
                } else {
                    writeContainerIds(false);
                }
                    
                idsWritten = true;
            }
        } catch (OpenEJBException e) {
            throw new RemoteException(e.getMessage());
        }

        return containerDeployIdsHTML;
    }
    
    public int getDeployerBeanLength() {
        return deployerBeans.length;
    }
    
    private void listBeanNames() {
        for (int i=0; i<deployerBeans.length; i++) {
            deploymentHTML += "<tr><td colspan=\"2\">Now deploying...<b>" + deployerBeans[i].getEjbName() + "</b></td></tr>\n";
        }
    }
    
    /** gets a container id */
    private void writeContainerIds(boolean auto) throws OpenEJBException {
        if(deployerBeans == null) {
            throw new OpenEJBException("The deployerBeans variable is null, please start the deployment process first");
        }
        
        
        //loop through the deployerBeans
        Container[] cs = null;
        for(int i=0; i<deployerBeans.length; i++) {
            if(auto) {
                containerDeployIdsHTML += "<input type=\"hidden\" name=\"containerId" + i + "\" value=\"" + 
                autoAssignContainerId(deployerBeans[i]) + "\">";
            } else {
                containerDeployIdsHTML += "<tr><td>" + deployerBeans[i].getEjbName() + "</td>\n";
                containerDeployIdsHTML += "<td><select name=\"containerId" + i + "\">\n";
                cs = getUsableContainers(deployerBeans[i]);
                //loop through the continer
                for(int j=0; j<cs.length; j++) {
                    containerDeployIdsHTML += "<option value=\"" + cs[j].getId() + "\">" + cs[j].getId() + "</option>\n";
                }
                containerDeployIdsHTML += "</select></td></tr>\n";
            }
        }
    }
    
    /** writes the html for deployment id */
    private void writeDeploymentId(boolean auto) {
        for(int i=0; i<deployerBeans.length; i++) {
            if(auto) {
                containerDeployIdsHTML += "<input type=\"hidden\" name=\"deploymentId" + i + "\" value=\"" + 
                autoAssignDeploymentId(deployerBeans[i]) + "\">";
            } else {
                containerDeployIdsHTML += "<tr><td colspan=\"2\">Please enter a deployment id for " + deployerBeans[i].getEjbName() + ":</td></tr>\n";
                containerDeployIdsHTML += "<tr><td>Deployment Id:</td>\n";
                containerDeployIdsHTML += "<td><input type=\"text\" name=\"deploymentId" + i + "\" size=\"25\" maxlength=\"50\"></td></tr>\n";
            }
        }
    }
    
    private String autoAssignDeploymentId(Bean bean) {
        String deploymentId = null;
        deploymentId = bean.getEjbName();
        deploymentHTML += "<tr><td colspan=\"2\">Your bean was automatically assigned <b>" + deploymentId + 
                          "</b> as its deployment id.</td></tr>";
        
        return deploymentId;
    }
    
    private String autoAssignContainerId(Bean bean) throws OpenEJBException {
        Container[] cs = getUsableContainers(bean);
        String containerId = null;
        if (cs.length == 0) {
            //we'll fix this later
            throw new OpenEJBException("There are no useable containers for this bean.");
        }
        containerId = cs[0].getId();
        deploymentHTML += "<tr><td colspan=\"2\">Your bean was automatically assigned to <b>" + containerId + "</b> as its container id.</td></tr>";
        return containerId;
    }
    
    private void saveChanges(String jarFile, OpenejbJar openejbJar) throws OpenEJBException {
        ConfigUtils.writeOpenejbJar("META-INF/openejb-jar.xml", openejbJar);
        JarUtils.addFileToJar(jarFile, "META-INF/openejb-jar.xml");
  
        if (configChanged) {
            ConfigUtils.writeConfig(configFile,config);
        }
        
        deploymentHTML += "<tr><td colspan=\"2\">Your bean has been deployed! You must restart the OpenEJB server " +
        "in order for your bean to be recognized.</td></tr>"; 
    }
    
    /*------------------------------------------------------*/
    /*    Refactored Methods                                */
    /*------------------------------------------------------*/
    private Bean[] getBeans(EjbJar jar) {
        return EjbJarUtils.getBeans(jar);
    }

    private Container[] getUsableContainers(Bean bean) {
        return EjbJarUtils.getUsableContainers(containers, bean);
    }
    
    private String moveJar(String jar) throws OpenEJBException{
        return EjbJarUtils.moveJar(jar, options[2]);
    }

    private String copyJar(String jar) throws OpenEJBException{
        return EjbJarUtils.copyJar(jar, options[2]);
    }

    private void addDeploymentEntryToConfig(String jarLocation){
        configChanged = ConfigUtils.addDeploymentEntryToConfig(jarLocation, config );
    }
        
    //api callback methods
    public void ejbActivate() throws javax.ejb.EJBException, java.rmi.RemoteException {
    }
    
    public void ejbPassivate() throws javax.ejb.EJBException, java.rmi.RemoteException {
    }
    
    public void ejbRemove() throws javax.ejb.EJBException, java.rmi.RemoteException {
    }
    
    public void setSessionContext(SessionContext sessionContext) throws javax.ejb.EJBException, java.rmi.RemoteException {
        this.context = sessionContext;
    }
}
