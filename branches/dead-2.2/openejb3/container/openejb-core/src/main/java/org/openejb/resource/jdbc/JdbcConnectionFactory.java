package org.openejb.resource.jdbc;

import javax.naming.Reference;
import javax.resource.ResourceException;
import javax.resource.spi.ApplicationServerInternalException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.ResourceAllocationException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

public class JdbcConnectionFactory implements javax.sql.DataSource, javax.resource.Referenceable, java.io.Serializable {
    private Reference jndiReference;

    private final transient ManagedConnectionFactory managedConnectionFactory;
    private final transient ConnectionManager connectionManager;
    private final String jdbcUrl;
    private final String jdbcDriver;
    private final String defaultPassword;
    private final String defaultUserName;
    private transient PrintWriter logWriter;
    private int logTimeout = 0;

    public JdbcConnectionFactory(ManagedConnectionFactory managedConnectionFactory,
                                 ConnectionManager connectionManager, String jdbcUrl,
                                 String jdbcDriver, String defaultPassword, String defaultUserName) throws ResourceException {
        this.managedConnectionFactory = managedConnectionFactory;
        this.connectionManager = connectionManager;
        this.logWriter = managedConnectionFactory.getLogWriter();
        this.jdbcUrl = jdbcUrl;
        this.jdbcDriver = jdbcDriver;
        this.defaultPassword = defaultPassword;
        this.defaultUserName = defaultUserName;
    }

    public void setReference(Reference jndiReference) {
        this.jndiReference = jndiReference;
    }

    public Reference getReference() {
        return jndiReference;
    }

    public Connection getConnection() throws SQLException {
        return getConnection(defaultUserName, defaultPassword);
    }

    public Connection getConnection(java.lang.String username, java.lang.String password) throws SQLException {
        return getConnection(new JdbcConnectionRequestInfo(username, password, jdbcDriver, jdbcUrl));
    }

    protected Connection getConnection(JdbcConnectionRequestInfo connectionRequestInfo) throws SQLException {

        try {
            return (Connection) connectionManager.allocateConnection(managedConnectionFactory, connectionRequestInfo);
        } catch (ApplicationServerInternalException e) {
            throw convertToSQLException(e, "Application error in ContainerManager");
        } catch (javax.resource.spi.SecurityException e) {
            throw convertToSQLException(e, "Authentication error. Invalid credentials");
        } catch (ResourceAdapterInternalException e) {
            throw convertToSQLException(e, "JDBC Connection problem");
        } catch (ResourceAllocationException e) {
            throw convertToSQLException(e, "JDBC Connection could not be obtained");
        } catch (ResourceException e) {
            throw convertToSQLException(e, "JDBC Connection Factory problem");
        }
    }

    private SQLException convertToSQLException(ResourceException e, String error) {
        Throwable cause = e.getCause();
        if (cause instanceof SQLException) {
            return (SQLException) cause;
        } else {
            String message = ((cause != null) ? cause.getMessage() : "");
            return (SQLException) new SQLException("Error code: " + e.getErrorCode() + error + message).initCause(e);
        }
    }

    public int getLoginTimeout() {
        return logTimeout;
    }

    public java.io.PrintWriter getLogWriter() {
        return logWriter;
    }

    public void setLoginTimeout(int seconds) {

        logTimeout = seconds;
    }

    public void setLogWriter(java.io.PrintWriter out) {
        logWriter = out;
    }
}
