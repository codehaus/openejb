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
 * $Id: JdbcManagedConnectionMetaData.java,v 1.1.1.1 2004/03/26 21:42:49 dblevins Exp $
 */
package org.openejb.resource.jdbc;

public class JdbcManagedConnectionMetaData 
implements javax.resource.spi.ManagedConnectionMetaData {
    
    private java.sql.DatabaseMetaData sqlMetaData;
    
    public JdbcManagedConnectionMetaData(java.sql.DatabaseMetaData sqlMetaData){
        this.sqlMetaData = sqlMetaData;
    }

    public java.lang.String getEISProductName() 
    throws javax.resource.spi.ResourceAdapterInternalException{
        try{
        return "OpenEJB JDBC Connector (over "+sqlMetaData.getDriverName()+")";
        }catch(java.sql.SQLException sqlE){
            throw new javax.resource.spi.ResourceAdapterInternalException("MetaData is not available. Connection may be lost", ErrorCode.JDBC_0004);
        }
    }
    public java.lang.String getEISProductVersion()
    throws javax.resource.spi.ResourceAdapterInternalException{
        try{
        return "Beta 1.0 (over "+sqlMetaData.getDriverVersion()+")";
        }catch(java.sql.SQLException sqlE){
            throw new javax.resource.spi.ResourceAdapterInternalException("MetaData is not available. Connection may be lost", ErrorCode.JDBC_0004);
        }
    }
    public int getMaxConnections()
    throws javax.resource.spi.ResourceAdapterInternalException{
        try{
        return sqlMetaData.getMaxConnections(); 
        }catch(java.sql.SQLException sqlE){
            throw new javax.resource.spi.ResourceAdapterInternalException("MetaData is not available. Connection may be lost", ErrorCode.JDBC_0004);
        }
    }
    public java.lang.String getUserName()
    throws javax.resource.spi.ResourceAdapterInternalException{
        try{
        return sqlMetaData.getUserName();
        }catch(java.sql.SQLException sqlE){
            throw new javax.resource.spi.ResourceAdapterInternalException("MetaData is not available. Connection may be lost", ErrorCode.JDBC_0004);
        }

    }

}