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

import java.sql.*;

import javax.resource.ResourceException;
import javax.resource.spi.LazyAssociatableConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnectionFactory;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

public class JdbcConnection implements java.sql.Connection {

    private static Log log = LogFactory.getLog(JdbcConnection.class);

    private JdbcManagedConnection managedConn;
    protected boolean isClosed = false;
    private final JdbcManagedConnectionFactory managedConnectionFactory;
    private final ConnectionRequestInfo connectionRequestInfo;

    protected JdbcConnection(JdbcManagedConnection managedConn, ConnectionRequestInfo connectionRequestInfo, JdbcManagedConnectionFactory managedConnectionFactory) {
        this.managedConn = managedConn;
        this.connectionRequestInfo = connectionRequestInfo;
        this.managedConnectionFactory = managedConnectionFactory;
    }

    protected Connection getPhysicalConnection() throws SQLException {
        if (isClosed) {
            throw new SQLException("Connection is closed");
        }
        if (managedConn == null) {
            managedConnectionFactory.associateConnection(this, connectionRequestInfo);
        }
        return getManagedConnection().getSQLConnection();
    }

    protected JdbcManagedConnection getManagedConnection() {
        return managedConn;
    }

    /**
     * Renders this conneciton invalid; unusable.  Its called by the
     * JdbcManagedConnection when its connectionClose() or cleanup()
     * methods are invoked.
     */
    protected void invalidate(boolean setClosed) {
        isClosed = setClosed;
        managedConn = null;
    }

    protected void associate(JdbcManagedConnection mngdConn) throws ResourceException {
        if (isClosed) {
            throw new ResourceException("Connection handle has been closed");
        }
        managedConn = mngdConn;
    }

    public Statement createStatement() throws SQLException {
        try {
            Connection physicalConn = getPhysicalConnection();
            synchronized (physicalConn) {
                return new JdbcStatement(this, physicalConn.createStatement());
            }
        } catch (SQLException sqlE) {
            managedConn.connectionErrorOccurred(this, sqlE);
            throw sqlE;
        }
    }

    public PreparedStatement prepareStatement(String sql)
            throws SQLException {
        log.info("Preparing statement: " + sql);
        try {
            Connection physicalConn = getPhysicalConnection();
            synchronized (physicalConn) {
                return new JdbcPreparedStatement(this, physicalConn.prepareStatement(sql));
            }
        } catch (SQLException sqlE) {
            managedConn.connectionErrorOccurred(this, sqlE);
            throw sqlE;
        }
    }

    public CallableStatement prepareCall(String sql) throws SQLException {
        log.info("Preparing call: " + sql);
        try {
            Connection physicalConn = getPhysicalConnection();
            synchronized (physicalConn) {
                return physicalConn.prepareCall(sql);
            }
        } catch (SQLException sqlE) {
            managedConn.connectionErrorOccurred(this, sqlE);
            throw sqlE;
        }
    }

    public String nativeSQL(String sql) throws SQLException {
        try {
            Connection physicalConn = getPhysicalConnection();
            synchronized (physicalConn) {
                return physicalConn.nativeSQL(sql);
            }
        } catch (SQLException sqlE) {
            managedConn.connectionErrorOccurred(this, sqlE);
            throw sqlE;
        }
    }

    public void setAutoCommit(boolean autoCommit) throws SQLException {
        throw new java.sql.SQLException("Method not supported. Commit is managed automatically by container provider");
    }

    public boolean getAutoCommit() throws SQLException {
        throw new java.sql.SQLException("Method not supported. Commit is managed automatically by container provider");
    }

    public void commit() throws SQLException {
        throw new java.sql.SQLException("Method not supported. Commit is managed automatically by container provider");
    }

    public void rollback() throws SQLException {
        throw new java.sql.SQLException("Method not supported. Rollback is managed automatically by container provider");
    }

    public void close() throws SQLException {
        if (isClosed)
            return;
        else {
            // managed conneciton will call this object's invalidate() method which
            // will set isClosed = true, and nullify references to the sqlConnection and managed connection.
            managedConn.connectionClose(this, true);
        }
    }

    public boolean isClosed() throws SQLException {
        return isClosed;
    }

    public DatabaseMetaData getMetaData() throws SQLException {
        try {
            Connection physicalConn = getPhysicalConnection();
            synchronized (physicalConn) {
                return physicalConn.getMetaData();
            }
        } catch (SQLException sqlE) {
            managedConn.connectionErrorOccurred(this, sqlE);
            throw sqlE;
        }
    }

    public void setReadOnly(boolean readOnly) throws SQLException {
        try {
            Connection physicalConn = getPhysicalConnection();
            synchronized (physicalConn) {
                physicalConn.setReadOnly(readOnly);
            }
        } catch (SQLException sqlE) {
            managedConn.connectionErrorOccurred(this, sqlE);
            throw sqlE;
        }
    }

    public boolean isReadOnly() throws SQLException {
        try {
            Connection physicalConn = getPhysicalConnection();
            synchronized (physicalConn) {
                return physicalConn.isReadOnly();
            }
        } catch (SQLException sqlE) {
            managedConn.connectionErrorOccurred(this, sqlE);
            throw sqlE;
        }
    }

