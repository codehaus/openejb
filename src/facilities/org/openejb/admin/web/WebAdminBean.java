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
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.ejb.CreateException;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;

/** This is the template web admin bean to extend from.  It contains all the functionality for the webadministration.  To use
 *  this class, simply sub-class it:<br><br>
 *
 *  <code>
 *  public class MyBean extends WebAdminBean {
 *     ...
 *  }
 *  </code>
 *  <br><br>
 *  and declare the following methods:<br><br>
 *
 *  <code>
 *  public void ejbCreate() {}<br>
 *  public void preProcess(HttpRequest request, HttpResponse response) throws IOException {}<br>
 *  public void postProcess(HttpRequest request, HttpResponse response) throws IOException {}<br>
 *  public void writeBody(PrintWriter body) throws IOException {}<br>
 *  public void writeHtmlTitle(PrintWriter body) throws IOException {}<br>
 *  public void writePageTitle(PrintWriter body) throws IOException {}<br>
 *  public void writeSubMenuItems(PrintWriter body) throws IOException {}<br>
 *  </code>
 *
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:tim_urberg@yahoo.com">Tim Urberg</a>
 */
public abstract class WebAdminBean implements HttpBean {
    /** used for the session context
     */    
    protected SessionContext ejbContext;
    /** the substitue
     */    
    public static final int SUBSTITUTE = 26;
    /** the navigation sections
     */    
    public static HashMap sections;
    /** the menu section
     */    
    protected String section = "";
    /** the HTTP request
     */    
    protected HttpRequest request;
    /** the HTTP response
     */    
    protected HttpResponse response;
    /** the standard title */
    public static final String HTML_TITLE = "OpenEJB Web Administration Console";


    /** the main method of this bean, it takes care of the processing
     * @param request the http request
     * @param response the http response
     * @throws IOException if an exception is thrown
     */    
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

    /** called before any content is written to the browser
     * @param request the http request
     * @param response the http response
     * @throws IOException if an exception is thrown
     */    
    public abstract void preProcess(HttpRequest request, HttpResponse response) throws IOException;
    /** called after all content is written to the browser
     * @param request the http request
     * @param response the http response
     * @throws IOException if an exception is thrown
     */    
    public abstract void postProcess(HttpRequest request, HttpResponse response) throws IOException;
    
    /** Write the TITLE of the HTML document.  This is the part
     * that goes into the <code>&lt;head&gt;&lt;title&gt;
     * &lt;/title&gt;&lt;/head&gt;</code> tags
     *
     * @param body the output to write to
     * @exception IOException of an exception is thrown
     *
     */
    public abstract void writeHtmlTitle(PrintWriter body) throws IOException;

    /** Write the title of the page.  This is displayed right
     * above the main block of content.
     * 
     * @param body the output to write to
     * @exception IOException if an exception is thrown
     */
    public abstract void writePageTitle(PrintWriter body) throws IOException;

    /** Write the top navigation bar of the page. This should look somthing
     * like the one below:
     * 
     *     <code>
     *     &lt;a href="system?show=server"&gt;
     *     &lt;span class="menuTopOff"&gt;Remote Server&lt;/span&gt;
     *     &lt;/a&gt; &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
     *     &lt;a href="system?show=containers"&gt;
     *     &lt;span class="menuTopOff"&gt;Containers&lt;/span&gt;
     *     &lt;/a&gt; &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
     *     &lt;a href="system?show=deployments"&gt;
     *     &lt;span class="menuTopOff"&gt;Deployments&lt;/span&gt;
     *     &lt;/a&gt; &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
     *     &lt;a href="system?show=logs"&gt;
     *     &lt;span class="menuTopOff"&gt;Logs&lt;/span&gt;
     *     &lt;/a&gt; &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
     *     </code>
     * 
     * @param body the output to write to
     * @exception IOException if an exception is thrown
     */
    public void writeTopNavBar(PrintWriter body) throws IOException{
//        for (int i=0; i < navSections.length; i+=2){
//            body.print("<a href=\"");
//            body.print(navSections[i]);
//            body.print("\" class=\"menuTopOff\">");
//            body.print(navSections[i+1]);
//            if(i == (navSections.length-2))
//				body.print("</a>");
//            else
//            	body.print("</a> | ");
//        }
    }
    
