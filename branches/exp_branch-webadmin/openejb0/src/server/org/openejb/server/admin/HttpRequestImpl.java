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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.DataInputStream;
import java.net.URL;
import java.util.HashMap;
import javax.naming.*;
import java.util.StringTokenizer;
import java.io.DataInput;
import org.openejb.admin.web.HttpRequest;
import java.net.URLDecoder;

/** A class to take care of HTTP Requests.  It parses headers, content, form and url
 * parameters.
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:tim_urberg@yahoo.com">Tim Urberg</a>
 */
public class HttpRequestImpl implements  HttpRequest {
    /** 5.1   Request-Line */
    private String line;
    /** 5.1.1    Method */
    private int method;
    /** 5.1.2    Request-URI */
    private URL uri;
    /** the headers for this page */    
    private HashMap headers;
    /** the form parameters for this page */    
    private HashMap formParams = new HashMap();
    /** the URL (or query) parameters for this page */    
    private HashMap queryParams = new HashMap();
    /** the content of the body of this page */    
    private byte[] body;

    /** Gets a header based the header name passed in.
     * @param name The name of the header to get
     * @return The value of the header
     */    
    public String getHeader(String name){
        return (String)headers.get(name);
    }

    /** Gets a form parameter based on the name passed in.
     * @param name The name of the form parameter to get
     * @return The value of the parameter
     */    
    public String getFormParameter(String name){
        return (String)formParams.get(name);
    }

    /** Gets a URL (or query) parameter based on the name passed in.
     * @param name The name of the URL (or query) parameter
     * @return The value of the URL (or query) parameter
     */    
    public String getQueryParameter(String name){
        return (String)queryParams.get(name);
    }

    /** Gets an integer value of the request method.  These values are:
     *
     * OPTIONS = 0
     * GET     = 1
     * HEAD    = 2
     * POST    = 3
     * PUT     = 4
     * DELETE  = 5
     * TRACE   = 6
     * CONNECT = 7
     * UNSUPPORTED = 8
     * @return The integer value of the method
     */    
    public int getMethod(){
        return method;
    }

    /** Gets the URI for the current URL page.
     * @return The URI
     */    
    public URL getURI(){
        return uri;
    }

    /*------------------------------------------------------------*/
    /*  Methods for reading in and parsing a request              */
    /*------------------------------------------------------------*/
    /** parses the request into the 3 different parts, request, headers, and body
     * @param input the data input for this page
     * @throws IOException if an exception is thrown
     */    
    protected void readMessage(InputStream input) throws IOException{
        DataInput in = new DataInputStream(input);
        
        readRequestLine(in);
        readHeaders(in);
        readBody(in);
    }

    /** reads and parses the request line
     * @param in the input to be read
     * @throws IOException if an exception is thrown
     */    
    private void readRequestLine(DataInput in) throws IOException{
        try{
            line = in.readLine();
        } catch (Exception e){
            throw new IOException("Could not read the HTTP Request Line :"+ e.getClass().getName()+" : "+e.getMessage());
        }
        
        StringTokenizer lineParts = new StringTokenizer(line, " ");
        /* [1] Parse the method */
        parseMethod(lineParts);
        /* [2] Parse the URI */
        parseURI(lineParts);
    }

    /** parses the method for this page
     * @param lineParts a StringTokenizer of the request line
     * @throws IOException if an exeption is thrown
     */    
    private void parseMethod(StringTokenizer lineParts) throws IOException{
        String token = null;
        try{
            token = lineParts.nextToken();
        } catch (Exception e){
            throw new IOException("Could not parse the HTTP Request Method :"+ e.getClass().getName()+" : "+e.getMessage());
        }

        if ( token.equalsIgnoreCase("GET") ) {
            method = GET;
        } else if ( token.equalsIgnoreCase("POST") ) {
            method = POST;
        } else {
            method = UNSUPPORTED;
            throw new IOException("Unsupported HTTP Request Method :"+ token);
        }
    }

    /** parses the URI into the different parts
     * @param lineParts a StringTokenizer of the URI
     * @throws IOException if an exeption is thrown
     */    
    private void parseURI(StringTokenizer lineParts) throws IOException{
        String token = null;
        try{
            token = lineParts.nextToken();
        } catch (Exception e){
            throw new IOException("Could not parse the HTTP Request Method :"+ e.getClass().getName()+" : "+e.getMessage());
        }

        try {
            uri = new URL("http","localhost", token );
        } catch (java.net.MalformedURLException e){
            throw new IOException("Malformed URL :"+ token +" Exception: "+e.getMessage());
        }

        parseQueryParams(uri.getQuery());
    }

