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
package org.openejb.admin.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.StringTokenizer;
import java.util.HashMap;
import java.util.Properties;
import java.net.URL;
import java.net.URLEncoder;
import java.net.URLDecoder;
import org.openejb.OpenEJB;
import org.openejb.spi.ContainerSystem;
import org.openejb.DeploymentInfo;
import org.openejb.alt.assembler.classic.*;
import org.openejb.alt.config.ejb11.*;
import org.openejb.alt.config.sys.*;
import org.openejb.alt.config.*;

/**
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 */
public class SystemInfoHttpBean implements HttpBean {
    //////////////////////////////////////////////////////////////////
    // DMB: This is probably too much to stick into one bean.       //
    //      With the new sessionbean idea, we can easily break      //
    //      these things out into different beans.                  //
    //      it would certainly be much easier to manage and update  //
    private static final String CONTAINERS  = "containers";         //
    private static final String CONTAINER   = "container";          //
    private static final String DEPLOYMENTS = "deployments";        //
    private static final String DEPLOYMENT  = "deployment";         //
    private static final String STATUS      = "status";             //
    private static final String SERVER      = "server";             //
    private static final String LOGS        = "logs";               //
    private static final String LOG         = "log";                //
    //////////////////////////////////////////////////////////////////

    private static final String SHOW = "show";
    private static final String LIST = "list";
    protected HashMap deploymentIdIndex;
    protected HashMap containerIdIndex;

    public void ejbActivate(){
        createIndexes();
    }

    public void onMessage(HttpRequest request, HttpResponse response) throws IOException{
        String action = null;
        System.out.println("[] processing");
        
        if (request.getQueryParameter(SHOW) != null) {
            action = request.getQueryParameter(SHOW);

            if ( action.equalsIgnoreCase( CONTAINER ) ) {
                showContainer(request,response);
            } else if ( action.equalsIgnoreCase( DEPLOYMENT ) ) {
                showDeployment(request,response);
            } else if ( action.equalsIgnoreCase( LOG ) ) {
                showLog(request,response);
            } else if ( action.equalsIgnoreCase( SERVER ) ) {
                showServer(request,response);
            } else if ( action.equalsIgnoreCase( STATUS ) ) {
                showStatus(request,response);
            } else {
                showStatus(request,response);
            }
        } else if ( request.getQueryParameter(LIST) != null ){
            action = request.getQueryParameter(LIST);

            if ( action.equalsIgnoreCase( CONTAINERS ) ) {
                listContainers(request,response);
            } else if ( action.equalsIgnoreCase( DEPLOYMENTS ) ) {
                listDeployments(request,response);
            } else if ( action.equalsIgnoreCase( LOGS ) ) {
                listLogs(request,response);
            } else {
                listDeployments(request,response);
            }
        } else {
            showStatus(request,response);
        }
    }

    public void showContainer(HttpRequest req, HttpResponse res) throws IOException {
    }

    public void showDeployment(HttpRequest req, HttpResponse res) throws IOException {
        String id = req.getQueryParameter("id");

        // TODO:0: Inform the user the id is bad
        if (id == null) return;
        EnterpriseBeanInfo bean = getBeanInfo(id);

        // TODO:0: Inform the user the id is bad
        if (bean == null) return;

        // Assuming things are good        
        java.io.PrintWriter body = res.getPrintWriter();
        InputStream template = getTemplate();

        // Write till PAGETITLE
        writeTemplate(body, template);
        body.print("OpenEJB - EJB ["+id+"]");

        // Write till TITLE
        writeTemplate(body, template);
        body.print("EnterpriseBean Details");

        // Write till BODY
        writeTemplate(body, template);

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

        // Write till FOOTER
        writeTemplate(body, template);
        body.print(footer);
        
        // Write the rest
        writeTemplate(body, template);
    }

    protected void printRow(String col1, String col2, PrintWriter out) throws IOException{
        out.println("<tr>"  );
        out.print("<td><span class=\"bodyBlack\">");
        out.print(col1);
        out.println("</span></td>");
        out.print("<td><span class=\"bodyBlack\">");
        out.print(col2);
        out.println("</span></td>");
        out.println("</tr>");
    }

    protected void printRow(String col1, String col2, String col3, PrintWriter out) throws IOException{
        out.println("<tr>");
        out.print("<td><span class=\"bodyBlack\">");
        out.print(col1);
        out.println("</span></td>");
        out.print("<td><span class=\"bodyBlack\">");
        out.print(col2);
        out.println("</span></td>");
        out.print("<td><span class=\"bodyBlack\">");
        out.print(col3);
        out.println("</span></td>");
        out.println("</tr>");
    }

    public void showLog(HttpRequest req, HttpResponse res) throws IOException {
    }

    public void showServer(HttpRequest req, HttpResponse res) throws IOException {
    }

    public void showStatus(HttpRequest req, HttpResponse res) throws IOException {
    }

    public void listContainers(HttpRequest req, HttpResponse res) throws IOException {
    }

