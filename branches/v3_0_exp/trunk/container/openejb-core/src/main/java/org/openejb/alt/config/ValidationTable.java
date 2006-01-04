package org.openejb.alt.config;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.openejb.util.JarUtils;
import org.openejb.ClassLoaderUtil;
import org.openejb.loader.SystemInstance;

public class ValidationTable {

    private static ValidationTable table;

    private static final String _createTable = "CREATE TABLE validation ( jar_path CHAR(150) PRIMARY KEY, last_validated CHAR(13), validator_version CHAR(20))";
    private static final String _selectValidated = "select last_validated, validator_version  from validation where jar_path = ?";
    private static final String _updateValidated = "update  validation set last_validated = (?), validator_version = ? where jar_path = ?";
    private static final String _insertValidated = "insert into validation (jar_path, last_validated, validator_version) values (?,?,?)";

    private static final String jdbcDriver = "org.enhydra.instantdb.jdbc.idbDriver";
    private static final String jdbcUrl = "jdbc:idb:conf/registry.properties";
    private static final String userName = "system";
    private static final String password = "system";

    private Connection conn;

    private ValidationTable() {
        try {

            ClassLoader cl = ClassLoaderUtil.getContextClassLoader();
            Class.forName(jdbcDriver, true, cl);

            conn = getConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {

            Statement stmt = conn.createStatement();
            stmt.execute(_createTable);
            stmt.close();
        } catch (Exception e) {

        } finally {
            try {
                conn.close();
            } catch (Exception e) {
            }
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, userName, password);
    }

    public static ValidationTable getInstance() {
        if (table == null) {
            table = new ValidationTable();
        }

        return table;
    }

    public boolean isValidated(String jarFile) {
        try {
            File jar = SystemInstance.get().getBase().getFile(jarFile);
            long lastModified = jar.lastModified();
            long lastValidated = getLastValidated(jar);

            return (lastValidated > lastModified);
        } catch (Exception e) {
            return false;
        }
    }

    public void setValidated(String jarFile) {
        setLastValidated(jarFile, System.currentTimeMillis());
    }

    public long getLastValidated(File jar) {
        long validated = 0L;
        try {
            conn = getConnection();

            String jarFileURL = jar.toURL().toExternalForm();

            PreparedStatement stmt = conn.prepareStatement(_selectValidated);
            stmt.setString(1, jarFileURL);

            ResultSet results = stmt.executeQuery();
            if (results.next()) {
                String version = results.getString(2);

                if (version == null || version.equals(getVersion())) {
                    validated = results.getLong(1);

                }
            }
            stmt.close();
        } catch (Exception e) {

        } finally {
            try {
                conn.close();
            } catch (Exception e) {
            }
        }
        return validated;
    }

    private long _getLastValidated(String jarFileURL) {
        long validated = 0L;
        try {
            conn = getConnection();

            PreparedStatement stmt = conn.prepareStatement(_selectValidated);
            stmt.setString(1, jarFileURL);

            ResultSet results = stmt.executeQuery();
            if (results.next()) {
                validated = results.getLong(1);
            }
            stmt.close();
        } catch (Exception e) {

        } finally {
            try {
                conn.close();
            } catch (Exception e) {
            }
        }
        return validated;
    }

    public void setLastValidated(String jarFile, long timeValidated) {
        try {
            conn = getConnection();
            File jar = SystemInstance.get().getBase().getFile(jarFile);
            String jarFileURL = jar.toURL().toExternalForm();

            PreparedStatement stmt = null;
            if (_getLastValidated(jarFileURL) != 0L) {
                stmt = conn.prepareStatement(_updateValidated);
                stmt.setLong(1, timeValidated);
                stmt.setString(2, getVersion());
                stmt.setString(3, jarFileURL);
            } else {
                stmt = conn.prepareStatement(_insertValidated);
                stmt.setString(1, jarFileURL);
                stmt.setLong(2, timeValidated);
                stmt.setString(3, getVersion());
            }

            stmt.executeUpdate();
            stmt.close();
        } catch (Exception e) {

        } finally {
            try {
                conn.close();
            } catch (Exception e) {
            }
        }
    }

    private String version = null;

    private String getVersion() {
        if (version == null) {
            /*
             * Output startup message
             */
            Properties versionInfo = new Properties();

            try {
                JarUtils.setHandlerSystemProperty();
                versionInfo.load(new URL("resource:/openejb-version.properties").openConnection().getInputStream());
            } catch (java.io.IOException e) {
            }
            version = (String) versionInfo.get("version");
        }
        return version;
    }
}