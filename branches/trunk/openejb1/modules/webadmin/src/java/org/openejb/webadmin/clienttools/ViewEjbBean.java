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
 *    please contact dev@openejb.org.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://www.openejb.org/).
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
package org.openejb.webadmin.clienttools;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.openejb.webadmin.HttpRequest;
import org.openejb.webadmin.HttpResponse;
import org.openejb.webadmin.HttpSession;
import org.openejb.webadmin.WebAdminBean;
import org.openejb.alt.assembler.classic.ContainerInfo;
import org.openejb.alt.assembler.classic.EnterpriseBeanInfo;
import org.openejb.alt.config.ConfigurationFactory;
import org.openejb.core.DeploymentInfo;

/**
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 */
public class ViewEjbBean extends WebAdminBean implements Constants {

    public void preProcess(HttpRequest request, HttpResponse response)
        throws IOException {
    }

    public void postProcess(HttpRequest request, HttpResponse response)
        throws IOException {
    }

    public void writeHtmlTitle(PrintWriter out) throws IOException {
        out.write("Client Tools -- EJB Viewer");
    }

    public void writePageTitle(PrintWriter out) throws IOException {
        out.write("EJB Viewer");
    }

    public void writeBody(PrintWriter out) throws IOException {
        try {
            String ejb = request.getQueryParameter("ejb");
            if (ejb == null) {
                ContainerInfo[] cnt = ConfigurationFactory.sys.containerSystem.containers;
                for (int i = 0; i < cnt.length; i++) {
                    EnterpriseBeanInfo[] beans = cnt[i].ejbeans;
                    for (int x = 0; x < beans.length; x++) {
                    	EnterpriseBeanInfo bean = beans[x];
                        out.print("<a href='"+VIEW_EJB+"?ejb="+bean.ejbDeploymentId+"'>"+ejbImg+"&nbsp;&nbsp;"+bean.ejbDeploymentId+"</a><br>");
                    }
                }
            } else {
                printEjb(ejb, out, request.getSession());
            }
        } catch (Exception e) {

            out.println("FAIL: ");
            out.print(e.getMessage());
            //            throw e;
            //return;
        }
    }

