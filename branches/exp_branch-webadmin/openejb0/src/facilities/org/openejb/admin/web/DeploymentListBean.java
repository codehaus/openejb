/*
 * DeploymentListBean.java
 *
 * Created on November 23, 2002, 1:35 PM
 */

package org.openejb.admin.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Arrays;
import java.net.URLEncoder;
import java.net.URLDecoder;
import org.openejb.OpenEJB;
import org.openejb.DeploymentInfo;
import org.openejb.alt.assembler.classic.EnterpriseBeanInfo;
import org.openejb.alt.assembler.classic.ContainerInfo;
import org.openejb.alt.assembler.classic.JndiEncInfo;
import org.openejb.alt.assembler.classic.EnvEntryInfo;
import org.openejb.alt.assembler.classic.EjbReferenceInfo;
import org.openejb.alt.assembler.classic.ResourceReferenceInfo;
import org.openejb.alt.config.ejb11.EjbDeployment;
import org.openejb.alt.config.sys.Deployments;
import org.openejb.alt.config.ConfigurationFactory;

/**
 *
 * @author  Administrator
 */
public class DeploymentListBean extends WebAdminBean {
    private HashMap deploymentIdIndex;
    private HashMap containerIdIndex;
    
    /** Creates a new instance of DeploymentListBean */
    public void ejbCreate() {
        this.section = "DeploymentList";
    }
    
    /** called after all content is written to the browser
     * @param request the http request
     * @param response the http response
     * @throws IOException if an exception is thrown
     *
     */
    public void postProcess(HttpRequest request, HttpResponse response) throws IOException {
    }
    
    /** called before any content is written to the browser
     * @param request the http request
     * @param response the http response
     * @throws IOException if an exception is thrown
     *
     */
    public void preProcess(HttpRequest request, HttpResponse response) throws IOException {
        createIndexes();
    }
    
    /** writes the main body content to the broswer.  This content is inside a <code>&lt;p&gt;</code> block
     *
     *
     * @param body the output to write to
     * @exception IOException if an exception is thrown
     *
     */
    public void writeBody(PrintWriter body) throws IOException {
        String id = request.getQueryParameter("id");
        if(id != null) {
            showDeployment(body, id);
        } else {
            printDeployments(body);
        }
    }
    
    private void createIndexes(){
        deploymentIdIndex = new HashMap();
        containerIdIndex  = new HashMap();
        ContainerInfo[] cnt = ConfigurationFactory.sys.containerSystem.containers;

        for (int i=0; i < cnt.length; i++){
            containerIdIndex.put(cnt[i].containerName, cnt[i]);
            EnterpriseBeanInfo[] beans = cnt[i].ejbeans;
            for (int x=0; x < beans.length; x++){            
                deploymentIdIndex.put( beans[x].ejbDeploymentId, beans[x] );
            }
        }
    }
    
    private void showDeployment(PrintWriter body, String id) throws IOException {
        EnterpriseBeanInfo bean = getBeanInfo(id);

        // TODO:0: Inform the user the id is bad
        if (bean == null) return;

        // Assuming things are good        
        body = response.getPrintWriter();

        body.println("<h2>General</h2><br>");
        body.println("<table width=\"100%\" border=\"1\">");
        body.println("<tr bgcolor=\"#5A5CB8\">");
        body.println("<td><font face=\"arial\" color=\"white\">ID</font></td>");
        body.println("<td><font color=\"white\">"+id+"</font></td>");
        body.println("</tr>");

        org.openejb.core.DeploymentInfo di = (org.openejb.core.DeploymentInfo)OpenEJB.getDeploymentInfo(id);

        printRow("Name", bean.ejbName, body);
        printRow("Description", bean.description, body);

        String type = null;

        switch ( di.getComponentType() ) {
        case org.openejb.core.DeploymentInfo.CMP_ENTITY:
            type = "EntityBean with Container-Managed Persistence"; break;
        case org.openejb.core.DeploymentInfo.BMP_ENTITY:    
            type = "EntityBean with Bean-Managed Persistence"; break;
        case org.openejb.core.DeploymentInfo.STATEFUL:    
            type = "Stateful SessionBean"; break;
        case org.openejb.core.DeploymentInfo.STATELESS:    
            type = "Stateless SessionBean"; break;
        default:    
            type = "Unkown Bean Type"; break;
        }
        
        printRow("Bean Type", type, body);
        printRow("Bean Class", bean.ejbClass, body);
        printRow("Home Interface", bean.home, body);
        printRow("Remote Interface", bean.remote, body);
        printRow("Jar location", bean.codebase, body);
        
        String container = URLEncoder.encode(""+di.getContainer().getContainerID());
        printRow("Deployed in", container, body);
        
        body.println("</table>");
        body.println("<h2>JNDI Environment Details</h2><br>");
        body.println("<table width=\"100%\" border=\"1\">");
        body.println("<tr bgcolor=\"#5A5CB8\">");
        body.println("<td><font face=\"arial\" color=\"white\">JNDI Name</font></td>");
        body.println("<td><font face=\"arial\" color=\"white\">Value</font></td>");
        body.println("<td><font face=\"arial\" color=\"white\">Type</font></td>");
        body.println("</tr>");

        JndiEncInfo enc = bean.jndiEnc;
        EnvEntryInfo[] envEntries = enc.envEntries;

        for (int i=0; i < envEntries.length; i++){
            EnvEntryInfo e = envEntries[i];
            printRow(e.name, e.value, e.type, body);
        }

        EjbReferenceInfo[] ejbReferences = enc.ejbReferences;
        
        for (int i=0; i < ejbReferences.length; i++){
            EjbReferenceInfo e = ejbReferences[i];
            printRow(e.referenceName, e.location.ejbDeploymentId, e.homeType, body);
        }

        ResourceReferenceInfo[] resourceRefs = enc.resourceRefs;

        for (int i=0; i < resourceRefs.length; i++){
            ResourceReferenceInfo r = resourceRefs[i];
            printRow(r.referenceName, r.resourceID, r.referenceType, body);
        }
        
        body.println("</table>");
    }
    
    
    private EnterpriseBeanInfo getBeanInfo(String id){
        return (EnterpriseBeanInfo) deploymentIdIndex.get(id);
    }

    private void printDeployments(PrintWriter out) throws IOException{
        DeploymentInfo[] deployments = OpenEJB.deployments();
        String[] deploymentString = new String[deployments.length];
        out.println("<table width=\"100%\" border=\"1\">");
        out.println("<tr bgcolor=\"#5A5CB8\">");
        out.println("<td><font color=\"white\">Deployment ID</font></td>");
        out.println("</tr>");

        if (deployments.length > 0) {
            for (int i=0; i < deployments.length; i++){
                deploymentString[i] = (String)deployments[i].getDeploymentID();
            }
            Arrays.sort(deploymentString);
            
            for(int i=0; i<deploymentString.length; i++) {
                if (i%2 == 1) {
                    out.println("<tr bgcolor=\"#c9c5fe\">");
                } else {
                    out.println("<tr>");
                }
                
                out.print("<td><span class=\"bodyBlack\">");
                out.print("<a href=\"DeploymentList?id="+deploymentString[i]+"\">");
                out.print(deploymentString[i]);
                out.print("</a>");
                out.println("</span></td></tr>");
            }
        }
        out.println("</table>");
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
        body.println("EnterpriseBean Details");
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
    public void writeSubMenuItems(PrintWriter body) throws IOException {
    }
    
}
