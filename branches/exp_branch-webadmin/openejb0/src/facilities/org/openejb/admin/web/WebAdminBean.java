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
import javax.naming.Context;
import javax.ejb.*;
import javax.naming.*;

/**
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 */
public abstract class WebAdminBean implements HttpBean {
    
    protected SessionContext ejbContext;
    public static final int SUBSTITUTE = 26;
    
    public static String[] navSections;
    
    protected String section = "";
    protected HttpRequest request;
    protected HttpResponse response;

    public void onMessage(HttpRequest request, HttpResponse response) throws IOException{
        this.request = request;
        this.response = response;

        preProcess(request, response);
        
        // Assuming things are good        
        java.io.PrintWriter body = response.getPrintWriter();
        InputStream template = getTemplate();

        // Write till PAGETITLE
        writeTemplate(body, template);
        writeHtmlTitle(body);
        
        // Write till TOP_NAV_BAR
        writeTemplate(body, template);
        writeTopNavBar(body);
        
        // Write till LEFT_NAV_BAR
        writeTemplate(body, template);
        writeLeftNavBar(body);

        // Write till TITLE
        writeTemplate(body, template);
        writePageTitle(body);
        
        // Write till BODY
        writeTemplate(body, template);
        writeBody(body);


        // Write till FOOTER
        writeTemplate(body, template);
        writeFooter(body);
        
        // Write the rest
        writeTemplate(body, template);
        postProcess(request, response);
    }

    public abstract void preProcess(HttpRequest request, HttpResponse response) throws IOException;
    public abstract void postProcess(HttpRequest request, HttpResponse response) throws IOException;
    
    /**
     * Write the TITLE of the HTML document.  This is the part
     * that goes into the <HEAD><TITLE></TITLE></HEAD> tags
     * 
     * @param body
     * @exception IOException
     */
    public abstract void writeHtmlTitle(PrintWriter body) throws IOException;

    /**
     * Write the title of the page.  This is displayed right
     * above the main block of content.
     * 
     * @param body
     * @exception IOException
     */
    public abstract void writePageTitle(PrintWriter body) throws IOException;

    /**
     * Write the top navigation bar of the page. This should look somthing
     * like the one below:
     * 
     *     <a href="system?show=server">
     *     <span class="menuTopOff">Remote Server</span>
     *     </a> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
     *     <a href="system?show=containers">
     *     <span class="menuTopOff">Containers</span>
     *     </a> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
     *     <a href="system?show=deployments">
     *     <span class="menuTopOff">Deployments</span>
     *     </a> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
     *     <a href="system?show=logs">
     *     <span class="menuTopOff">Logs</span>
     *     </a> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
     * 
     * @param body
     * @exception IOException
     */
    public void writeTopNavBar(PrintWriter body) throws IOException{
        for (int i=0; i < navSections.length; i+=2){
            body.print("<a href=\"");
            body.print(navSections[i]);
            body.print("\"><span class=\"menuTopOff\">");
            body.print(navSections[i+1]);
            body.print("</span></a> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
        }
    }
    
    /**
     * Write the left navigation bar of the page.  This should look somthing
     * like the one below:
     * 
     *      <tr>
     *       <td valign="top" align="left">
     *        <span class="subMenuOn">
     *        Admin
     *        </span>
     *       </td>
     *      </tr>
     *      <tr>
     *       <td valign="top" align="left">
     *        <a href="system?show=status"><span class="subMenuOff">
     *        &nbsp;&nbsp;&nbsp;Status
     *        </span>
     *        </a></td>
     *      </tr>
     *      <tr>
     *       <td valign="top" align="left">
     *        <a href="system?show=deployments"><span class="subMenuOff">
     *        &nbsp;&nbsp;&nbsp;Deployments
     *        </span>
     *        </a></td>
     *      </tr>
     * 
     * @param body
     * @exception IOException
     */
    public void writeLeftNavBar(PrintWriter body) throws IOException{
        for (int i=0; i < navSections.length; i+=2){
            body.println("<tr><td valign=\"top\" align=\"left\">");
            body.print("<a href=\"");
            body.print(navSections[i]);
            body.print("\"><span class=\"subMenuOn\">");
            body.print(navSections[i+1]);
            body.print("</span></a></td></tr>");
            if ( navSections[i].equals( this.section )) writeSubMenuItems(body);            
        }
    }
    
    /**
     * Write the sub items for this bean in the left navigation bar of 
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
     * @param body
     * @exception IOException
     */
    public abstract void writeSubMenuItems(PrintWriter body) throws IOException;

    public String formatSubMenuItem(String itemName, String url){
        StringBuffer buff = new StringBuffer();
        buff.append("<tr>");
        buff.append("<td valign=\"top\" align=\"left\">");
        buff.append("<a href=\"").append(url).append("\">");
        buff.append("<span class=\"subMenuOff\">");
        buff.append("&nbsp;&nbsp;&nbsp;").append(itemName);
        buff.append("</span></a></td></tr>");

        return buff.toString();
    }

    /**
     * Write the main content
     * 
     * @param body
     * @exception IOException
     */
    public abstract void writeBody(PrintWriter body) throws IOException;

    /**
     * Write the footer
     * 
     * @param body
     * @exception IOException
     */
    public  void writeFooter(PrintWriter body) throws IOException{
        body.print(footer);
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

    public void writeTemplate(PrintWriter out, InputStream template) throws IOException{
        int b = template.read();
        //System.out.println("[] read");
        while (b != -1 && b != SUBSTITUTE) {
            out.write( b );
            b = template.read();
        }
        //System.out.println("[] done reading");
    }

    public InputStream getTemplate() throws IOException{
        System.out.println("[] get template");
        return new URL( "resource:/openejb/webadmin/template.html" ).openConnection().getInputStream();
    }

    public void initNavSections(){
        try{
            java.util.Vector sections = new java.util.Vector();

            Context ctx = org.openejb.OpenEJB.getJNDIContext();
            NamingEnumeration enum = ctx.list("openejb/ejb/webadmin");

            System.out.println("\n\nENUM "+enum);
            if ( enum == null){
                return;
            }

            while (enum.hasMore()) {

                NameClassPair entry = (NameClassPair)enum.next();
                System.out.println("ITEM NAME  "+entry.getName());
                System.out.println("ITEM CLASS "+entry.getClassName());
                if ( !entry.getClassName().equals("org.openejb.core.stateless.EncReference") ) {
                    continue;
                } 
                if ( entry.getName().startsWith("Default") ) {
                    continue;
                } 
                
                String beanName  = entry.getName();
                sections.add(beanName);
                sections.add(beanName);
            }

            navSections = new String[sections.size()];
            sections.copyInto( navSections );

        } catch (Exception e){
            e.printStackTrace();
        }
    }
    /*---------------------------------------------------------------*/
    /* EJB API Callbacks                                             */
    /*---------------------------------------------------------------*/
    
    public void ejbCreate() throws javax.ejb.CreateException {} 
    
    public void ejbActivate() throws javax.ejb.EJBException, java.rmi.RemoteException {
    }

    public void ejbPassivate() throws javax.ejb.EJBException, java.rmi.RemoteException {
    }
    
    public void ejbRemove() throws javax.ejb.EJBException, java.rmi.RemoteException {
    }
    
    public void setSessionContext(javax.ejb.SessionContext sessionContext) throws javax.ejb.EJBException, java.rmi.RemoteException {
        ejbContext = sessionContext;
        if (navSections == null) {
            initNavSections();
        }
    }
    
}
