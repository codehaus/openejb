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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.ejb.Handle;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

/**
 * This class takes care of deploying a bean in the web administration. 
 *
 * TODO:
 * 1. Add better error handling
 * 2. Use multiple file names for handle file (ie deploy23049.obj, deploy503930.obj) 
 *    and then delete them when finished
 * 3. Make sure we are removing the deployer object when finished to free resources
 *    as well as deleting the handle file
 * 4. Finish implementing the writeForm function
 * 5. Add documentation
 *
 * @author  <a href="mailto:tim_urberg@yahoo.com">Tim Urberg</a>
 */
public class DeployBean extends WebAdminBean {
    private static final String HANDLE_FILE =
        System.getProperty("openejb.home") + System.getProperty("file.separator") + "handle.obj";
    private DeployerObject deployer = null;

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

    /** Creates a new instance of DeployBean */
    public void ejbCreate() {
        this.section = "Deployment";
    }

    /** called after all content is written to the browser
     * @param request the http request
     * @param response the http response
     * @throws IOException if an exception is thrown
     *
     */
    public void postProcess(HttpRequest request, HttpResponse response) throws IOException {}

    /** called before any content is written to the browser
     * @param request the http request
     * @param response the http response
     * @throws IOException if an exception is thrown
     *
     */
    public void preProcess(HttpRequest request, HttpResponse response) throws IOException {}

    /** writes the main body content to the broswer.  This content is inside a <code>&lt;p&gt;</code> block
     *
     *
     * @param body the output to write to
     * @exception IOException if an exception is thrown
     *
     */
    public void writeBody(PrintWriter body) throws IOException {
        String deploy = request.getFormParameter("deploy");
        String submitDeployment = request.getFormParameter("submitDeploymentAndContainerIds");

        try {
            //the user has hit the deploy button
            if (deploy != null) {
                getDeployerHandle();
                setOptions();
                deployer.startDeployment();
                body.print(deployer.createIdTable());
                //more information is needed from the user
            } else if (submitDeployment != null) {
                deployPartTwo(body);
            } else {
                createDeployerHandle();
                writeForm(body);
            }
        } catch (Exception e) {
            body.println(e.getMessage());
        }
    }

    private void deployPartTwo(PrintWriter body) throws Exception {
        String deploymentId;
        String containerId;
        String[][] resourceRef = null;
        String[][] ejbRef = null;
        String tempEjbRef;
        String tempResourceRef;
        String tempEjbName;
        String tempResourceName;
        StringTokenizer ejbRefToken;
        StringTokenizer resourceRefToken;

        int deployerBeansLength = deployer.getDeployerBeanLength();

        //here we need to continue the deployment
        getDeployerHandle(); //gets the deployment handle
        //loop through all the beans and set the ids
        for (int i = 0; i < deployerBeansLength; i++) {
            deploymentId = request.getFormParameter("deploymentId" + i);
            containerId = request.getFormParameter("containerId" + i);
            tempResourceRef = request.getFormParameter("resourceRefId" + i);
            tempResourceName = request.getFormParameter("resourceRefName" + i);
            tempEjbRef = request.getFormParameter("ejbRefId" + i);
            tempEjbName = request.getFormParameter("ejbRefName" + i);

            //resolve the ejb references
            if (tempEjbRef != null) {
                resolveHtmlTokens(tempEjbRef, tempEjbName, ejbRef);
            }

            //resolve the ejb references
            if (tempResourceRef != null) {
                resolveHtmlTokens(tempResourceRef, tempResourceName, resourceRef);
            }

            //check for null
            if (deploymentId == null) {
                throw new IOException("Please enter a deployment id");
            }
            //this should never happen, but better safe than sorry
            if (containerId == null) {
                throw new IOException("Please enter a container id");
            }

            deployer.setDeployAndContainerIds(deploymentId, containerId, resourceRef, ejbRef, i);
        }

        printDeploymentHtml(body);
    }

    private void resolveHtmlTokens(
        String referenceId,
        String referenceName,
        String refString[][]) {
        StringTokenizer refToken = new StringTokenizer(referenceId, ",");
        refString = new String[refToken.countTokens()][2];

        int i = 0;
        while (refToken.hasMoreTokens()) {
            refString[i++][0] = refToken.nextToken();
        }

        refToken = new StringTokenizer(referenceName, ",");

        i = 0;
        while (refToken.hasMoreTokens()) {
            refString[i++][1] = refToken.nextToken();
        }
    }