     /** Write the left navigation bar of the page.  This should look somthing
     * like the one below:
     * 
     *     <code>
     *     &lt;tr&gt;
     *       &lt;td valign="top" align="left"&gt;
     *        &lt;span class="subMenuOn"&gt;
     *        Admin
     *        &lt;/span&gt;
     *       &lt;/td&gt;
     *      &lt;/tr&gt;
     *      &lt;tr&gt;
     *       &lt;td valign="top" align="left"&gt;
     *        &lt;a href="system?show=status"&gt;&lt;span class="subMenuOff"&gt;
     *        &nbsp;&nbsp;&nbsp;Status
     *        &lt;/span&gt;
     *        &lt;/a&gt;&lt;/td&gt;
     *      &lt;/tr&gt;
     *      &lt;tr&gt;
     *       &lt;&lt;td valign="top" align="left"&gt;
     *        &lt;a href="system?show=deployments"&gt;&lt;span class="subMenuOff"&gt;
     *        &nbsp;&nbsp;&nbsp;Deployments
     *        &lt;/span&gt;
     *        &lt;/a&gt;&lt;/td&gt;
     *      &lt;/tr&gt;
     *      </code>
     * 
      * @param body the output to write to
      * @exception IOException if an exception is thrown
     */
    public void writeLeftNavBar(PrintWriter body) throws IOException{
        Object[] entries = sections.entrySet().toArray();
        
        for (int i = 0; i < entries.length; i++) {
            Map.Entry entry = (Map.Entry)entries[i];
            String section = (String)entry.getKey();
            String[] subSections = (String[])entry.getValue();
            
            body.println("<tr><td valign=\"top\" align=\"left\">");
            body.print("<span class=\"subMenuOn\">");
            body.print(section);
            body.print("</td></tr>");
            
            for (int j=0; j < subSections.length; j+=2){
                String name = subSections[j];
                String url = subSections[j+1];
                
                body.print("<tr>");
                body.print("<td valign=\"top\" align=\"left\">");
                body.print("<a href=\"/");
                body.print(section);
                body.print('/');
                body.print(url);
                body.print("\" class=\"subMenuOff\">");
                body.print("&nbsp;&nbsp;&nbsp;");
                body.print(name);
                body.print("</a></td></tr>");
            }
        }
    }
    

    /** formats a sub menu item for the left navigation
     * @param itemName the name for display
     * @param url the url to link
     * @return the html that is formatted
     */    
    public String formatSubMenuItem(String itemName, String url){
        StringBuffer buff = new StringBuffer();

        return buff.toString();
    }

    /** writes the main body content to the broswer.  This content is inside a <code>&lt;p&gt;</code> block
     *  
     * 
     * @param body the output to write to
     * @exception IOException if an exception is thrown
     */
    public abstract void writeBody(PrintWriter body) throws IOException;

    /** Write the footer
     * 
     * @param body the output to write to
     * @exception IOException if an exception is thrown
     */
    public  void writeFooter(PrintWriter body) throws IOException{
        body.print(footer);
    }
    
    /** the footer
     */    
    protected static String footer = getFooter();

    /** gets a footer for the document
     * @return the footer string
     */    
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

    /** writes a template from the input stream to the output stream
     * @param out the output to write to
     * @param template the template to read
     * @throws IOException if an exception is thrown
     */    
    public void writeTemplate(PrintWriter out, InputStream template) throws IOException{
        int b = template.read();
        //System.out.println("[] read");
        while (b != -1 && b != SUBSTITUTE) {
            out.write( b );
            b = template.read();
        }
        //System.out.println("[] done reading");
    }

    /** gets an html template which is the content of the pages written to the browser
     * @throws IOException if an exception is thrown
     * @return the template
     */    
    public InputStream getTemplate() throws IOException{
        //System.out.println("[] get template");
        return new URL( "resource:/openejb/webadmin/template.html" ).openConnection().getInputStream();
    }

