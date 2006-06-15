package org.openejb.resource.jdbc;

import org.openejb.loader.SystemInstance;

import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.EISSystemException;
import javax.resource.ResourceException;
import javax.security.auth.Subject;
import java.util.Set;
import java.util.Properties;
import java.io.PrintWriter;
import java.io.File;
import java.sql.DriverManager;

public class ManagedConnectionFactoryPathHack extends ManagedConnectionFactoryAdapter {

    public ManagedConnectionFactoryPathHack(ManagedConnectionFactory factory) {
        super(factory);
    }

    public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo connectionRequestInfo) throws ResourceException {
        Properties systemProperties = System.getProperties();
        synchronized (systemProperties) {
            String userDir = systemProperties.getProperty("user.dir");
            try {
                File base = SystemInstance.get().getBase().getDirectory();
                systemProperties.setProperty("user.dir", base.getAbsolutePath());
                return super.createManagedConnection(subject, connectionRequestInfo);
            } finally {
                systemProperties.setProperty("user.dir", userDir);
            }
        }
    }
}