    /** parses the URL (or query) parameters
     * @param query the URL (or query) parameters to be parsed
     * @throws IOException if an exception is thrown
     */    
    private void parseQueryParams(String query) throws IOException{
        if (query == null) return;
        StringTokenizer parameters = new StringTokenizer(query, "&");

        while (parameters.hasMoreTokens()) {
            StringTokenizer param = new StringTokenizer(parameters.nextToken(), "=");    

            /* [1] Parse the Name */
            if (!param.hasMoreTokens()) continue;
            String name = URLDecoder.decode(param.nextToken());
            if (name == null) continue;

            /* [2] Parse the Value */
            if (!param.hasMoreTokens()) continue;
            String value = URLDecoder.decode(param.nextToken());
            if (value == null) continue;

            //System.out.println("[] "+name+" = "+value);
            queryParams.put(name, value);
        }
    }

    /** reads the headers from the data input sent from the browser
     * @param in the data input sent from the browser
     * @throws IOException if an exeption is thrown
     */    
    private void readHeaders(DataInput in) throws IOException{
        headers = new HashMap();
        while (true) {
            // Header Field
            String hf = null;

            try{
                hf = in.readLine();
                //System.out.println(hf);
            } catch (Exception e){
                throw new IOException("Could not read the HTTP Request Header Field :"+ e.getClass().getName()+" : "+e.getMessage());
            }
                
            if ( hf == null || hf.equals("") ) {
                break;
            }
            
            /* [1] parse the name */
            int colonIndex = hf.indexOf((int)':');
            String name = hf.substring(0, colonIndex);
            if (name == null) break;

            /* [2] Parse the Value */
            String value = hf.substring(colonIndex+1, hf.length());
            if (value == null) break;
            value = value.trim();
            headers.put(name, value);
        }
        
        //temp-debug-------------------------------------------
        java.util.Iterator myKeys = headers.keySet().iterator();
        String temp = null;
        //while(myKeys.hasNext()) {
        //    temp = (String)myKeys.next();
        //    System.out.println("Test: " + temp + "=" + headers.get(temp));
        //}
        //end temp-debug---------------------------------------
     }

    /** reads the body from the data input passed in
     * @param in the data input with the body of the page
     * @throws IOException if an exception is thrown
     */    
    private void readBody(DataInput in) throws IOException{
        readRequestBody(in);
        //System.out.println("Body Length: " + body.length);
        // Content-type: application/x-www-form-urlencoded
        // or multipart/form-data
        String type = getHeader("Content-Type");
        if (type != null && type.equals("application/x-www-form-urlencoded")) {
            parseFormParams();
        }
    }

    /** reads the request line of the data input
     * @param in the data input that contains the request line
     * @throws IOException if an exception is thrown
     */    
    private void readRequestBody(DataInput in) throws IOException{
        // Content-length: 384
        String len  = getHeader("Content-Length");
        //System.out.println("readRequestBody Content-Length: " + len);

        int length = -1;
        if (len != null) {
            try{
                length = Integer.parseInt(len);
            } catch (Exception e){
                //don't care
            }
        } 

        if (length < 1) {
            this.body = new byte[0];
        } else if (length > 0) {
            this.body = new byte[length];

            try {
                in.readFully( body );
            } catch (Exception e){
                throw new IOException("Could not read the HTTP Request Body :"+ e.getClass().getName()+" : "+e.getMessage());
            }
        }
    }

    /** parses form parameters into the formParams variable
     * @throws IOException if an exeption is thrown
     */    
    private void parseFormParams() throws IOException{
        String rawParams = new String( body );
        //System.out.println("rawParams: " + rawParams);
        StringTokenizer parameters = new StringTokenizer(rawParams, "&");
        String name = null;
        String value = null;
        
        while (parameters.hasMoreTokens()) {
            StringTokenizer param = new StringTokenizer(parameters.nextToken(), "=");    
            
            /* [1] Parse the Name */
            name = URLDecoder.decode(param.nextToken());
            if (name == null) break;

            /* [2] Parse the Value */
            try {
                value = URLDecoder.decode(param.nextToken());
            } catch (java.util.NoSuchElementException nse) {
                value = ""; //if there is no token set value to null
            }
            
            if (value == null) value = "";
            formParams.put(name, value);
            //System.out.println(name + ": " + value);
        }
    }
}