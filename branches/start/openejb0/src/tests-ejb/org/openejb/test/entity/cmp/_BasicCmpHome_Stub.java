package org.openejb.test.entity.cmp;

//
// Interface definition : BasicCmpHome
//
// @author OpenORB Compiler
//
public class _BasicCmpHome_Stub extends javax.rmi.CORBA.Stub
		implements BasicCmpHome
{

	static final String[] _ids_list =
	{
		"RMI:org.openejb.test.entity.cmp.BasicCmpHome:0000000000000000", 
		"RMI:javax.ejb.EJBHome:0000000000000000"
	};

	public String[] _ids()
	{
		return _ids_list;
	}

	final public static java.lang.Class _opsClass = BasicCmpHome.class;

	//
	// Operation create
	//
	public org.openejb.test.entity.cmp.BasicCmpObject create(java.lang.String arg0)
		throws javax.ejb.CreateException, java.rmi.RemoteException
	{
		while( true )
		{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("create",true);
					_output.write_value((java.io.Serializable)arg0,java.lang.String.class);
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					org.openejb.test.entity.cmp.BasicCmpObject _arg_ret = ( org.openejb.test.entity.cmp.BasicCmpObject ) javax.rmi.PortableRemoteObject.narrow(_input.read_Object(), org.openejb.test.entity.cmp.BasicCmpObject.class);
					return _arg_ret;
				}
				catch( org.omg.CORBA.portable.RemarshalException _exception )
				{
					continue;
				}
				catch( org.omg.CORBA.portable.ApplicationException _exception )
				{
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) _exception.getInputStream();
					java.lang.String _exception_id = _input.read_string();
					if ( _exception_id.equals("RMI:javax.ejb.CreateException:0000000000280A69:0000000000000000") )
					{
						throw ( javax.ejb.CreateException ) _input.read_value();
					}

					throw new java.rmi.UnexpectedException(_exception_id);
				}
				catch( org.omg.CORBA.SystemException _exception )
				{
					throw javax.rmi.CORBA.Util.mapSystemException(_exception);
				}
				finally
				{
					this._releaseReply(_input);
				}
		}
	}

	//
	// Operation findByPrimaryKey
	//
	public org.openejb.test.entity.cmp.BasicCmpObject findByPrimaryKey(java.lang.Integer arg0)
		throws javax.ejb.FinderException, java.rmi.RemoteException
	{
		while( true )
		{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("findByPrimaryKey",true);
					_output.write_value((java.io.Serializable)arg0,java.lang.Integer.class);
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					org.openejb.test.entity.cmp.BasicCmpObject _arg_ret = ( org.openejb.test.entity.cmp.BasicCmpObject ) javax.rmi.PortableRemoteObject.narrow(_input.read_Object(), org.openejb.test.entity.cmp.BasicCmpObject.class);
					return _arg_ret;
				}
				catch( org.omg.CORBA.portable.RemarshalException _exception )
				{
					continue;
				}
				catch( org.omg.CORBA.portable.ApplicationException _exception )
				{
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) _exception.getInputStream();
					java.lang.String _exception_id = _input.read_string();
					if ( _exception_id.equals("RMI:javax.ejb.FinderException:00000000001360E2:0000000000000000") )
					{
						throw ( javax.ejb.FinderException ) _input.read_value();
					}

					throw new java.rmi.UnexpectedException(_exception_id);
				}
				catch( org.omg.CORBA.SystemException _exception )
				{
					throw javax.rmi.CORBA.Util.mapSystemException(_exception);
				}
				finally
				{
					this._releaseReply(_input);
				}
		}
	}

	//
	// Operation findEmptyCollection
	//
	public java.util.Collection findEmptyCollection()
		throws javax.ejb.FinderException, java.rmi.RemoteException
	{
		while( true )
		{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("findEmptyCollection",true);
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					java.util.Collection _arg_ret = ( java.util.Collection )((org.omg.CORBA_2_3.portable.InputStream)_input).read_value(java.util.Collection.class);
					return _arg_ret;
				}
				catch( org.omg.CORBA.portable.RemarshalException _exception )
				{
					continue;
				}
				catch( org.omg.CORBA.portable.ApplicationException _exception )
				{
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) _exception.getInputStream();
					java.lang.String _exception_id = _input.read_string();
					if ( _exception_id.equals("RMI:javax.ejb.FinderException:00000000001360E2:0000000000000000") )
					{
						throw ( javax.ejb.FinderException ) _input.read_value();
					}

					throw new java.rmi.UnexpectedException(_exception_id);
				}
				catch( org.omg.CORBA.SystemException _exception )
				{
					throw javax.rmi.CORBA.Util.mapSystemException(_exception);
				}
				finally
				{
					this._releaseReply(_input);
				}
		}
	}

	//
	// Operation sum
	//
	public int sum(int arg0, int arg1)
		throws java.rmi.RemoteException
	{
		while( true )
		{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("sum",true);
					_output.write_long(arg0);
					_output.write_long(arg1);
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					int _arg_ret = _input.read_long();
					return _arg_ret;
				}
				catch( org.omg.CORBA.portable.RemarshalException _exception )
				{
					continue;
				}
				catch( org.omg.CORBA.portable.ApplicationException _exception )
				{
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) _exception.getInputStream();
					java.lang.String _exception_id = _input.read_string();
					throw new java.rmi.UnexpectedException(_exception_id);
				}
				catch( org.omg.CORBA.SystemException _exception )
				{
					throw javax.rmi.CORBA.Util.mapSystemException(_exception);
				}
				finally
				{
					this._releaseReply(_input);
				}
		}
	}

	//
	// Attribute getEJBMetaData
	//
	public javax.ejb.EJBMetaData getEJBMetaData()
		throws java.rmi.RemoteException
	{
		while( true )
		{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("_get_EJBMetaData",true);
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					javax.ejb.EJBMetaData _arg_ret = ( javax.ejb.EJBMetaData )((org.omg.CORBA_2_3.portable.InputStream)_input).read_value(javax.ejb.EJBMetaData.class);
					return _arg_ret;
				}
				catch( org.omg.CORBA.portable.RemarshalException _exception )
				{
					continue;
				}
				catch( org.omg.CORBA.portable.ApplicationException _exception )
				{
					java.lang.String _exception_id = _exception.getId();
					throw new org.omg.CORBA.UNKNOWN("Unexcepected User Exception: "+ _exception_id);
				}
				catch( org.omg.CORBA.SystemException _exception )
				{
					throw javax.rmi.CORBA.Util.mapSystemException(_exception);
				}
				finally
				{
					this._releaseReply(_input);
				}
		}
	}

	//
	// Attribute getHomeHandle
	//
	public javax.ejb.HomeHandle getHomeHandle()
		throws java.rmi.RemoteException
	{
		while( true )
		{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("_get_homeHandle",true);
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					javax.ejb.HomeHandle _arg_ret = ( javax.ejb.HomeHandle ) javax.rmi.PortableRemoteObject.narrow(((org.omg.CORBA_2_3.portable.InputStream)_input).read_abstract_interface(), javax.ejb.HomeHandle.class);
					return _arg_ret;
				}
				catch( org.omg.CORBA.portable.RemarshalException _exception )
				{
					continue;
				}
				catch( org.omg.CORBA.portable.ApplicationException _exception )
				{
					java.lang.String _exception_id = _exception.getId();
					throw new org.omg.CORBA.UNKNOWN("Unexcepected User Exception: "+ _exception_id);
				}
				catch( org.omg.CORBA.SystemException _exception )
				{
					throw javax.rmi.CORBA.Util.mapSystemException(_exception);
				}
				finally
				{
					this._releaseReply(_input);
				}
		}
	}

	//
	// Operation remove__javax_ejb_Handle
	//
	public void remove(javax.ejb.Handle arg0)
		throws javax.ejb.RemoveException, java.rmi.RemoteException
	{
		while( true )
		{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("remove__javax_ejb_Handle",true);
					javax.rmi.CORBA.Util.writeAbstractObject( _output, arg0 );
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					return;
				}
				catch( org.omg.CORBA.portable.RemarshalException _exception )
				{
					continue;
				}
				catch( org.omg.CORBA.portable.ApplicationException _exception )
				{
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) _exception.getInputStream();
					java.lang.String _exception_id = _input.read_string();
					if ( _exception_id.equals("RMI:javax.ejb.RemoveException:000000000064C34E:0000000000000000") )
					{
						throw ( javax.ejb.RemoveException ) _input.read_value();
					}

					throw new java.rmi.UnexpectedException(_exception_id);
				}
				catch( org.omg.CORBA.SystemException _exception )
				{
					throw javax.rmi.CORBA.Util.mapSystemException(_exception);
				}
				finally
				{
					this._releaseReply(_input);
				}
		}
	}

	//
	// Operation remove__java_lang_Object
	//
	public void remove(java.lang.Object arg0)
		throws javax.ejb.RemoveException, java.rmi.RemoteException
	{
		while( true )
		{
				org.omg.CORBA_2_3.portable.InputStream _input = null;
				try
				{
					org.omg.CORBA_2_3.portable.OutputStream _output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request("remove__java_lang_Object",true);
					javax.rmi.CORBA.Util.writeAny(_output,arg0);
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) this._invoke(_output);
					return;
				}
				catch( org.omg.CORBA.portable.RemarshalException _exception )
				{
					continue;
				}
				catch( org.omg.CORBA.portable.ApplicationException _exception )
				{
					_input = ( org.omg.CORBA_2_3.portable.InputStream ) _exception.getInputStream();
					java.lang.String _exception_id = _input.read_string();
					if ( _exception_id.equals("RMI:javax.ejb.RemoveException:000000000064C34E:0000000000000000") )
					{
						throw ( javax.ejb.RemoveException ) _input.read_value();
					}

					throw new java.rmi.UnexpectedException(_exception_id);
				}
				catch( org.omg.CORBA.SystemException _exception )
				{
					throw javax.rmi.CORBA.Util.mapSystemException(_exception);
				}
				finally
				{
					this._releaseReply(_input);
				}
		}
	}

}