    public void setCatalog(String catalog) throws SQLException {
        try {
            Connection physicalConn = getPhysicalConnection();
            synchronized (physicalConn) {
                physicalConn.setCatalog(catalog);
            }
        } catch (SQLException sqlE) {
            managedConn.connectionErrorOccurred(this, sqlE);
            throw sqlE;
        }
    }

    public String getCatalog() throws SQLException {
        try {
            Connection physicalConn = getPhysicalConnection();
            synchronized (physicalConn) {
                return physicalConn.getCatalog();
            }
        } catch (SQLException sqlE) {
            managedConn.connectionErrorOccurred(this, sqlE);
            throw sqlE;
        }
    }

    public void setTransactionIsolation(int level) throws SQLException {
        try {
            Connection physicalConn = getPhysicalConnection();
            synchronized (physicalConn) {
                physicalConn.setTransactionIsolation(level);
            }
        } catch (SQLException sqlE) {
            managedConn.connectionErrorOccurred(this, sqlE);
            throw sqlE;
        }
    }

    public int getTransactionIsolation() throws SQLException {
        try {
            Connection physicalConn = getPhysicalConnection();
            synchronized (physicalConn) {
                return physicalConn.getTransactionIsolation();
            }
        } catch (SQLException sqlE) {
            managedConn.connectionErrorOccurred(this, sqlE);
            throw sqlE;
        }
    }

    public SQLWarning getWarnings() throws SQLException {
        try {
            Connection physicalConn = getPhysicalConnection();
            synchronized (physicalConn) {
                return physicalConn.getWarnings();
            }
        } catch (SQLException sqlE) {
            managedConn.connectionErrorOccurred(this, sqlE);
            throw sqlE;
        }
    }

    public void clearWarnings() throws SQLException {
        try {
            Connection physicalConn = getPhysicalConnection();
            synchronized (physicalConn) {
                physicalConn.clearWarnings();
            }
        } catch (SQLException sqlE) {
            managedConn.connectionErrorOccurred(this, sqlE);
            throw sqlE;
        }
    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency)
            throws SQLException {
        try {
            Connection physicalConn = getPhysicalConnection();
            synchronized (physicalConn) {
                return new JdbcStatement(this, physicalConn.createStatement(resultSetType, resultSetConcurrency));
            }
        } catch (SQLException sqlE) {
            managedConn.connectionErrorOccurred(this, sqlE);
            throw sqlE;
        }
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
            throws SQLException {
        log.trace("preparing statement: " + sql + ", resultSetType: " + resultSetType + ", resultSetConcurrency" + resultSetConcurrency);
        try {
            Connection physicalConn = getPhysicalConnection();
            synchronized (physicalConn) {
                return new JdbcPreparedStatement(this, physicalConn.prepareStatement(sql, resultSetType, resultSetConcurrency));
            }
        } catch (SQLException sqlE) {
            managedConn.connectionErrorOccurred(this, sqlE);
            throw sqlE;
        }
    }

    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        log.trace("Preparing call: " + sql);
        try {
            Connection physicalConn = getPhysicalConnection();
            synchronized (physicalConn) {
                return physicalConn.prepareCall(sql, resultSetType, resultSetConcurrency);
            }
        } catch (SQLException sqlE) {
            managedConn.connectionErrorOccurred(this, sqlE);
            throw sqlE;
        }
    }

    public java.util.Map getTypeMap() throws SQLException {
        try {
            Connection physicalConn = getPhysicalConnection();
            synchronized (physicalConn) {
                return physicalConn.getTypeMap();
            }
        } catch (SQLException sqlE) {
            managedConn.connectionErrorOccurred(this, sqlE);
            throw sqlE;
        }
    }

    public void setTypeMap(java.util.Map map) throws SQLException {
        try {
            Connection physicalConn = getPhysicalConnection();
            synchronized (physicalConn) {
                physicalConn.setTypeMap(map);
            }
        } catch (SQLException sqlE) {
            managedConn.connectionErrorOccurred(this, sqlE);
            throw sqlE;
        }
    }

    /**
     * JDBC 3
     */
    public void setHoldability(int holdability) throws java.sql.SQLException {
        throw new SQLException("method setHoldability not implemented");
    }

    public int getHoldability() throws java.sql.SQLException {
        throw new SQLException("method getHoldability not implemented");
    }

    public java.sql.Savepoint setSavepoint() throws java.sql.SQLException {
        throw new SQLException("method not implemented");
    }

    public java.sql.Savepoint setSavepoint(String name) throws java.sql.SQLException {
        throw new SQLException("method not implemented");
    }

    public void rollback(java.sql.Savepoint savepoint) throws java.sql.SQLException {
        throw new SQLException("method not implemented");
    }

    public void releaseSavepoint(java.sql.Savepoint savepoint) throws java.sql.SQLException {
        throw new SQLException("method not implemented");
    }

    public java.sql.Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws java.sql.SQLException {
        throw new SQLException("method not implemented");
    }

    public java.sql.PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws java.sql.SQLException {
        throw new SQLException("method not implemented");
    }

    public java.sql.PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws java.sql.SQLException {
        throw new SQLException("method not implemented");
    }

    public java.sql.PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws java.sql.SQLException {
        throw new SQLException("method not implemented");
    }

    public java.sql.PreparedStatement prepareStatement(String sql, String[] columnNames) throws java.sql.SQLException {
        throw new SQLException("method not implemented");
    }

    public java.sql.CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws java.sql.SQLException {
        throw new SQLException("method not implemented");
    }
}