    public void printEjb(String name, PrintWriter out, HttpSession session)
        throws Exception {
    	
    	
        String id =
            (name.startsWith("/")) ? name.substring(1, name.length()) : name;
        org.openejb.DeploymentInfo ejb =
            org.openejb.OpenEJB.getDeploymentInfo(id);

        
        if (ejb == null) {
            out.print("No such EJB: " + id);
            return;
        }
        String type = null;

        switch (ejb.getComponentType()) {
            case org.openejb.core.DeploymentInfo.CMP_ENTITY :
                type = "EntityBean with Container-Managed Persistence";
                break;
            case org.openejb.core.DeploymentInfo.BMP_ENTITY :
                type = "EntityBean with Bean-Managed Persistence";
                break;
            case org.openejb.core.DeploymentInfo.STATEFUL :
                type = "Stateful SessionBean";
                break;
            case org.openejb.core.DeploymentInfo.STATELESS :
                type = "Stateless SessionBean";
                break;
            default :
                type = "Unkown Bean Type";
                break;
        }
        out.print("<b>" + type + "</b><br>");
        out.print("<table>");
        printRow("JNDI Name", name, out);
        
        
        boolean hasLocal = ejb.getLocalInterface() != null;
        boolean hasRemote = ejb.getRemoteInterface() != null;

        String remoteInterfaceClassRef;
        String homeInterfaceClassRef;
		if (hasRemote){
            remoteInterfaceClassRef = getClassRef(ejb.getRemoteInterface());
            homeInterfaceClassRef = getClassRef(ejb.getHomeInterface());
        } else {
            remoteInterfaceClassRef = "none";
            homeInterfaceClassRef = "none";
        }
        
		printRow("Remote Interface",remoteInterfaceClassRef,out);
		printRow("Home Interface", homeInterfaceClassRef, out);

		if (hasLocal){
            String clzz = getClassRef(ejb.getLocalInterface());
    		printRow("Local Interface",clzz,out);
            clzz = getClassRef(ejb.getLocalHomeInterface());
    		printRow("LocalHome Interface",clzz,out);
        }
        
		printRow("Bean Class", getClassRef(ejb.getBeanClass()), out);

        if (ejb.getComponentType() == DeploymentInfo.BMP_ENTITY
            || ejb.getComponentType() == DeploymentInfo.CMP_ENTITY) {
            printRow("Primary Key", getClassRef(ejb.getPrimaryKeyClass()), out);
        }

        out.print("</table>");
        out.print("<br><br><b>Actions:</b><br>");
        out.print("<table>");

        // Browse JNDI with this ejb
        //javax.servlet.http.HttpSession session = this.session;
        HashMap objects = (HashMap) session.getAttribute("objects");
        if (objects == null) {
            objects = new HashMap();
            session.setAttribute("objects", objects);
        }

        InitialContext ctx;
        Properties p = new Properties();

        p.put(
            Context.INITIAL_CONTEXT_FACTORY,
            "org.openejb.client.LocalInitialContextFactory");
        p.put("openejb.loader", "embed");

        ctx = new InitialContext(p);
        

        if (hasRemote){
	        Object obj = ctx.lookup(name);
	        String objID = ejb.getHomeInterface().getName() + "@" + obj.hashCode();
	        objects.put(objID, obj);
	        String invokerURL =
	            "<a href='"
	                + INVOKE_OBJ
	                + "?obj="
	                + objID
	                + "'>Invoke this EJB's home interface</a>";
	        printRow(pepperImg, invokerURL, out);
        }
        if (hasLocal){
	        Object obj = ctx.lookup(name+"Local");
	        String objID = ejb.getLocalHomeInterface().getName() + "@" + obj.hashCode();
	        objects.put(objID, obj);
	        String invokerURL =
	            "<a href='"
	                + INVOKE_OBJ
	                + "?obj="
	                + objID
	                + "'>Invoke this EJB's local home interface</a>";
	        printRow(pepperImg, invokerURL, out);
        }

        Context enc = ((org.openejb.core.DeploymentInfo) ejb).getJndiEnc();
        String ctxID = "enc" + enc.hashCode();
        session.setAttribute(ctxID, enc);
        String jndiURL =
            "<a href='"
                + VIEW_JNDI
                + "?ctx="
                + ctxID
                + "'>Browse this EJB's private JNDI namespace</a>";
        printRow(pepperImg, jndiURL, out);
        out.print("</table>");

    }

    protected void printRow(String col1, String col2, PrintWriter out)
        throws IOException {
        out.print("<tr><td><font size='2'>");
        out.print(col1);
        out.print("</font></td><td><font size='2'>");
        out.print(col2);
        out.print("</font></td></tr>");
    }

    public String getClassRef(Class clazz) throws Exception {
        String name = clazz.getName();
        return "<a href='"
            + VIEW_CLASS
            + "?class="
            + name
            + "'>"
            + name
            + "</a>";
    }

    public String getShortClassRef(Class clazz) throws Exception {
        if (clazz.isPrimitive()) {
            return "<font color='gray'>" + clazz.getName() + "</font>";
        } else if (clazz.isArray() && clazz.getComponentType().isPrimitive()) {
            return "<font color='gray'>"
                + clazz.getComponentType()
                + "[]</font>";
        } else if (clazz.isArray()) {
            String name = clazz.getComponentType().getName();
            int dot = name.lastIndexOf(".") + 1;
            String shortName = name.substring(dot, name.length());
            return "<a href='"
                + VIEW_CLASS
                + "?class="
                + name
                + "'>"
                + shortName
                + "[]</a>";
        } else {
            String name = clazz.getName();
            int dot = name.lastIndexOf(".") + 1;
            String shortName = name.substring(dot, name.length());
            return "<a href='"
                + VIEW_CLASS
                + "?class="
                + name
                + "'>"
                + shortName
                + "</a>";
        }
    }
}
