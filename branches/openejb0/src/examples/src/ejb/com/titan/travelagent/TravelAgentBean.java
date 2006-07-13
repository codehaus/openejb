package com.titan.travelagent;

import java.util.Vector;

import javax.ejb.EJBException;
import javax.naming.InitialContext;

import com.titan.cabin.CabinHomeRemote;
import com.titan.cabin.CabinRemote;

public class TravelAgentBean implements javax.ejb.SessionBean 
{

   public void ejbCreate() 
   {
      // Do nothing.
   }

   public String [] listCabins(int shipID, int bedCount) 
   {
      try 
      {
	 javax.naming.Context jndiContext = new InitialContext();
	 Object obj = 
	    jndiContext.lookup("java:comp/env/ejb/titan/CabinEJB");


	 CabinHomeRemote home = (CabinHomeRemote)
	    javax.rmi.PortableRemoteObject.narrow(obj,CabinHomeRemote.class);

	 Vector vect = new Vector();
	 for (int i = 1; ; i++) 
	 {
	    Integer pk = new Integer(i);
	    CabinRemote cabin = null;
	    try 
	    {
	       cabin = home.findByPrimaryKey(pk);
	    } 
	    catch(javax.ejb.FinderException fe) 
	    {
	       System.out.println("Caught exception: "+fe.getMessage()+" for pk="+i); 
	       break;
	    }
	    // Check to see if the bed count and ship ID match.
	    if (cabin != null &&
		cabin.getShipId() == shipID && 
		cabin.getBedCount() == bedCount) 
	    {
	       String details = 
		  i+","+cabin.getName()+","+cabin.getDeckLevel();
	       vect.addElement(details);
	    }
	 }
        
	 String [] list = new String[vect.size()];
	 vect.copyInto(list);
	 return list;
       
      } 
      catch(Exception e) 
      {
	 throw new EJBException(e);
      }    
   }

   public void ejbRemove(){}
   public void ejbActivate(){}
   public void ejbPassivate(){}
   public void setSessionContext(javax.ejb.SessionContext cntx){}
}
