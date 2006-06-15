package org.openejb.test.beans;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.ejb.EJBException;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;
import javax.sql.DataSource;

public class DatabaseBean implements javax.ejb.SessionBean {
    
    public SessionContext context;
    public InitialContext jndiContext;
    
    public void ejbCreate( ) throws javax.ejb.CreateException{
        try{        
            jndiContext = new InitialContext();
        } catch (Exception e){
            throw new EJBException(e.getMessage());
        }
    }
    
    public void executeQuery(String statement) throws java.sql.SQLException{
        try{        

        DataSource ds = (DataSource)jndiContext.lookup("java:comp/env/database");
        Connection con = ds.getConnection();

        PreparedStatement stmt = con.prepareStatement(statement);
        ResultSet rs = stmt.executeQuery();
        
        con.close();
        } catch (Exception e){
            throw new EJBException("Cannot execute the statement: "+statement+ e.getMessage());
        }
    }
    
    public boolean execute(String statement) throws java.sql.SQLException{
        boolean retval;
        Connection con = null;
        try{        

        DataSource ds = (DataSource)jndiContext.lookup("java:comp/env/database");
        con = ds.getConnection();

        Statement stmt = con.createStatement();
        retval = stmt.execute(statement);
        
        } catch (javax.naming.NamingException e){
//        } catch (Exception e){
//            e.printStackTrace();
            //throw new RemoteException("Cannot execute the statement: "+statement, e);
            throw new EJBException("Cannot lookup the Database bean."+e.getMessage());
        } finally {
            if(con!=null) {
                con.close();
            }
        }
        return retval;
    }
    
    public void ejbPassivate( ){
        // never called
    }
    public void ejbActivate(){
        // never called
    }
    public void ejbRemove(){
    }
    
    public void setSessionContext(javax.ejb.SessionContext cntx){
        context = cntx;
    }
} 
   