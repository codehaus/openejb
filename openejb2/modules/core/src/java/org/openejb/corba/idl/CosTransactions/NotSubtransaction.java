package org.openejb.corba.idl.CosTransactions;


/**
* org/apache/geronimo/interop/CosTransactions/NotSubtransaction.java .
* Generated by the IDL-to-Java compiler (portable), version "3.1"
* from C:/dev/corba/geronimo/trunk/modules/interop/src/idl/CosTransactions.idl
* Saturday, March 12, 2005 1:30:01 PM EST
*/

public final class NotSubtransaction extends org.omg.CORBA.UserException
{

  public NotSubtransaction ()
  {
    super(NotSubtransactionHelper.id());
  } // ctor


  public NotSubtransaction (String $reason)
  {
    super(NotSubtransactionHelper.id() + "  " + $reason);
  } // ctor

} // class NotSubtransaction