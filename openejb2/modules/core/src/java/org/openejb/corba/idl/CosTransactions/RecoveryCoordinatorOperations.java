package org.openejb.corba.idl.CosTransactions;


/**
* org/apache/geronimo/interop/CosTransactions/RecoveryCoordinatorOperations.java .
* Generated by the IDL-to-Java compiler (portable), version "3.1"
* from C:/dev/corba/geronimo/trunk/modules/interop/src/idl/CosTransactions.idl
* Saturday, March 12, 2005 1:30:01 PM EST
*/

public interface RecoveryCoordinatorOperations 
{
  org.openejb.corba.idl.CosTransactions.Status replay_completion (org.openejb.corba.idl.CosTransactions.Resource r) throws org.openejb.corba.idl.CosTransactions.NotPrepared;
} // interface RecoveryCoordinatorOperations