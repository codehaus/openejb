package org.openejb.test;

import java.rmi.RemoteException;
import java.util.Properties;
import javax.naming.InitialContext;

import org.openejb.test.beans.Database;
import org.openejb.test.beans.DatabaseHome;

/**
 *
 */
public abstract class AbstractTestDatabase implements TestDatabase {

    protected Database database;
    protected InitialContext initialContext;

    protected abstract String getCreateAccount();
    protected abstract String getDropAccount();

    protected abstract String getCreateEntity();
    protected abstract String getDropEntity();

    protected abstract String getCreateEntityExplictitPK();
    protected abstract String getDropEntityExplicitPK();

    public void createEntityTable() throws java.sql.SQLException {
        executeStatementIgnoreErrors(getDropEntity());
        executeStatement(getCreateEntity());
    }

    public void dropEntityTable() throws java.sql.SQLException {
        executeStatement(getDropEntity());
    }

    public void createEntityExplicitePKTable() throws java.sql.SQLException {
        executeStatementIgnoreErrors(getDropEntityExplicitPK());
        executeStatement(getCreateEntityExplictitPK());
    }

    public void dropEntityExplicitePKTable() throws java.sql.SQLException {
        executeStatement(getDropEntityExplicitPK());
    }

    protected void executeStatementIgnoreErrors(String command) {
        try {
            getDatabase().execute(command);
        } catch (Exception e) {
            // not concerned
        }
    }

    protected void executeStatement(String command) throws java.sql.SQLException {
        try {
            getDatabase().execute(command);
        } catch (RemoteException re) {
            if (re.detail != null && re.detail instanceof java.sql.SQLException) {
                throw (java.sql.SQLException) re.detail;
            } else {
                throw new java.sql.SQLException("Cannot exectute statement: " + re.getMessage(), command);
            }
        }
    }

    public void createAccountTable() throws java.sql.SQLException {
        executeStatementIgnoreErrors(getDropAccount());
        executeStatement(getCreateAccount());
    }

    public void dropAccountTable() throws java.sql.SQLException {
        executeStatement(getDropAccount());
    }

    public void start() throws IllegalStateException {
        try {
            // @todo this is a hack that limits us to a single server 
//            Properties properties = TestManager.getServer().getContextEnvironment();
            Properties properties = new Properties();
            properties.put("test.server.class", "org.openejb.test.RemoteTestServer");
            properties.put("java.naming.factory.initial", "org.openejb.client.RemoteInitialContextFactory");
            properties.put("java.naming.provider.url", "127.0.0.1:4201");
            properties.put("java.naming.security.principal", "testuser");
            properties.put("java.naming.security.credentials", "testpassword");
            initialContext = new InitialContext(properties);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot create initial context: " + e.getClass().getName() + " " + e.getMessage());
        }
    }


    private Database getDatabase() {
        if (initialContext == null) {
            start();
        }
        if (database == null) {
            database = createDatabaseObject();
        }
        return database;
    }

    private Database createDatabaseObject() {
        Object obj = null;
        DatabaseHome databaseHome = null;
        Database database = null;
        try {
            /* Create database */
            obj = initialContext.lookup("client/tools/DatabaseHome");
            databaseHome = (DatabaseHome) javax.rmi.PortableRemoteObject.narrow(obj, DatabaseHome.class);
        } catch (Exception e) {
            throw (IllegalStateException)new IllegalStateException("Cannot find 'client/tools/DatabaseHome': "
                    + e.getMessage()).initCause(e);
        }
        try {
            database = databaseHome.create();
        } catch (Exception e) {
            throw (IllegalStateException)new IllegalStateException("Cannot start database: "
                    + e.getMessage()).initCause(e);
        }
        return database;
    }

    public void stop() throws IllegalStateException {
    }

    public void init(Properties props) throws IllegalStateException {
    }
}



