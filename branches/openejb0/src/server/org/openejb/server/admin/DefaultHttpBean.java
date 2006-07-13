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
package org.openejb.server.admin;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.ejb.SessionContext;

import org.openejb.admin.web.HttpBean;
import org.openejb.admin.web.HttpRequest;
import org.openejb.admin.web.HttpResponse;
import org.openejb.util.FileUtils;

/** This is a webadmin bean which has default functionality such as genderating
 * error pages and setting page content.
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 */
public class DefaultHttpBean implements HttpBean {

    /** The path in which to look for files. */
    private static final URL[] PATH = getSearchPath();
    
    /** the ejb session context */
	private SessionContext context;

    
    private static URL[] getSearchPath(){
        ArrayList path = new ArrayList();

        try {
            //OpenEJB Home and Base folders
            URL base = FileUtils.getBase().getDirectory().toURL();
            URL home = FileUtils.getHome().getDirectory().toURL();

            
            if (!base.sameFile(home)){
                path.add(new URL(base,"htdocs/"));
            }
            path.add(new URL(home,"htdocs/"));
            path.add(new URL("resource:/htdocs/"));
            path.add(new URL("resource:/openejb/webadmin/"));
           } catch (Exception e) {
            // TODO: 1: We should never get an exception here
            e.printStackTrace();
        }
        
        return (URL[]) path.toArray(new URL[0]);
    }

	/** Creates a new instance */
	public void ejbCreate() {}

	/** the main processing part of the this bean
	 * @param request the http request object
	 * @param response the http response object
	 * @throws IOException if an exception is thrown
	 */
	public void onMessage(HttpRequest request, HttpResponse response) throws java.io.IOException {
        InputStream in = null;  
        OutputStream out = null;
		// Internationalize this
		try {
			String file = request.getURI().getFile();
			String ext = (file.indexOf('.') == -1) ? null : file.substring(file.indexOf('.'));

			if (ext != null) {
				//resolve the content type
				if (ext.equalsIgnoreCase(".gif")) {
					response.setContentType("image/gif");
				} else if (ext.equalsIgnoreCase(".jpeg") || ext.equalsIgnoreCase(".jpg")) {
					response.setContentType("image/jpeg");
				} else if (ext.equalsIgnoreCase(".png")) {
					response.setContentType("image/png");
				} else if (ext.equalsIgnoreCase(".css")) {
					response.setContentType("text/css");
				} else if (ext.equalsIgnoreCase(".js")) {
					response.setContentType("text/javascript");
				} else if (ext.equalsIgnoreCase(".txt")) {
					response.setContentType("text/plain");
				} else if (ext.equalsIgnoreCase(".java")) {
					response.setContentType("text/plain");
				} else if (ext.equalsIgnoreCase(".xml")) {
					response.setContentType("text/plain");
				} else if (ext.equalsIgnoreCase(".zip")) {
					response.setContentType("application/zip");
				}
			}

            
            
			URLConnection resource = findResource(request.getURI().getFile());  
            HttpResponseImpl res = (HttpResponseImpl)response;
            res.setContent(resource);

        } catch (java.io.FileNotFoundException e) {
			do404(request, response);

		} catch (java.io.IOException e) {
			do500(request, response, e.getMessage());
		} finally {
            if (in != null) in.close();
        }
	}

    private URLConnection findResource(String fileName) throws FileNotFoundException, IOException{
        if (fileName.startsWith("/")){
            fileName = fileName.substring(1);
        }
        
        for (int i = 0; i < PATH.length; i++) {
            try {
                URL base = PATH[i];
                URL resource = new URL(base, fileName);
                URLConnection conn = resource.openConnection();
                if (resource.openConnection().getContentLength() > 0){
                    return conn;
                }
            } catch (MalformedURLException e) {
            } catch (FileNotFoundException e) {
            }
        }
        throw new FileNotFoundException("Cannot locate resource: "+fileName);
    }
	/** Creates a "Page not found" error screen
	 * @param request the HTTP request object
	 * @param response the HTTP response object
	 */
	public void do404(HttpRequest request, HttpResponse response) {
		response.reset(404, "Object not found.");
		java.io.PrintWriter body = response.getPrintWriter();

		body.println("<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\">");
		body.println("<HTML><HEAD>");
		body.println("<TITLE>404 Not Found</TITLE>");
		body.println("</HEAD><BODY>");
		body.println("<H1>Not Found</H1>");
		body.println(
			"The requested URL <font color=\"red\">"
				+ request.getURI().getFile()
				+ "</font> was not found on this server.<P>");
		body.println("<HR>");
		body.println("<ADDRESS>" + response.getServerName() + "</ADDRESS>");
		body.println("</BODY></HTML>");
	}

	/** Creates and "Internal Server Error" page
	 * @param request the HTTP request object
	 * @param response the HTTP response object
	 * @param message the message to be sent back to the browser
	 */
	public void do500(HttpRequest request, HttpResponse response, String message) {
		response.reset(500, "Internal Server Error.");
		java.io.PrintWriter body = response.getPrintWriter();
		body.println("<html>");
		body.println("<body>");
		body.println("<h3>Internal Server Error</h3>");
		body.println("<br><br>");

		if (message != null) {
			StringTokenizer msg = new StringTokenizer(message, "\n\r");
			while (msg.hasMoreTokens()) {
				body.print(msg.nextToken());
				body.println("<br>");
			}
		}

		body.println("</body>");
		body.println("</html>");
	}

	/** called on a stateful sessionbean after the bean is
	 * deserialized from storage and put back into use.      
	 * @throws EJBException if an exeption is thrown
	 * @throws RemoteException if an exception is thrown
	 */
	public void ejbActivate() throws javax.ejb.EJBException, java.rmi.RemoteException {}

	/** called on a stateful sessionbean before the bean is 
	 * removed from memory and serialized to a temporary store.  
	 * This method is never called on a stateless sessionbean
	 * @throws EJBException if an exception is thrown
	 * @throws RemoteException if an exception is thrown
	 */
	public void ejbPassivate() throws javax.ejb.EJBException, java.rmi.RemoteException {}

	/** called by the ejb container when this bean is about to be garbage collected
	 * @throws EJBException if an exception is thrown
	 * @throws RemoteException if an exception is thrown
	 */
	public void ejbRemove() throws javax.ejb.EJBException, java.rmi.RemoteException {}

	/** sets the session context for this bean
	 * @param sessionContext the session context to be set
	 * @throws EJBException if an exception is thrown
	 * @throws RemoteException if an exception is thrown
	 */
	public void setSessionContext(javax.ejb.SessionContext sessionContext)
		throws javax.ejb.EJBException, java.rmi.RemoteException {
		this.context = sessionContext;
	}
}
