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

import java.io.PrintWriter; 
import java.io.IOException;

public class HelloBean extends WebAdminBean {

    public void ejbCreate(){
        section = "HelloWorld";
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
        body.print("Hello World!");
    }

    /**
     * Write the title of the page.  This is displayed right
     * above the main block of content.
     * 
     * @param body
     * @exception IOException
     */
    public void writePageTitle(PrintWriter body) throws IOException{
        body.print("A simple example");
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
    public void writeSubMenuItems(PrintWriter body) throws IOException{
        body.print(formatSubMenuItem("English", "HelloWorld?lang=en"));
        body.print(formatSubMenuItem("French", "HelloWorld?lang=fr"));
        body.print(formatSubMenuItem("Spanish", "HelloWorld?lang=es"));
    }

    

    /**
     * Write the main content
     * 
     * @param body
     * @exception IOException
     */
    public void writeBody(PrintWriter body) throws IOException{
        String lang = request.getQueryParameter("lang");
        if (lang == null) lang = "en";

        if (lang.equals("en")) {
            body.print("Hello World!");
        } else if (lang.equals("fr")) {
            body.print("Bonjour Monde!");
        } else if (lang.equals("es")) {
            body.print("Hola Mundo!");
        } 
    }
} 
