package org.openejb.corba.idl.CosTransactions;

/**
* org/apache/geronimo/interop/CosTransactions/StatusHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.1"
* from C:/dev/corba/geronimo/trunk/modules/interop/src/idl/CosTransactions.idl
* Saturday, March 12, 2005 1:30:01 PM EST
*/


// DATATYPES
public final class StatusHolder implements org.omg.CORBA.portable.Streamable
{
  public org.openejb.corba.idl.CosTransactions.Status value = null;

  public StatusHolder ()
  {
  }

  public StatusHolder (org.openejb.corba.idl.CosTransactions.Status initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = org.openejb.corba.idl.CosTransactions.StatusHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    org.openejb.corba.idl.CosTransactions.StatusHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return org.openejb.corba.idl.CosTransactions.StatusHelper.type ();
  }

}