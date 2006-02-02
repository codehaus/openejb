package org.openejb.test.beans;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.ejb.EntityContext;
import javax.ejb.FinderException;
import javax.naming.InitialContext;

public class EmployeeBean implements javax.ejb.EntityBean {
    int id;
    String lastName;
    String firstName;
    
    EntityContext ejbContext;
    
    public int ejbHomeSum(int one, int two) {
        return one+two;
    }
    public Integer ejbFindByPrimaryKey(Integer primaryKey)
    throws javax.ejb.FinderException{
        boolean found = false;
        try{
        InitialContext jndiContext = new InitialContext( ); 
        
        javax.sql.DataSource ds = 
        (javax.sql.DataSource)jndiContext.lookup("java:comp/env/jdbc/orders");
        
        Connection con = ds.getConnection();
        
        
        PreparedStatement stmt = con.prepareStatement("select * from Employees where EmployeeID = ?");
        stmt.setInt(1, primaryKey.intValue());
        ResultSet rs = stmt.executeQuery();
        found = rs.next();
        con.close();
        }catch(Exception e){
            e.printStackTrace();
            throw new FinderException("FindByPrimaryKey failed");
        }
        
        if(found)
            return primaryKey;
        else
            throw new javax.ejb.ObjectNotFoundException();
        
        
    }
    public java.util.Collection ejbFindAll( ) throws FinderException{
        try{
        InitialContext jndiContext = new InitialContext( ); 
        
        javax.sql.DataSource ds = 
        (javax.sql.DataSource)jndiContext.lookup("java:comp/env/jdbc/orders");
        
        Connection con = ds.getConnection();
        
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("select EmployeeID from Employees");
        java.util.Vector keys = new java.util.Vector();
        while(rs.next()){
            keys.addElement(new Integer(rs.getInt("EmployeeID")));
        }
        con.close();
        return keys;
        }catch(Exception e){
            e.printStackTrace();
            throw new FinderException("FindAll failed");
        }
    }
    
    public Integer ejbCreate(String fname, String lname)
    throws javax.ejb.CreateException{
        try{
        lastName = lname;
        firstName = fname;
        
        InitialContext jndiContext = new InitialContext( ); 
 
        javax.sql.DataSource ds = 
        (javax.sql.DataSource)jndiContext.lookup("java:comp/env/jdbc/orders");
        
        Connection con = ds.getConnection();
        
        
        PreparedStatement stmt = con.prepareStatement("insert into Employees (FirstName, LastName) values (?,?)");
        stmt.setString(1, firstName);
        stmt.setString(2, lastName);
        stmt.executeUpdate();
        
        stmt = con.prepareStatement("select EmployeeID from Employees where FirstName = ? AND LastName = ?");
        stmt.setString(1, firstName);
        stmt.setString(2, lastName);
        ResultSet set = stmt.executeQuery();
        while(set.next())
            id = set.getInt("EmployeeID");
        con.close();
        
        return new Integer(id);
        
        }catch(Exception e){
            e.printStackTrace();
            throw new javax.ejb.CreateException("can't create");
        }
    }
    public String getLastName( ){
        return lastName;
    }
    public String getFirstName( ){
        return firstName;
    }
    public void setLastName(String lname){
        lastName = lname;
    }
    public void setFirstName(String fname){
        firstName = fname;
    }
    
    public void ejbLoad( ){
        try{
        InitialContext jndiContext = new InitialContext( ); 
        
        javax.sql.DataSource ds = 
        (javax.sql.DataSource)jndiContext.lookup("java:comp/env/jdbc/orders");
        
        Connection con = ds.getConnection();
        
        
        PreparedStatement stmt = con.prepareStatement("select * from Employees where EmployeeID = ?");
        Integer primaryKey = (Integer)ejbContext.getPrimaryKey();
        stmt.setInt(1, primaryKey.intValue());
        ResultSet rs = stmt.executeQuery();
        while(rs.next()){
            lastName = rs.getString("LastName");
            firstName = rs.getString("FirstName");
        }
        con.close();
        
        }catch(Exception e){
            e.printStackTrace();
        }
        
    }
    
    public void ejbStore( ){
        try{
        InitialContext jndiContext = new InitialContext( ); 
        
        javax.sql.DataSource ds = 
        (javax.sql.DataSource)jndiContext.lookup("java:comp/env/jdbc/orders");
        Connection con = ds.getConnection();
        
        PreparedStatement stmt = con.prepareStatement("update Employees set FirstName = ?, LastName = ? where EmployeeID = ?");
        stmt.setString(1, firstName);
        stmt.setString(2, lastName);
        stmt.setInt(3, id);
        stmt.execute();
        con.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        
    }
    
    public void ejbActivate( ){}
    public void ejbPassivate( ){}
    public void ejbRemove( ){
                
        try{
            InitialContext jndiContext = new InitialContext( ); 
            
            javax.sql.DataSource ds = 
            (javax.sql.DataSource)jndiContext.lookup("java:comp/env/jdbc/orders");
            
            Connection con = ds.getConnection();
            
            
            PreparedStatement stmt = con.prepareStatement("delete from Employees where EmployeeID = ?");
            Integer primaryKey = (Integer)ejbContext.getPrimaryKey();
            stmt.setInt(1, primaryKey.intValue());
            stmt.executeUpdate();
            con.close();
        
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public void setEntityContext(javax.ejb.EntityContext cntx){
        ejbContext = cntx;
    }
    public void unsetEntityContext(){}
}
    
        