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


/** An interface to take care of HTTP Requests.  It parses headers, content, form and url
 *  parameters.
 *
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 */
public interface HttpRequest extends java.io.Serializable{
    
    /** the HTTP OPTIONS type */    
    public static final int OPTIONS = 0; // Section 9.2
    /** the HTTP GET type */    
    public static final int GET     = 1; // Section 9.3
    /** the HTTP HEAD type */    
    public static final int HEAD    = 2; // Section 9.4
    /** the HTTP POST type */    
    public static final int POST    = 3; // Section 9.5
    /** the HTTP PUT type */    
    public static final int PUT     = 4; // Section 9.6
    /** the HTTP DELETE type */    
    public static final int DELETE  = 5; // Section 9.7
    /** the HTTP TRACE type */    
    public static final int TRACE   = 6; // Section 9.8
    /** the HTTP CONNECT type */    
    public static final int CONNECT = 7; // Section 9.9
    /** the HTTP UNSUPPORTED type */    
    public static final int UNSUPPORTED = 8;

    /** Gets a header based the header name passed in.
     * @param name The name of the header to get
     * @return The value of the header
     */  
    public String getHeader(String name);

    /** Gets a form parameter based on the name passed in.
     * @param name The name of the form parameter to get
     * @return The value of the parameter
     */
    public String getFormParameter(String name);
    
    /** Gets all the form parameters in the form of a two-dimentional array
     *  The second dimention has two indexes which contain the key and value
     *  for example: 
     *  <code>
     *  for(int i=0; i<formParams.length; i++) {
     *     key = formParams[i][0];
     *     value = formParams[i][1];
     *  }
     *  </code>
     * 
     *  All values are strings
     * @return All the form parameters
     */
    public String[][] getFormParameters();

    /** Gets a URL (or query) parameter based on the name passed in.
     * @param name The name of the URL (or query) parameter
     * @return The value of the URL (or query) parameter
     */
    public String getQueryParameter(String name);

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
    public int getMethod();

    /** Gets the URI for the current URL page.
     * @return The URI
     */ 
    public java.net.URL getURI();

}