    private void printDeploymentHtml(PrintWriter body) throws Exception {
        deployer.finishDeployment();
        body.println("<table border=\"0\" cellspacing=\"2\" cellpadding=\"2\">");
        body.println(deployer.getDeploymentHTML());
        body.println("</table>");
        //deployer.remove();
    }

    /** gets an object reference and handle */
    private void createDeployerHandle() throws Exception {
        Properties p = new Properties();
        p.put(Context.INITIAL_CONTEXT_FACTORY, "org.openejb.core.ivm.naming.InitContextFactory");

        //lookup the bean
        InitialContext ctx = new InitialContext(p);
        Object obj = ctx.lookup("deploy/webadmin/Deployer");
        //create a new instance
        DeployerHome home = (DeployerHome) PortableRemoteObject.narrow(obj, DeployerHome.class);
        deployer = home.create();
        //execute a method to create the object
        String temp = deployer.getDeploymentHTML();

        //get the handle for that instance
        Handle deployerHandle = deployer.getHandle();

        //write the handle out to a file
        File myHandleFile = new File(HANDLE_FILE);
        if (!myHandleFile.exists()) {
            myHandleFile.createNewFile();
        }

        //System.out.println("deployerHandle: " + deployerHandle);
        ObjectOutputStream objectOut = new ObjectOutputStream(new FileOutputStream(myHandleFile));
        objectOut.writeObject(deployerHandle); //writes the handle to the file
        objectOut.flush();
        objectOut.close();
    }

    /** this function gets the deployer handle */
    private void getDeployerHandle() throws Exception {
        File myHandleFile = new File(HANDLE_FILE);

        //get the object
        ObjectInputStream objectIn = new ObjectInputStream(new FileInputStream(myHandleFile));
        //get the handle
        Handle deployerHandle = (Handle) objectIn.readObject();
        this.deployer = (DeployerObject) deployerHandle.getEJBObject();
    }

    /** starts the deployment process */
    private void setOptions() throws Exception {
        //the the form values
        String jarFile = request.getFormParameter("jarFile");
        String moveType = request.getFormParameter("moveType");
        String containerId = request.getFormParameter("assignC");
        String deploymentId = request.getFormParameter("assignD");
        String automate = request.getFormParameter("automate");
        String force = request.getFormParameter("force");
        String configFile = request.getFormParameter("configFile");
        String homeDir = request.getFormParameter("homeDir");
        String log4JFile = request.getFormParameter("log4JFile");
        File testForValidFile = null;

        if (jarFile == null) {
            //do this for now, needs better exception handling
            throw new IOException("No jar file was provided, please try again.");
        }
        //set the jar file
        this.deployer.setJarFile(jarFile);

        //copy or move the jar file
        if (moveType.equals("-c")) {
            options[3] = true;
        } else if (moveType.equals("-m")) {
            options[1] = true;
        }
        //set container id
        if (containerId != null) {
            options[0] = true;
        }
        //set deployment id
        if (deploymentId != null) {
            options[5] = true;
        }
        //automate deployment
        if (automate != null) {
            options[0] = true;
            options[5] = true;
        }
        //force overwrite
        if (force != null) {
            options[2] = true;
        }
        //set the openejb config file
        if (!configFile.trim().equals("")) {
            //first check to make sure it's a file, a check to
            //make sure it's a valid xml file will come later
            testForValidFile = new File(configFile);
            if (!testForValidFile.isFile())
                throw new IOException("OpenEJB configuration: " + configFile + " is not a file.");

            System.setProperty("openejb.configuration", configFile);
        }
        //set the OPENEJB_HOME directory
        if (!homeDir.trim().equals("")) {
            //check for valid directory
            testForValidFile = new File(homeDir);
            if (!testForValidFile.isDirectory())
                throw new IOException("OPENEJB_HOME: " + homeDir + " is not a directory.");

            System.setProperty("openejb.home", homeDir);
        }
        //set the log4j configuration
        if (!log4JFile.trim().equals("")) {
            //check for valid file
            testForValidFile = new File(log4JFile);
            if (!testForValidFile.isFile())
                throw new IOException("OpenEJB configuration: " + configFile + " is not a file.");
        }

        testForValidFile = null;
        this.deployer.setBooleanValues(options);
    }

