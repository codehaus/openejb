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
package org.openejb.resource.jdbc;

import javax.resource.spi.ConnectionRequestInfo;

public class JdbcConnectionRequestInfo implements ConnectionRequestInfo {
    
    private String userName;
    private String password;
    private String jdbcDriver;
    private String jdbcUrl;
    private int hashCode;
    
    public JdbcConnectionRequestInfo(String userName, String password, String jdbcDriver, String jdbcUrl){
        this.userName = userName;
        this.password = password;
        this.jdbcDriver = jdbcDriver;
        this.jdbcUrl = jdbcUrl;
    }
    protected String getUserName(){
        return userName;
    }
    protected String getPassword(){
        return password;
    }
    protected String getJdbcDriver(){
        return jdbcDriver;
    }
    protected String getJdbcUrl(){
        return jdbcUrl;
    }
    public boolean equals(java.lang.Object other){
        if(other instanceof JdbcConnectionRequestInfo &&
           ((JdbcConnectionRequestInfo)other).getUserName().equals(userName) &&
           ((JdbcConnectionRequestInfo)other).getPassword().equals(password) &&
           ((JdbcConnectionRequestInfo)other).getJdbcDriver().equals(jdbcDriver) &&
           ((JdbcConnectionRequestInfo)other).getJdbcUrl().equals(jdbcUrl) )
           return true;
        else
           return false;
    }
    public int hashCode(){
        if(hashCode != 0) return hashCode;
        hashCode = jdbcDriver.hashCode()^jdbcUrl.hashCode()^userName.hashCode()^password.hashCode();
        return hashCode;
    }
    
}