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

import java.io.*;

public class ConfigBean extends WebAdminBean {

    public void ejbCreate(){
        section = "Configuration";
    }
    
    public void preProcess(HttpRequest request, HttpResponse response) throws IOException{
    }
    
    public void postProcess(HttpRequest request, HttpResponse response) throws IOException{
    }
    
    /**
     * Write the TITLE of the HTML document.  This is the part
     * that goes into the <HEAD><TITLE></TITLE></HEAD> tags
     * 
     * @param body
     * @exception IOException
     */
    public void writeHtmlTitle(PrintWriter body) throws IOException{
        body.print("OpenEJB Configuration File");
    }

    /**
     * Write the title of the page.  This is displayed right
     * above the main block of content.
     * 
     * @param body
     * @exception IOException
     */
    public void writePageTitle(PrintWriter body) throws IOException{
        body.print("View your OpenEJB Configuration File");
    }

    public void writeSubMenuItems(PrintWriter body) throws IOException{
        body.print(formatSubMenuItem("Menu Item 1", "Configuration?foo=bar1"));
        body.print(formatSubMenuItem("Menu Item 2", "Configuration?foo=bar2"));
        body.print(formatSubMenuItem("Menu Item 3", "Configuration?foo=bar3"));
        body.print(formatSubMenuItem("Menu Item 4", "Configuration?foo=bar4"));
    }

    

    /**
     * Write the main content
     * 
     * @param body
     * @exception IOException
     */
    public void writeBody(PrintWriter body) throws IOException{
        String confLocation = System.getProperty("openejb.configuration");
        System.out.println("CONF? "+confLocation);
        File confFile = new File(confLocation);
        FileInputStream conf = new FileInputStream( confFile );

        int LT = (int)'<';
        int GT = (int)'>';
        int LF = (int)'\n';

        body.write("<span class=\"code-block\">");
        int i = conf.read();
        while (i != -1) {
            if (i == LT) {
                body.write("&lt;");
            } else if (i == GT) {
                body.write("&gt;");
            } else if (i == LF) {
                body.write("<br>");
            } else {
                body.write(i);
            }
            i = conf.read();
        }
        body.write("</span>");
    }
} 
