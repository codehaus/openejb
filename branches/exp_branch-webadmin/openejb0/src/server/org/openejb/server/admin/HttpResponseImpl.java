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

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Properties;
import java.util.Set;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Iterator;
import org.openejb.util.JarUtils;
import javax.naming.*;
import org.openejb.admin.web.HttpResponse;

/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 */
public class HttpResponseImpl implements HttpResponse {
    
    /** Response string */
    private String responseString = "OK";

    /** Code */
    private int code = 200;
    
    /** Response headers */
    private HashMap headers;
    
    /** Response body */
    private byte[] body = new byte[0];
    
    private transient PrintWriter writer;
    private transient ByteArrayOutputStream baos;
    
    public static final String HTTP_VERSION = "HTTP/1.1";
    public static final String CRLF = "\r\n";
    public static final String SP = " ";
    public static final String CSP = ": ";
    public static String server;

    public void setHeader(String name, String value){
        headers.put(name, value);
    }

    public String getHeader(String name){
        return (String) headers.get(name);
    }

    public PrintWriter getPrintWriter(){
        return writer;
    }

    public OutputStream getOutputStream(){
        return baos;
    }

    public void setCode(int code){
        this.code = code;
    }

    public int getCode(){
        return code;
    }

    public void setContentType(String type){
        setHeader("Content-Type", type);
    }

    public String getContentType(){
        return getHeader("Content-Type");
    }

    public void setResponseString(String responseString){
       this.responseString = responseString;
    }

    public void reset(){
        initBody();
    }

    public void reset(int code, String responseString){
        setCode(code);
        setResponseString(responseString);
        initBody();
    }

    /*------------------------------------------------------------*/
    /*  Methods for writing out a response                        */
    /*------------------------------------------------------------*/
    protected HttpResponseImpl(){
        this(200, "OK", "text/html");
    }

    protected HttpResponseImpl(int code, String responseString, String contentType){
        this.code = code;
        this.responseString = responseString;
    }

    /**
     *  HTTP/1.1 200 OK
     *  Server: Netscape-Enterprise/3.6 SP3
     *  Date: Thu, 07 Jun 2001 17:30:42 GMT
     *  Content-type: text/html
     *  Connection: close
     *
     */
    protected void writeMessage(OutputStream output) throws IOException{
        DataOutput out = new DataOutputStream(output);
        DataOutput log = new DataOutputStream(System.out);
        closeMessage();
        writeResponseLine(log);
        writeHeaders(log);
        writeBody(log);
        writeResponseLine(out);
        writeHeaders(out);
        writeBody(out);
    }

    private void initBody(){
        baos = new ByteArrayOutputStream();
        writer = new PrintWriter( baos );
    }

    public String toString(){
        StringBuffer buf = new StringBuffer(40);

        buf.append(HTTP_VERSION);
        buf.append(SP);
        buf.append(code+"");
        buf.append(SP);
        buf.append(responseString);

        return buf.toString();
    }

    private void closeMessage() throws IOException{
        writer.flush();
        writer.close();
        body = baos.toByteArray();
        setHeader("Content-length", body.length+"");
    }

    /**
     *  HTTP/1.1 200 OK
     */
    private void writeResponseLine(DataOutput out) throws IOException{
        out.writeBytes(HTTP_VERSION);
        out.writeBytes(SP);
        out.writeBytes(code+"");
        out.writeBytes(SP);
        out.writeBytes(responseString);
        out.writeBytes(CRLF);
    }

    private void writeHeaders(DataOutput out) throws IOException{
        Iterator it =  headers.entrySet().iterator();

        while (it.hasNext()){
            Map.Entry entry = (Map.Entry)it.next();
            out.writeBytes(""+entry.getKey());
            out.writeBytes(CSP);
            out.writeBytes(""+entry.getValue());
            out.writeBytes(CRLF);
        }
    }

    private void writeBody(DataOutput out) throws IOException{
        out.writeBytes(CRLF);
        out.write(body);
    }

    public String getServerName(){
        if (server == null) {
            String version = "???";
            String os = "(unknown os)";
            
            try {
                Properties versionInfo = new Properties();
                JarUtils.setHandlerSystemProperty();
                versionInfo.load( new URL( "resource:/openejb-version.properties" ).openConnection().getInputStream() );
                version = versionInfo.getProperty( "version" );
                os = System.getProperty("os.name")+"/"+System.getProperty("os.version")+" ("+System.getProperty("os.arch")+")";
            } catch (java.io.IOException e) {
            }
            
            server = "OpenEJB/" +version+ " "+os;
        }
        return server;
    }
    
    
    /**
     * This could be improved at some day in the future 
     * to also include a stack trace of the exceptions
     * 
     * @param message
     * @return 
     */

    protected static HttpResponseImpl createError(String message){
        return createError(message, null);
    }

    protected static HttpResponseImpl createError(String message, Throwable t){
        HttpResponseImpl res = new HttpResponseImpl(500, "Internal Server Error", "text/html");
        java.io.PrintWriter body = res.getPrintWriter();

        body.println("<html>");
        body.println("<body>");
        body.println("<h3>Internal Server Error</h3>");
        body.println("<br><br>");

        if (message != null) {
            StringTokenizer msg = new StringTokenizer(message, "\n\r");

            while (msg.hasMoreTokens()) {
                body.print( msg.nextToken() );
                body.println("<br>");
            }
        }

        if (t != null) {
            try{
                body.println("<br><br>");
                body.println("Stack Trace:<br>");
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PrintWriter writer = new PrintWriter( baos );
                t.printStackTrace(writer);
                writer.flush();
                writer.close();
                message = new String(baos.toByteArray());
                StringTokenizer msg = new StringTokenizer(message, "\n\r");
                
                while (msg.hasMoreTokens()) {
                    body.print( msg.nextToken() );
                    body.println("<br>");
                }
            } catch (Exception e){
            }
        }

        body.println("</body>");
        body.println("</html>");

        return res;
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException{
        /** Response string */
        out.writeObject( responseString );

        /** Code */
        out.writeInt( code );

        /** Response headers */
        out.writeObject( headers );

        /** Response body */
        writer.flush();
        body = baos.toByteArray();
        out.writeObject( body );
    }
    
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException{
        /** Response string */
        this.responseString = (String)in.readObject();

        /** Code */
        this.code = in.readInt();

        /** Response headers */
        this.headers = (HashMap) in.readObject();

        /** Response body */
        body = (byte[]) in.readObject();
        baos = new ByteArrayOutputStream();
        baos.write( body );
        writer = new PrintWriter( baos );

    }
}