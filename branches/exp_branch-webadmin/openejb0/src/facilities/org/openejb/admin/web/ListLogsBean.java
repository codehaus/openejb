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
 */
package org.openejb.admin.web;

import javax.ejb.EJBException;
import java.rmi.RemoteException;
import javax.ejb.SessionContext;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

/** This bean lists the openejb.log and transaction.log files
 *
 * @author  <a href="mailto:tim_urberg@yahoo.com">Tim Urberg</a>
 */
public class ListLogsBean extends WebAdminBean {
    /** the type of log we're using */    
    private String logType;
    
    /** called with the bean is created */ 
    public void ejbCreate() {
        this.section = "ListLogs";
    }
    
    /** after the processing is completed
     * @param request the http request
     * @param response the http response
     * @throws IOException if an exception is thrown
     */
    public void postProcess(HttpRequest request, HttpResponse response) throws IOException {
    }
    
    /** before the processing is done
     * @param request the http request
     * @param response the http response
     * @throws IOException if an exception is thrown
     */
    public void preProcess(HttpRequest request, HttpResponse response) throws IOException {
        //get the log type
        this.logType = request.getQueryParameter("log");
    }
    
    /** Write the main content
     *
     * @param body the output to write to
     * @exception IOException if an exception is thrown
     */
    public void writeBody(PrintWriter body) throws IOException {
        //get the openejb home property and openejb
        String openejbHome = System.getProperty("openejb.home") + System.getProperty("file.separator");
        //create the files
        String transactionLogString = openejbHome + "transaction.log";
        File transactionLog = new File(transactionLogString);
        String openejbLogString = openejbHome + "openejb.log";
        File openejbLog = new File(openejbLogString);
        
        //check the log type
        if(this.logType != null) {
            if(this.logType.equals("trans")) {
                //print the links and the log
                body.print("<a href=\"ListLogs?log=ejb\">openejb.log</a>");
                body.print("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
                body.println("transaction.log<br><br>");
                this.printLogFile(body, transactionLog);
            } else {
                //print out the links for the log
                body.print("openejb.log");
                body.print("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
                body.println("<a href=\"ListLogs?log=trans\">transaction.log</a><br><br>");
                this.printLogFile(body, openejbLog);
            }
        } else {
            //print out the links for the log
            body.print("openejb.log");
            body.print("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
            body.println("<a href=\"ListLogs?log=trans\">transaction.log</a><br><br>");
            this.printLogFile(body, openejbLog);
        }
    }
    
    /** gets the openejb.log file
     * @param body the output to send the data to
     * @param logFile the logfile that we're printing
     * @throws IOException if an exception is thrown
     */
    private void printLogFile(PrintWriter body, File logFile) throws IOException {
        BufferedReader fileReader = new BufferedReader(new FileReader(logFile));
        //create a regular expression to figure out what kind of message we have
        try {
            //create an array of regular expressions
            RE[] expArray = new RE[5];
            expArray[0] = new RE("^INFO :");
            expArray[1] = new RE("^DEBUG:");
            expArray[2] = new RE("^WARN :");
            expArray[3] = new RE("^ERROR:");
            expArray[4] = new RE("^FATAL:");
            
            //create an array of colors
            String[] colorArray = new String[5];
            colorArray[0] = "<span class=\"log4j-info\">";
            colorArray[1] = "<span class=\"log4j-debug\">";
            colorArray[2] = "<span class=\"log4j-warn\">";
            colorArray[3] = "<span class=\"log4j-error\">";
            colorArray[4] = "<span class=\"log4j-fatal\">";

            //read the file line by line
            String lineOfText = null;
            String expMatch = colorArray[0];
            while(true) {
                lineOfText = fileReader.readLine();
                //check for null
                if(lineOfText == null) {
                    break;
                }

                //loop through the array of expressions to find a match
                for(int i=0; i<expArray.length; i++) {
                    if(expArray[i].match(lineOfText)) {
                        expMatch = colorArray[i];
                    }
                }

                //print line of text to the page
                body.println(expMatch + lineOfText + "</span><br>");
            }
        } catch (RESyntaxException se) {
            throw new IOException(se.getMessage());
        }
        
        //close the file
        fileReader.close();
    }
 
    /** Write the TITLE of the HTML document.  This is the part
     * that goes into the <HEAD><TITLE></TITLE></HEAD> tags
     *
     * @param body the output to write to
     * @exception IOException of an exception is thrown
     */
    public void writeHtmlTitle(PrintWriter body) throws IOException {
        body.println(HTML_TITLE);
    }
    
    /** Write the title of the page.  This is displayed right
     * above the main block of content.
     *
     * @param body the output to write to
     * @exception IOException if an exception is thrown
     */
    public void writePageTitle(PrintWriter body) throws IOException {
        body.println("System Log Files");
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
     */
    public void writeSubMenuItems(PrintWriter body) throws IOException {
    }
    
}