    public void listDeployments(HttpRequest req, HttpResponse res) throws IOException {
        java.io.PrintWriter body = res.getPrintWriter();
        System.out.println("[] list deployments");
        InputStream template = getTemplate();
        System.out.println("[] got template");

        // Write till PAGETITLE
        writeTemplate(body, template);
        body.print("OpenEJB - Deployed EnterpriseBeans");

        // Write till TITLE
        writeTemplate(body, template);
        body.print("Deployed EnterpriseBeans");

        // Write till BODY
        writeTemplate(body, template);
        printDeployments(body);
        
        // Write till FOOTER
        writeTemplate(body, template);
        body.print(footer);

        // Write the rest
        writeTemplate(body, template);
    }

    public void listLogs(HttpRequest req, HttpResponse res) throws IOException {
    }

    private void foo(HttpRequest req, HttpResponse res) throws IOException{
        java.io.PrintWriter body = res.getPrintWriter();
        InputStream template = getTemplate();

        // Write till PAGETITLE
        writeTemplate(body, template);

        // Write till TITLE
        writeTemplate(body, template);

        // Write till BODY
        writeTemplate(body, template);
        
        // Write till FOOTER
        writeTemplate(body, template);
        
        // Write the rest
        writeTemplate(body, template);
    }

//
//      logger.info( "startup.banner", versionInfo.get( "url" ), new Date(), versionInfo.get( "copyright" ),
//                   versionInfo.get( "version" ), versionInfo.get( "date" ), versionInfo.get( "time" ) );
//  }
//
//  
//  public void printContainers(PrintWriter out) throws IOException{
//      ContainerSystem sys = OpenEJB.containers()
//      if (containerSystem == null) {
//          logger.fatal( "startup.assemblerReturnedNullContainer" );
//          throw new OpenEJBException( "startup.assemblerReturnedNullContainer" );
//      }
//  
//      if (logger.isDebugEnabled()){
//          logger.debug( "startup.debugContainers", new Integer(containerSystem.containers().length) );
//  
//          if (containerSystem.containers().length > 0) {
//              Container[] c = containerSystem.containers();
//              logger.debug( "startup.debugContainersType" );
//              for (int i=0; i < c.length; i++){
//                  String entry = "   ";
//                  switch ( c[i].getContainerType() ) {
//                  case Container.ENTITY:    entry += "ENTITY      "; break;
//                  case Container.STATEFUL:  entry += "STATEFUL    "; break;
//                  case Container.STATELESS: entry += "STATELESS   "; break;
//                  }
//                  entry += c[i].getContainerID();
//                  logger.debug( "startup.debugEntry", entry) ;
//              }
//          }
//      }
//  }
//

    public void printDeployments(PrintWriter out) throws IOException{
        DeploymentInfo[] deployments = OpenEJB.deployments();
        out.println("<table width=\"100%\" border=\"1\">");
        out.println("<tr bgcolor=\"#5A5CB8\">");
        out.println("<td><font color=\"white\">Deployment ID</font></td>");
        out.println("</tr>");

        if (deployments.length > 0) {
            for (int i=0; i < deployments.length; i++){
                if (i%2 == 1) {
                    out.println("<tr bgcolor=\"#c9c5fe\">");
                } else {
                    out.println("<tr>");
                }
                
                out.print("<td><span class=\"bodyBlack\">");
                out.print("<a href=\"system?show=deployment&id="+deployments[i].getDeploymentID()+"\">");
                out.print(deployments[i].getDeploymentID());
                out.print("</a>");
                out.println("</span></td></tr>");
            }
        }
        out.println("</table>");
    }

    public void showDetails(HttpRequest request, HttpResponse response) throws IOException{
        java.io.PrintWriter out = response.getPrintWriter();
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
    
    protected EnterpriseBeanInfo getBeanInfo(String id){
        return (EnterpriseBeanInfo) deploymentIdIndex.get(id);
    }

    public static final int SUBSTITUTE = 26;

    public void writeTemplate(PrintWriter out, InputStream template) throws IOException{
        int b = template.read();
        System.out.println("[] read");
        while (b != -1 && b != SUBSTITUTE) {
            out.write( b );
            b = template.read();
        }
        System.out.println("[] done reading");
    }

    public InputStream getTemplate() throws IOException{
        System.out.println("[] get template");
        return new URL( "resource:/openejb/webadmin/template.html" ).openConnection().getInputStream();
    }

    protected static String footer = getFooter();

    public static String getFooter(){
        StringBuffer out = new StringBuffer(100);
        try {
            Properties openejbProps = new Properties();
            openejbProps.load( new URL( "resource:/openejb-version.properties" ).openConnection().getInputStream() );
            out.append( "<a href=\""+openejbProps.get("url")+"\">OpenEJB</a> ");
            out.append( openejbProps.get( "version" ) +"<br>");
            out.append( "build: "+openejbProps.get( "date" )+"-"+openejbProps.get( "time" ));

        } catch (java.io.IOException e) {
        }

        return out.toString();
    }

    public void ejbPassivate() throws javax.ejb.EJBException, java.rmi.RemoteException {
    }
    
    public void ejbRemove() throws javax.ejb.EJBException, java.rmi.RemoteException {
    }
    
    public void setSessionContext(javax.ejb.SessionContext sessionContext) throws javax.ejb.EJBException, java.rmi.RemoteException {
    }
    
}