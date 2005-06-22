package org.openejb.test;

import java.sql.SQLException;


/**
 *
 */
public class DerbyTestDatabase extends AbstractTestDatabase {
    private static final String CREATE_ACCOUNT = "CREATE TABLE account ( ssn VARCHAR(25), first_name VARCHAR(256), last_name VARCHAR(256), balance integer)";
    private static final String DROP_ACCOUNT = "DROP TABLE account";

    private static final String CREATE_ENTITY = "CREATE TABLE entity ( id integer GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), first_name VARCHAR(256), last_name VARCHAR(256) )";
    private static final String DROP_ENTITY = "DROP TABLE entity";

    private static final String CREATE_ENTITY_EXPLICIT_PK = "CREATE TABLE entity_explicit_pk ( id integer, first_name VARCHAR(256), last_name VARCHAR(256) )";
    private static final String DROP_ENTITY_EXPLICIT_PK = "DROP TABLE entity_explicit_pk";
    
    private static final String CREATE_ADDRESS = "CREATE TABLE address (id INTEGER, street VARCHAR(256), city VARCHAR(256))";
    private static final String DROP_ADDRESS = "DROP TABLE address";
    
    private static final String CREATE_LINE_ITEM = "CREATE TABLE line_item (id INTEGER, quantity INTEGER, fk_order INTEGER, fk_product INTEGER)";
    private static final String DROP_LINE_ITEM = "DROP TABLE line_item";

    private static final String CREATE_ORDER = "CREATE TABLE order_table (id INTEGER, reference VARCHAR(256), fk_shipping_address INTEGER, fk_billing_address INTEGER)";
    private static final String DROP_ORDER = "DROP TABLE order_table";

    private static final String CREATE_PRODUCT = "CREATE TABLE product (id INTEGER, name VARCHAR(256), product_type VARCHAR(256))";
    private static final String DROP_PRODUCT = "DROP TABLE product";

    static {
        System.setProperty("noBanner", "true");
    }

    protected String getCreateAccount() {
        return CREATE_ACCOUNT;
    }

    protected String getDropAccount() {
        return DROP_ACCOUNT;
    }

    protected String getCreateEntity() {
        return CREATE_ENTITY;
    }

    protected String getDropEntity() {
        return DROP_ENTITY;
    }

    protected String getCreateEntityExplictitPK() {
        return CREATE_ENTITY_EXPLICIT_PK;
    }

    protected String getDropEntityExplicitPK() {
        return DROP_ENTITY_EXPLICIT_PK;
    }
    
    public void createCMP2Model() throws SQLException {
        executeStatementIgnoreErrors(DROP_ACCOUNT);
        executeStatement(CREATE_ACCOUNT);

        executeStatementIgnoreErrors(DROP_ADDRESS);
        executeStatement(CREATE_ADDRESS);

        executeStatementIgnoreErrors(DROP_LINE_ITEM);
        executeStatement(CREATE_LINE_ITEM);

        executeStatementIgnoreErrors(DROP_ORDER);
        executeStatement(CREATE_ORDER);
        
        executeStatementIgnoreErrors(DROP_PRODUCT);
        executeStatement(CREATE_PRODUCT);
    }
    
    public void dropCMP2Model() throws SQLException {
        executeStatementIgnoreErrors(DROP_ACCOUNT);

        executeStatementIgnoreErrors(DROP_ADDRESS);

        executeStatementIgnoreErrors(DROP_LINE_ITEM);

        executeStatementIgnoreErrors(DROP_ORDER);
        
        executeStatementIgnoreErrors(DROP_PRODUCT);
    }
}