    /** writes the form for this page 
     *
     * TODO - finish the sections that are not implemented
     */
    private void writeForm(PrintWriter body) throws IOException {
        //the form decleration
        body.println(
            "<form action=\"Deployment\" method=\"post\" onsubmit=\"return checkDeploy(this)\">");
        //the start table
        body.println("<table border=\"0\" cellspacing=\"0\" cellpadding=\"2\">");

        //info about CMP mapping - not yet implemented
        /*body.println("<tr>");
        body.println("<td colspan=\"2\">");
        body.println("<strong>Important Note:</strong> If you are deploying a Container Managed Persistance");
        body.println("bean, you must first <a href=\"\">map the fields</a> and then deploy.  Once that step is completed");
        body.println("you will be sent to this page and your configuration files will be set up for you.");
        body.println("(see the help section for more information).");
        body.println("</td>");
        body.println("</tr>");
        body.println("<tr>");
        body.println("<td colspan=\"2\">&nbsp;</td>");
        body.println("</tr>"); */

        //info about step 1
        body.println("<tr>");
        body.println("<td colspan=\"2\">");
        //body.println("<strong>Step 1:</strong> Copy the full path to your bean into the form field below and then click the \"Add Jar\"");
        //body.println("button to add them to the list of jar files to be deployed (if you want to deploy only one");
        //body.println("jar, you don't need to add it to the jar list).");
        body.println(
            "<strong>Step 1:</strong> Copy the full path to your bean into the form field below.");
        body.println("</td>");
        body.println("</tr>");
        body.println("<tr>");
        body.println("<td colspan=\"2\">&nbsp;</td>");
        body.println("</tr>");

        //the file upload for the jar file (this may need to be changed)
        body.println("<tr>");
        body.println("<td><nobr>Jar File</nobr></td>");
        body.println(
            "<td><input type=\"text\" name=\"jarFile\" size=\"35\" maxlength=\"100\"></td>");
        body.println("</tr>");

        /* multiple jar combo box, not yet implemented - we may or may not implement this
        body.println("<tr valign=\"top\">");
        body.println("<td><nobr>Jar List</nobr></td>");
        body.println("<td>");
        body.println("<select name=\"jarList\" size=\"5\">");
        body.println("<option>");
        body.println("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
        body.println("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
        body.println("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
        body.println("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
        body.println("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
        body.println("</option>");
        body.println("</select>");
        body.println("</td>");
        body.println("</tr>"); */
        /* button for adding a jar to the combo box (also not yet implemented)
        body.println("<tr>");
        body.println("<td>&nbsp;</td>");
        body.println("<td><input type=\"button\" name=\"addJar\" value=\"Add Jar\"></td>");
        body.println("</tr>"); */
        body.println("<tr>");
        body.println("<td colspan=\"2\">&nbsp;</td>");
        body.println("</tr>");

        /***************************
         * Deployment options
         ***************************/
        body.println("<tr>");
        body.println("<td colspan=\"2\">");
        body.println("<strong>Step 2:</strong> Choose options for deployment.");
        body.println("</td>");
        body.println("</tr>");
        body.println("<tr>");
        body.println("<td colspan=\"2\">&nbsp;</td>");
        body.println("</tr>");

        //move, copy or leave jar where it's at
        body.println("<tr>");
        body.println(
            "<td colspan=\"2\">Move or copy the jar file(s) to the OPENEJB_HOME/beans directory</td>");
        body.println("</tr>");
        body.println("<tr>");
        body.println("<td colspan=\"2\">");
        body.println("<input type=\"radio\" name=\"moveType\" value=\"-c\" checked>Copy Jar");
        body.println("<input type=\"radio\" name=\"moveType\" value=\"-m\">Move Jar");
        body.println("<input type=\"radio\" name=\"moveType\" value=\"\">Leave Jar Where it is");
        body.println("</td>");
        body.println("</tr>");

        //automate deployment
        body.println("<tr>");
        body.println("<td colspan=\"2\">");
        body.println("<input type=\"checkbox\" name=\"automate\" value=\"-a\">");
        body.println(
            "Automate deployment as much as possible. (the equivalent of checking the next two check boxes)");
        body.println("</td>");
        body.println("</tr>");

        //assign the bean to the first containter
        body.println("<tr>");
        body.println("<td colspan=\"2\">");
        body.println("<input type=\"checkbox\" name=\"assignC\" value=\"-C\">");
        body.println(
            "Automatically assign each bean in the jar to the first container of the appropriate bean type.");
        body.println("</td>");
        body.println("</tr>");

        //assigns a deployment id
        body.println("<tr>");
        body.println("<td colspan=\"2\">");
        body.println("<input type=\"checkbox\" name=\"assignD\" value=\"-D\">");
        body.println("Automatically assign the OpenEJB deployment ID");
        body.println("for each bean by using the &lt;ejb-name&gt; in your");
        body.println("ejb-jar.xml.  The deployment ID uniquely identifies");
        body.println("the bean in the OpenEJB container system and is used");
        body.println("by most servers as the client-side JNDI name.  No");
        body.println("two beans can share the same deployment ID.");
        body.println("</td>");
        body.println("</tr>");

        //force over write of the bean
        body.println("<tr>");
        body.println("<td colspan=\"2\">");
        body.println("<input type=\"checkbox\" name=\"force\" value=\"-f\">");
        body.println(
            "Forces a move or a copy, overwriting any previously existing jar with the same name.");
        body.println("</td>");
        body.println("</tr>");
        body.println("<tr>");
        body.println("<td colspan=\"2\">&nbsp;</td>");
        body.println("</tr>");

        // sets the OpenEJB configuration file 
        body.println("<tr>");
        body.println(
            "<td colspan=\"2\">Sets the OpenEJB configuration to the specified file. (leave blank for non-use)</td>");
        body.println("</tr>");
        body.println("<tr>");
        body.println("<td><nobr>Config File</nobr></td>");
        body.println(
            "<td><input type=\"text\" name=\"configFile\" size=\"35\" maxlength=\"75\"></td>");
        body.println("</tr>");
        body.println("<tr>");
        body.println("<td colspan=\"2\">&nbsp;</td>");
        body.println("</tr>");

        // sets the openejb home env variable 
        body.println("<tr>");
        body.println(
            "<td colspan=\"2\">Set the OPENEJB_HOME to the specified directory. (leave blank for non-use)</td>");
        body.println("</tr>");
        body.println("<tr>");
        body.println("<td><nobr>OPENEJB_HOME:</nobr></td>");
        body.println(
            "<td><input type=\"text\" name=\"homeDir\" size=\"35\" maxlength=\"75\"></td>");
        body.println("</tr>");
        body.println("<tr>");
        body.println("<td colspan=\"2\">&nbsp;</td>");
        body.println("</tr>");

        // sets the log4j configuration file 
        body.println("<tr>");
        body.println(
            "<td colspan=\"2\">Set the log4j configuration to the specified file. (leave blank for non-use)</td>");
        body.println("</tr>");
        body.println("<tr>");
        body.println("<td><nobr>Log4J File</nobr></td>");
        body.println(
            "<td><input type=\"text\" name=\"log4JFile\" size=\"35\" maxlength=\"75\"></td>");
        body.println("</tr>");
        body.println("<tr>");
        body.println("<td colspan=\"2\">&nbsp;</td>");
        body.println("</tr>");

        //deploy the bean
        body.println("<tr>");
        body.println("<td colspan=\"2\">");
        body.println("<strong>Step 3:</strong> Deploy the bean.");
        body.println("</td>");
        body.println("</tr>");
        body.println("<tr>");
        body.println("<td colspan=\"2\">");
        body.println("<input type=\"submit\" name=\"deploy\" value=\"Deploy\">");
        body.println("</td>");
        body.println("</tr>");

        //the end...
        /* we don't have help yet
        body.println("<tr>");
        body.println("<td colspan=\"2\">Note: see the help section for examples on how to deploy beans.</td>");
        body.println("</tr>"); */
        body.println("</table>");
        //the handle file name
        body.println("<input type=\"hidden\" name=\"handleFile\" value=\"\">");
        body.println("</form>");
    }

    /** Write the TITLE of the HTML document.  This is the part
     * that goes into the <HEAD><TITLE></TITLE></HEAD> tags
     *
     * @param body the output to write to
     * @exception IOException of an exception is thrown
     *
     */
    public void writeHtmlTitle(PrintWriter body) throws IOException {
        body.println(HTML_TITLE);
    }

    /** Write the title of the page.  This is displayed right
     * above the main block of content.
     *
     * @param body the output to write to
     * @exception IOException if an exception is thrown
     *
     */
    public void writePageTitle(PrintWriter body) throws IOException {
        body.println("EJB Deployment");
    }

    /** Write the sub items for this bean in the left navigation bar of
     * the page.  This should look somthing like the one below:
     *
     *      <tr>
     *       <td valign="top" align="left">
     *        <a href="system?show=deployments"><span class="subMenuOff">
     *        &nbsp;&nbsp;&nbsp;Deployments
     *        </span>
     *        </a></td>
     *      </tr>
     *
     * Alternately, the bean can use the method formatSubMenuItem(..) which
     * will create HTML like the one above
     *
     * @param body the output to write to
     * @exception IOException if an exception is thrown
     *
     */
    public void writeSubMenuItems(PrintWriter body) throws IOException {}
}