    /** initalizes the left and top menu navigation
     */    
    public HashMap initNavSections(){
        HashMap sections = new HashMap();
        try{
            Context ctx = org.openejb.OpenEJB.getJNDIContext();
            ctx = (Context) ctx.lookup("openejb/ejb");
            NamingEnumeration enum = ctx.list("");
            //System.out.println("\n\nENUM "+enum);
            
            if ( enum == null){
                return sections;
            }

            while (enum.hasMore()) {
                NameClassPair entry = (NameClassPair)enum.next();
                //System.out.println("ITEM NAME  "+entry.getName());
                //System.out.println("ITEM CLASS "+entry.getClassName());
                if ( !entry.getClassName().equals("org.openejb.core.ivm.naming.IvmContext") ) {
                    continue;
                } 
                
                Context subCtx = (Context) ctx.lookup(entry.getName());
                String[] subSections = getSubsections(subCtx);
                if (subSections.length > 0){
                    sections.put(entry.getName(), subSections );
                }                                                 
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return sections;
    }

    private String[] getSubsections(Context ctx){
        ArrayList sections = new ArrayList();
        try{
            NamingEnumeration enum = ctx.list("");
            
            if ( enum == null){
                return new String[0];
            }

            while (enum.hasMore()) {
                NameClassPair entry = (NameClassPair)enum.next();
                //System.out.println("ITEM NAME  "+entry.getName());
                //System.out.println("ITEM CLASS "+entry.getClassName());
                if ( !entry.getClassName().equals("org.openejb.core.stateless.EncReference") ) {
                    continue;
                } 

                if ( entry.getName().startsWith("Default") ) {
                    continue;
                } 

                Object obj = ctx.lookup(entry.getName());
                if (obj instanceof HttpHome){
                    String beanName  = entry.getName();
                    sections.add(beanName);
                    sections.add(beanName);
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return (String[]) sections.toArray(new String[0]);
    }
    /** prints a table row similar to this
     *
     * &lt;tr&gt;
     *   &lt;td&gt;some info&lt;/td&gt;
     *   &lt;td&gt;some more info&lt;/td&gt;
     * &lt;/tr&gt;
     * @param col1 the first column
     * @param col2 the second column
     * @param out the output to write to
     * @throws IOException if an exception is thrown
     */    
    protected void printRow(String col1, String col2, PrintWriter out) throws IOException{
        out.println("<tr>"  );
        out.print("<td class=\"bodyBlack\">");
        out.print(col1);
        out.println("</td>");
        out.print("<td class=\"bodyBlack\">");
        out.print(col2);
        out.println("</td>");
        out.println("</tr>");
    }

    /** prints a table row similar to this
     *
     * &lt;tr&gt;
     *   &lt;td&gt;some info&lt;/td&gt;
     *   &lt;td&gt;some more info&lt;/td&gt;
     *   &lt;td&gt;yet some more info&lt;/td&gt;
     * &lt;/tr&gt;
     * @param col1 the first column
     * @param col2 the second column
     * @param col3 the third column
     * @param out the output to write to
     * @throws IOException if an exception is thrown
     */    
    protected void printRow(String col1, String col2, String col3, PrintWriter out) throws IOException{
        out.println("<tr>");
        out.print("<td class=\"bodyBlack\">");
        out.print(col1);
        out.println("</td>");
        out.print("<td class=\"bodyBlack\">");
        out.print(col2);
        out.println("</td>");
        out.print("<td class=\"bodyBlack\">");
        out.print(col3);
        out.println("</td>");
        out.println("</tr>");
    }

    /*---------------------------------------------------------------*/
    /* EJB API Callbacks                                             */
    /*---------------------------------------------------------------*/
    /** called with the bean is created
     * @throws CreateException if the bean cannot be created
     */    
    public void ejbCreate() throws CreateException {} 
    
    /** called on a stateful sessionbean after the bean is
     * deserialized from storage and put back into use.      
     * @throws EJBException if an exeption is thrown
     * @throws RemoteException if an exception is thrown
     */    
    public void ejbActivate() {}

    /** called on a stateful sessionbean before the bean is 
     * removed from memory and serialized to a temporary store.  
     * This method is never called on a stateless sessionbean
     * @throws EJBException if an exception is thrown
     * @throws RemoteException if an exception is thrown
     */    
    public void ejbPassivate() {}

    /** called when the bean is about to be garbage collected
     * @throws EJBException if an exception is thrown
     * @throws RemoteException if an exception is thrown
     */    
    public void ejbRemove() {}

    /** sets the session context
     * @param sessionContext the session context
     * @throws EJBException if an exception is thrown
     * @throws RemoteException if an exception is thrown
     */    
    public void setSessionContext(SessionContext sessionContext) {
        ejbContext = sessionContext;
        if (sections == null) {
            sections = initNavSections();
        }
    }
}
