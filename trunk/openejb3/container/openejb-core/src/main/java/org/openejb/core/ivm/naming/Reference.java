package org.openejb.core.ivm.naming;

import javax.naming.NamingException;
/*
  This interface is implemented by special wrappers for EJB references and
  resource references. When the getObject( ) method is invoked the Operation
  is checked to ensure that its is allowed for the bean's current state.

  In addition, dynamic resolution and special conditions can be encapsulated
  in the implementation object.

*/

public interface Reference {
    public Object getObject() throws NamingException;
}