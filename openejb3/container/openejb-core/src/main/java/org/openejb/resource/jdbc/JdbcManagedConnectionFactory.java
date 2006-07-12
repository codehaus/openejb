package org.openejb.resource.jdbc;

import org.openejb.core.EnvProps;
import org.openejb.util.Logger;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.security.auth.Subject;
import java.io.PrintWriter;
import java.util.Set;

public class JdbcManagedConnectionFactory implements javax.resource.spi.ManagedConnectionFactory, java.io.Serializable {
    private static final long serialVersionUID = 8797357228901190014L;
    protected Logger logger = Logger.getInstance("OpenEJB.connector", "org.openejb.alt.util.resources");
    private ManagedConnectionFactory factory;
    private String defaultUserName;
    private String defaultPassword;
    private String url;
    private String driver;

    public void init(java.util.Properties props) throws javax.resource.spi.ResourceAdapterInternalException {
        defaultUserName = props.getProperty(EnvProps.USER_NAME);
        defaultPassword = props.getProperty(EnvProps.PASSWORD);
        url = props.getProperty(EnvProps.JDBC_URL);
        driver = props.getProperty(EnvProps.JDBC_DRIVER);

        start();
    }

    public String getDefaultUserName() {
        return defaultUserName;
    }

    public void setDefaultUserName(String defaultUserName) {
        this.defaultUserName = defaultUserName;
    }

    public String getDefaultPassword() {
        return defaultPassword;
    }

    public void setDefaultPassword(String defaultPassword) {
        this.defaultPassword = defaultPassword;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public void start() throws ResourceAdapterInternalException {
        loadDriver(driver);

        factory = new BasicManagedConnectionFactory(this, driver, url, defaultUserName, defaultPassword);

        if (driver.equals("org.enhydra.instantdb.jdbc.idbDriver")) {
            factory = new InstantdbPropertiesHack(factory, url);
            factory = new ManagedConnectionFactoryPathHack(factory);
        }

        JdbcConnectionRequestInfo info = new JdbcConnectionRequestInfo(defaultUserName, defaultPassword, driver, url);
        ManagedConnection connection = null;
        try {
            connection = factory.createManagedConnection(null, info);
        } catch (Throwable e) {
            logger.error("Testing driver failed.  " + "[" + url + "]  "
                    + "Could not obtain a physical JDBC connection from the DriverManager."
                    + "\nThe error message was:\n" + e.getMessage() + "\nPossible cause:"
                    + "\n\to JDBC driver classes are not available to OpenEJB"
                    + "\n\to Relative paths are not resolved properly");
        } finally {
            if (connection != null) {
                try {
                    connection.destroy();
                } catch (ResourceException dontCare) {
                }
            }
        }
    }

    private void loadDriver(String driver) throws ResourceAdapterInternalException {
        try {
            ClassLoader classLoader = (ClassLoader) java.security.AccessController.doPrivileged(new java.security.PrivilegedAction() {
                public Object run() {
                    return Thread.currentThread().getContextClassLoader();
                }
            });
            Class.forName(driver, true, classLoader);
        } catch (ClassNotFoundException cnf) {
            throw new ResourceAdapterInternalException("JDBC Driver class \"" + driver + "\" not found by class loader", ErrorCode.JDBC_0002);
        }
    }

    public Object createConnectionFactory(ConnectionManager connectionManager) throws ResourceException {
        return factory.createConnectionFactory(connectionManager);
    }

    public Object createConnectionFactory() throws ResourceException {
        return factory.createConnectionFactory();
    }

    public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo connectionRequestInfo) throws ResourceException {
        return factory.createManagedConnection(subject, connectionRequestInfo);
    }

    public ManagedConnection matchManagedConnections(Set set, Subject subject, ConnectionRequestInfo connectionRequestInfo) throws ResourceException {
        return factory.matchManagedConnections(set, subject, connectionRequestInfo);
    }

    public void setLogWriter(PrintWriter printWriter) throws ResourceException {
        factory.setLogWriter(printWriter);
    }

    public PrintWriter getLogWriter() throws ResourceException {
        return factory.getLogWriter();
    }

    public int hashCode() {
        return factory.hashCode();
    }

    public boolean equals(Object o) {
        return factory.equals(o);
    }
}