package org.openejb.corba.idl.CosTransactions;

/**
* org/apache/geronimo/interop/CosTransactions/HeuristicRollbackHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.1"
* from C:/dev/corba/geronimo/trunk/modules/interop/src/idl/CosTransactions.idl
* Saturday, March 12, 2005 1:30:01 PM EST
*/

public final class HeuristicRollbackHolder implements org.omg.CORBA.portable.Streamable
{
  public org.openejb.corba.idl.CosTransactions.HeuristicRollback value = null;

  public HeuristicRollbackHolder ()
  {
  }

  public HeuristicRollbackHolder (org.openejb.corba.idl.CosTransactions.HeuristicRollback initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = org.openejb.corba.idl.CosTransactions.HeuristicRollbackHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    org.openejb.corba.idl.CosTransactions.HeuristicRollbackHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return org.openejb.corba.idl.CosTransactions.HeuristicRollbackHelper.type ();
  }

}
