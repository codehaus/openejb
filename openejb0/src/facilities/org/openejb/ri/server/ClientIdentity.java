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
 * 3. The name "Exolab" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of Exoffice Technologies.  For written permission,
 *    please contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Exoffice Technologies. Exolab is a registered
 *    trademark of Exoffice Technologies.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.exolab.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY EXOFFICE TECHNOLOGIES AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * EXOFFICE TECHNOLOGIES OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 1999 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.ri.server;

import java.net.InetAddress;
import org.openejb.util.proxy.InvocationHandler;

/**
* The unique identity of the client application that invoked the method.
* Uniquely identifies the process (Java Virtual Machine) and computer (URL) that invoked the method.
*/
public class ClientIdentity implements java.io.Serializable {
    final static String JVM_ID = Runtime.getRuntime().hashCode()+":"+System.currentTimeMillis();
    protected InetAddress inet;
    protected String jvm;
    protected String proxy;

    public ClientIdentity() {
        jvm = JVM_ID;
        try {
            inet = InetAddress.getLocalHost();
        } catch ( Exception e ) {
            inet = null;
        }
    }
    public String getJvmID( ) {
        return jvm;
    }
    public InetAddress getInetAddress( ) {
        return inet;
    }
    /*
    * This method tests if the ClientIdentity object come from the same JVM on the same 
    * Host.
    */
    public boolean equals(Object other) {
        if ( other instanceof ClientIdentity ) {
            ClientIdentity otherIdentity = (ClientIdentity)other;
            if ( this.jvm.equals(otherIdentity.jvm) ) {
                if ( (otherIdentity.inet == null && this.inet == null) || this.inet.equals(otherIdentity.inet) )
                    return true;
            }
        }
        return false;
    }
    public String toString( ) {
        return inet.toString()+":"+jvm;
    }
    public int hashCode() {
        return this.toString().hashCode();
    }

}