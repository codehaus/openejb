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

import java.rmi.RemoteException;
import java.util.Vector;

import javax.ejb.SessionContext;

import org.openejb.OpenEJBException;
import org.openejb.alt.config.Bean;
import org.openejb.alt.config.ConfigUtils;
import org.openejb.alt.config.EjbJarUtils;
import org.openejb.alt.config.ejb11.EjbDeployment;
import org.openejb.alt.config.ejb11.EjbJar;
import org.openejb.alt.config.ejb11.EjbRef;
import org.openejb.alt.config.ejb11.OpenejbJar;
import org.openejb.alt.config.ejb11.ResourceLink;
import org.openejb.alt.config.ejb11.ResourceRef;
import org.openejb.alt.config.sys.Connector;
import org.openejb.alt.config.sys.Container;
import org.openejb.alt.config.sys.Openejb;
import org.openejb.util.JarUtils;

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
    private StringBuffer deploymentHTML = new StringBuffer();
    private String containerDeployIdsHTML = "";
    private OpenejbJar openejbJar;
    private Vector beanList = new Vector();
    private boolean idsWritten = false;

    /** Creates a new instance of DeployerBean */
    public void ejbCreate() {
        try {
            if (configFile == null) {
                try {
                    configFile = System.getProperty("openejb.configuration");
                } catch (Exception e) {}
            }
            if (configFile == null) {
                configFile = ConfigUtils.searchForConfiguration();
            }
            config = ConfigUtils.readConfig(configFile);

            /* Load container list */
            containers = config.getContainer();

            /* Load resource list */
            resources = config.getConnector();

        } catch (Exception e) {
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
        return deploymentHTML.toString();
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
    }

    /** sets the deployment and container ids */
    public void setDeployAndContainerIds(
        String deploymentId,
        String containerId,
        String[][] resourceRef,
        String[][] ejbRef,
        int i)
        throws RemoteException {
        //local variables
        EjbDeployment deployment = new EjbDeployment();
        ResourceLink link;

        //set the deployment info
        deployment.setEjbName(deployerBeans[i].getEjbName());
        deployment.setDeploymentId(deploymentId);
        deployment.setContainerId(containerId);

        //set the resource references
        for (int j=0; j<ejbRef.length; j++) {
            link = new ResourceLink();
            link.setResId(ejbRef[j][0]);
            link.setResRefName(ejbRef[j][1]);
            deployment.addResourceLink(link);
        }

        //set the ejb references
        for (int j=0; j<resourceRef.length; j++) {
            link = new ResourceLink();
            link.setResId(resourceRef[j][0]);
            link.setResRefName(resourceRef[j][1]);
            deployment.addResourceLink(link);
        }
        
        try {
            openejbJar.addEjbDeployment(deployment);
        } catch (IndexOutOfBoundsException e) {
            throw new RemoteException(e.getMessage());
        }

        deploymentHTML.append("<tr><td colspan=\"2\">Your bean: <b>").append(
            deployerBeans[i].getEjbName());
        deploymentHTML.append("</b> is has been given the id: <b>").append(deploymentId);
        deploymentHTML.append("</b> and has been assigned to the container: <b>").append(
            containerId);
        deploymentHTML.append("</b></td></tr>");
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

    public int getDeployerBeanLength() {
        return deployerBeans.length;
    }
    
    private String autoAssignDeploymentId(Bean bean) {
        return bean.getEjbName();
    }

    private String autoAssignContainerId(Bean bean) throws OpenEJBException {
        Container[] cs = getUsableContainers(bean);
        
        if (cs.length == 0) {
            //we'll fix this later
            throw new OpenEJBException("There are no useable containers for this bean.");
        }
      
        return cs[0].getId();
    }

    private void saveChanges(String jarFile, OpenejbJar openejbJar) throws OpenEJBException {
        ConfigUtils.writeOpenejbJar("META-INF/openejb-jar.xml", openejbJar);
        JarUtils.addFileToJar(jarFile, "META-INF/openejb-jar.xml");

        if (configChanged) {
            ConfigUtils.writeConfig(configFile, config);
        }

        deploymentHTML.append("<tr><td colspan=\"2\">Your bean has been deployed! ");
        deploymentHTML.append("You must restart the OpenEJB server in order for your");
        deploymentHTML.append("bean to be recognized.</td></tr>");
    }

    public String createIdTable() throws OpenEJBException {
        //string that contains all the html
        StringBuffer htmlString = new StringBuffer();
        String deploymentId;
        String containerId;
        Container[] cs;
        ResourceRef[] refs;
        EjbRef[] ejbRefs;

        htmlString.append("<table cellspacing=\"1\" cellpadding=\"2\" border=\"1\">\n");
        htmlString.append("<tr align=\"left\">\n");
        htmlString.append("<th>Bean Name</th>\n");
        htmlString.append("<th>Deployment Id</th>\n");
        htmlString.append("<th>Container Id</th>\n");
        htmlString.append("<th>Resource References</th>\n");
        htmlString.append("</tr>\n");

        for (int i = 0; i < deployerBeans.length; i++) {
            //in here we check to see if we need to write the different parts or not
            htmlString.append("<tr>\n");
            htmlString.append("<td>" + deployerBeans[i].getEjbName() + "</td>\n");

            //deployment id
            if (options[5]) {
                deploymentId = autoAssignDeploymentId(deployerBeans[i]);
                htmlString.append("<td><input type=\"hidden\" name=\"deploymentId").append(
                    i).append(
                    "\" value=\"");
                htmlString.append(deploymentId).append("\">").append(deploymentId).append(
                    "</td>\n");
            } else {
                htmlString.append("<td><input type=\"text\" name=\"deploymentId").append(i);
                htmlString.append("\" size=\"25\" maxlength=\"50\"></td>\n");
            }

            //container id
            if (options[0]) {
                containerId = autoAssignContainerId(deployerBeans[i]);
                htmlString.append("<td><input type=\"hidden\" name=\"containerId").append(
                    i).append(
                    "\" value=\"");
                htmlString.append(containerId).append("\">").append(containerId).append("</td>\n");
            } else {
                htmlString.append("<td><select name=\"containerId").append(i).append("\">\n");
                cs = getUsableContainers(deployerBeans[i]);
                //loop through the continer
                for (int j = 0; j < cs.length; j++) {
                    htmlString.append("<option value=\"").append(cs[j].getId()).append("\">");
                    htmlString.append(cs[j].getId()).append("</option>\n");
                }
                htmlString.append("</select></td>\n");
            }

            //outside references go here - put in a seperate method
            refs = deployerBeans[i].getResourceRef();
            ejbRefs = deployerBeans[i].getEjbRef();

            if ((refs.length > 0) || (ejbRefs.length > 0)) {
                htmlString.append("<td>");
                createIdTableOutsideRef(htmlString, refs, ejbRefs, i);
                htmlString.append("</td>");
            } else {
                htmlString.append("<td>N/A</td>\n");
            }

            htmlString.append("</tr>\n");
        }

        htmlString.append(
            "<tr><td colspan=\"4\"><input type=\"submit\" name=\"submitDeploymentAndContainerIds\"");
        htmlString.append(" value=\"Continue &gt;&gt;\"></td></tr></table>\n");

        return htmlString.toString();
    }

    private void createIdTableOutsideRef(
        StringBuffer htmlString,
        ResourceRef[] refs,
        EjbRef[] ejbRefs,
        int index)
        throws OpenEJBException {

        //this will create the html for outside references
        htmlString.append(
            "<table cellspacing=\"0\" cellpadding=\"2\" border=\"0\" width=\"100%\">\n");
        htmlString.append("<tr align=\"left\">\n");
        htmlString.append("<th>Name</th>\n");
        htmlString.append("<th>Type</th>\n");
        htmlString.append("<th>Id</th>\n");
        htmlString.append("</tr>\n");

        if (refs.length > 0) {
            for (int i = 0; i < refs.length; i++) {
                htmlString.append("<tr>\n");
                htmlString.append("<td>").append(refs[i].getResRefName()).append("</td>\n");
                htmlString.append("<td>").append(refs[i].getResType()).append("</td>\n");
                htmlString.append("<td>\n<select name=\"resourceRefId").append(index).append("\">");

                //loop through the available resources
                for (int j = 0; j < this.resources.length; j++) {
                    htmlString.append("<option value=\"").append(this.resources[j].getId());
                    htmlString.append("\">").append(this.resources[j].getId()).append(
                        "</option>\n");
                }

                htmlString.append("</select>\n");
                htmlString.append("<input type=\"hidden\" name=\"resourceRefName").append(i);
                htmlString.append("\" value=\"").append(refs[i].getResRefName()).append("\">");
                htmlString.append("</td>\n</tr>\n");
            }
        }

        if (ejbRefs.length > 0) {
            String ejbLink;
            for (int i = 0; i < ejbRefs.length; i++) {
                htmlString.append("<tr>\n");
                htmlString.append("<td>").append(ejbRefs[i].getEjbRefName()).append("</td>\n");
                htmlString.append("<td>").append(ejbRefs[i].getEjbRefType()).append("</td>\n");

                //check for an available link
                ejbLink = ejbRefs[i].getEjbLink();
                if (ejbLink == null) {
                    htmlString.append("<td>\n<select name=\"ejbRefId").append(index).append("\">");
                    //loop through the available beans in the jar
                    for (int j = 0; j < deployerBeans.length; j++) {
                        if (!deployerBeans[j].getEjbName().equals(ejbRefs[i].getEjbRefName())) {
                            htmlString.append("<option value=\"").append(
                                deployerBeans[j].getEjbName());
                            htmlString.append("\">").append(deployerBeans[j].getEjbName()).append(
                                "</option>\n");
                        }
                    }

                    htmlString.append("</select>\n");
                } else {
                    htmlString.append("<td><input type=\"hidden\" name=\"ejbRefId").append(index);
                    htmlString.append("\" value=\"").append(ejbLink).append("\">\n").append(ejbLink);
                }

                htmlString.append("<input type=\"hidden\" name=\"ejbRefName").append(i);
                htmlString.append("\" value=\"").append(ejbRefs[i].getEjbRefName()).append("\">");
                htmlString.append("</td>\n</tr>\n");
            }
        }

        htmlString.append("</table>\n");
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

    private String moveJar(String jar) throws OpenEJBException {
        return EjbJarUtils.moveJar(jar, options[2]);
    }

    private String copyJar(String jar) throws OpenEJBException {
        return EjbJarUtils.copyJar(jar, options[2]);
    }

    private void addDeploymentEntryToConfig(String jarLocation) {
        configChanged = ConfigUtils.addDeploymentEntryToConfig(jarLocation, config);
    }

    //api callback methods
    public void ejbActivate() throws javax.ejb.EJBException, java.rmi.RemoteException {}

    public void ejbPassivate() throws javax.ejb.EJBException, java.rmi.RemoteException {}

    public void ejbRemove() throws javax.ejb.EJBException, java.rmi.RemoteException {}

    public void setSessionContext(SessionContext sessionContext)
        throws javax.ejb.EJBException, java.rmi.RemoteException {
        this.context = sessionContext;
    }
}
