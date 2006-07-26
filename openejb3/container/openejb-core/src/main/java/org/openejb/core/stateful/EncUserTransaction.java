package org.openejb.core.stateful;

import org.openejb.core.Operations;
import org.openejb.core.ivm.naming.ObjectReference;

import javax.naming.NameNotFoundException;

/*
  This class is a wrapper for CoreUserTransaction reference in the 
  JNDI ENC of a stateful bean.  When the getObject( ) method is invoked the 
  Operation is checked to ensure that its is allowed for the bean's current state.
*/

public class EncUserTransaction extends org.openejb.core.ivm.naming.ENCReference {

    /*
    * This constructor take a new CoreUserTransaction object as the object reference
    */
    public EncUserTransaction(org.openejb.core.CoreUserTransaction reference) {
        super(new ObjectReference(reference));
    }

    /*
    * This method is invoked by the ENCReference super class each time its 
    * getObject() method is called within the container system.  This checkOperation
    * method ensures that the stateful bean is in the correct state before the super
    * class can return the requested reference object.
    */
    public void checkOperation(byte operation) throws NameNotFoundException {
        if (operation == Operations.OP_SET_CONTEXT || operation == Operations.OP_AFTER_COMPLETION || operation == Operations.OP_BEFORE_COMPLETION)
        {
            throw new NameNotFoundException("Operation Not Allowed");
        }

    }

}
