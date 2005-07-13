/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact info@openejb.org.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE OPENEJB GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE OPENEJB GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2005 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.corba;

import java.lang.reflect.Method;
import java.rmi.AccessException;
import java.rmi.MarshalException;
import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.RemoveException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.TransactionRequiredException;
import javax.transaction.TransactionRolledbackException;
import javax.naming.Context;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.core.service.InvocationResult;
import org.apache.geronimo.naming.java.SimpleReadOnlyContext;
import org.apache.geronimo.naming.java.RootContext;
import org.omg.CORBA.BAD_OPERATION;
import org.omg.CORBA.INVALID_TRANSACTION;
import org.omg.CORBA.MARSHAL;
import org.omg.CORBA.NO_PERMISSION;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.TRANSACTION_REQUIRED;
import org.omg.CORBA.TRANSACTION_ROLLEDBACK;
import org.omg.CORBA.UNKNOWN;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.InvokeHandler;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ResponseHandler;
import org.omg.CORBA.portable.UnknownException;
import org.omg.PortableServer.Servant;
import org.openejb.EJBContainer;
import org.openejb.EJBInterfaceType;
import org.openejb.EJBInvocation;
import org.openejb.EJBInvocationImpl;
import org.openejb.corba.compiler.IiopOperation;
import org.openejb.corba.compiler.PortableStubCompiler;
import org.openejb.corba.util.Util;

/**
 * @version $Revision$ $Date$
 */
public class StandardServant extends Servant implements InvokeHandler {
    private static final Log log = LogFactory.getLog(StandardServant.class);

    private static final Method GETEJBMETADATA = getMethod(EJBHome.class, "getEJBMetaData", null);
    private static final Method GETHOMEHANDLE = getMethod(EJBHome.class, "getHomeHandle", null);
    private static final Method REMOVE_W_KEY = getMethod(EJBHome.class, "remove", new Class[]{Object.class});
    private static final Method REMOVE_W_HAND = getMethod(EJBHome.class, "remove", new Class[]{Handle.class});
    private static final Method GETEJBHOME = getMethod(EJBObject.class, "getEJBHome", null);
    private static final Method GETHANDLE = getMethod(EJBObject.class, "getHandle", null);
    private static final Method GETPRIMARYKEY = getMethod(EJBObject.class, "getPrimaryKey", null);
    private static final Method ISIDENTICAL = getMethod(EJBObject.class, "isIdentical", new Class[]{EJBObject.class});
    private static final Method REMOVE = getMethod(EJBObject.class, "remove", null);


    private final EJBInterfaceType ejbInterfaceType;
    private final EJBContainer ejbContainer;
    private final Object primaryKey;
    private final String[] typeIds;
    private final Map operations;
    private final Context enc;

    public StandardServant(EJBInterfaceType ejbInterfaceType, EJBContainer ejbContainer) {
        this(ejbInterfaceType, ejbContainer, null);
    }

    public StandardServant(EJBInterfaceType ejbInterfaceType, EJBContainer ejbContainer, Object primaryKey) {
        this.ejbInterfaceType = ejbInterfaceType;
        this.ejbContainer = ejbContainer;
        this.primaryKey = primaryKey;

        // get the interface class
        Class type;
        if (EJBInterfaceType.HOME == ejbInterfaceType) {
            type = ejbContainer.getProxyInfo().getHomeInterface();
        } else if (EJBInterfaceType.REMOTE == ejbInterfaceType) {
            type = ejbContainer.getProxyInfo().getRemoteInterface();
        } else {
            throw new IllegalArgumentException("Only home and remote interfaces are supported in this servant: " + ejbInterfaceType);
        }

        // build the operations index
        IiopOperation[] iiopOperations = PortableStubCompiler.createIiopOperations(type);
        Map operations = new HashMap(iiopOperations.length);
        for (int i = 0; i < iiopOperations.length; i++) {
            IiopOperation iiopOperation = iiopOperations[i];
            operations.put(iiopOperation.getName(), iiopOperation.getMethod());
        }
        this.operations = Collections.unmodifiableMap(operations);

        // creat the corba ids array
        List ids = new LinkedList();
        for (Iterator iterator = PortableStubCompiler.getAllInterfaces(type).iterator(); iterator.hasNext();) {
            Class superInterface = (Class) iterator.next();
            if (Remote.class.isAssignableFrom(superInterface) && superInterface != Remote.class) {
                ids.add("RMI:" + superInterface.getName() + ":0000000000000000");
            }
        }
        typeIds = (String[]) ids.toArray(new String[ids.size()]);

        // create ReadOnlyContext
        Map componentContext = new HashMap(2);
        componentContext.put("ORB", Util.getORB());
        componentContext.put("HandleDelegate", new CORBAHandleDelegate());
        try {
            enc = new SimpleReadOnlyContext(componentContext);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    public EJBInterfaceType getEjbInterfaceType() {
        return ejbInterfaceType;
    }

    public EJBContainer getEjbContainer() {
        return ejbContainer;
    }

    public Object getPrimaryKey() {
        return primaryKey;
    }

    public String[] _all_interfaces(org.omg.PortableServer.POA poa, byte[] objectId) {
        return typeIds;
    }

    public OutputStream _invoke(String operationName, InputStream _in, ResponseHandler reply) throws SystemException {
        // get the method object
        Method method = (Method) operations.get(operationName);
        int index = ejbContainer.getMethodIndex(method);
        if (index < 0 &&
                method.getDeclaringClass() != javax.ejb.EJBObject.class &&
                method.getDeclaringClass() != javax.ejb.EJBHome.class) {
            throw new BAD_OPERATION(operationName);
        }

        org.omg.CORBA_2_3.portable.InputStream in = (org.omg.CORBA_2_3.portable.InputStream) _in;

        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        Context oldContext = RootContext.getComponentContext();
        try {
            Thread.currentThread().setContextClassLoader(ejbContainer.getClassLoader());
            RootContext.setComponentContext(enc);

            // read in all of the arguments
            Class[] parameterTypes = method.getParameterTypes();
            Object[] arguments = new Object[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i++) {
                Class parameterType = parameterTypes[i];
                arguments[i] = Util.readObject(parameterType, in);
            }

            // invoke the method
            Object result = null;
            try {

                if (log.isDebugEnabled()) log.debug("Calling " + method.getName());

                if (method.getDeclaringClass() == javax.ejb.EJBObject.class) {
                    if (method.equals(GETHANDLE)) {
                        result = ejbContainer.getEjbObject(primaryKey).getHandle();
                    } else if (method.equals(GETPRIMARYKEY)) {
                        result = ejbContainer.getEjbObject(primaryKey).getPrimaryKey();
                    } else if (method.equals(ISIDENTICAL)) {
                        org.omg.CORBA.Object thisObject = this._this_object();
                        org.omg.CORBA.Object otherObject = (org.omg.CORBA.Object)arguments[0];
                        result = new Boolean(thisObject._is_equivalent(otherObject));
                    } else if (method.equals(GETEJBHOME)) {
                        result = ejbContainer.getEjbHome();
                    } else if (method.equals(REMOVE)) {
                        try {
                            ejbContainer.getEjbObject(primaryKey).remove();
                            result = null;
                        } catch (RemoveException e) {
                            return Util.writeUserException(method, reply, e);
                        }
                    } else {
                        throw new UnsupportedOperationException("Unkown method: " + method);
                    }
                } else if (method.getDeclaringClass() == javax.ejb.EJBHome.class) {
                   if (method.equals(GETEJBMETADATA)) {
                        result = ejbContainer.getEjbHome().getEJBMetaData();
                    } else if (method.equals(GETHOMEHANDLE)) {
                        result = ejbContainer.getEjbHome().getHomeHandle();
                    } else if (method.equals(REMOVE_W_HAND)) {
                        CORBAHandle handle = (CORBAHandle) arguments[0];
                        try {
                            if (ejbContainer.getProxyInfo().isStatelessSessionBean()) {
                                if (handle == null) {
                                    throw new RemoveException("Handle is null");
                                }
                                Class remoteInterface = ejbContainer.getProxyInfo().getRemoteInterface();
                                if (!remoteInterface.isInstance(handle.getEJBObject())) {
                                    throw new RemoteException("Handle does not hold a " + remoteInterface.getName());
                                }
                            } else {
                                // create the invocation object
                                EJBInvocation invocation = new EJBInvocationImpl(ejbInterfaceType, handle.getPrimaryKey(), index, arguments);

                                // invoke the container
                                InvocationResult invocationResult = ejbContainer.invoke(invocation);

                                // process the result
                                if (invocationResult.isException()) {
                                    // all other exceptions are written to stream
                                    // if this is an unknown exception type it will
                                    // be thrown out of writeException
                                    return Util.writeUserException(method, reply, invocationResult.getException());
                                }
                                invocationResult.getResult();
//
//                                ejbContainer.getEjbHome().remove(handle.getPrimaryKey());
                            }
                        } catch (RemoveException e) {

                            return Util.writeUserException(method, reply, e);
                        }
                        result = null;
                    } else if (method.equals(REMOVE_W_KEY)) {
                        try {
                            ejbContainer.getEjbHome().remove(arguments[0]);
                            result = null;
                        } catch (RemoveException e) {
                            return Util.writeUserException(method, reply, e);
                        }
                    } else {
                        throw new UnsupportedOperationException("Unkown method: " + method);
                    }
                } else {
                    // create the invocation object
                    EJBInvocation invocation = new EJBInvocationImpl(ejbInterfaceType, primaryKey, index, arguments);

                    // invoke the container
                    InvocationResult invocationResult = ejbContainer.invoke(invocation);

                    // process the result
                    if (invocationResult.isException()) {
                        // all other exceptions are written to stream
                        // if this is an unknown exception type it will
                        // be thrown out of writeException
                        return Util.writeUserException(method, reply, invocationResult.getException());
                    }
                    result = invocationResult.getResult();
                }
            } catch (TransactionRolledbackException e) {
                log.debug("TransactionRolledbackException", e);
                throw new TRANSACTION_ROLLEDBACK(e.toString());
            } catch (TransactionRequiredException e) {
                log.debug("TransactionRequiredException", e);
                throw new TRANSACTION_REQUIRED(e.toString());
            } catch (InvalidTransactionException e) {
                log.debug("InvalidTransactionException", e);
                throw new INVALID_TRANSACTION(e.toString());
            } catch (NoSuchObjectException e) {
                log.debug("NoSuchObjectException", e);
                throw new OBJECT_NOT_EXIST(e.toString());
            } catch (AccessException e) {
                log.debug("AccessException", e);
                throw new NO_PERMISSION(e.toString());
            } catch (MarshalException e) {
                log.debug("MarshalException", e);
                throw new MARSHAL(e.toString());
            } catch (RemoteException e) {
                log.debug("RemoteException", e);
                throw new UnknownException(e);
            } catch (RuntimeException e) {
                log.debug("RuntimeException", e);
                RemoteException remoteException = new RemoteException(e.getClass().getName() + " thrown from " + ejbContainer.getContainerID() + ": " + e.getMessage());
                throw new UnknownException(remoteException);
            } catch (Error e) {
                log.debug("Error", e);
                RemoteException remoteException = new RemoteException(e.getClass().getName() + " thrown from " + ejbContainer.getContainerID() + ": " + e.getMessage());
                throw new UnknownException(remoteException);
            } catch (Throwable e) {
                log.warn("Unexpected throwable", e);
                throw new UNKNOWN("Unknown exception type " + e.getClass().getName() + ": " + e.getMessage());
            }

            // creat the output stream
            org.omg.CORBA_2_3.portable.OutputStream out = (org.omg.CORBA_2_3.portable.OutputStream) reply.createReply();

            // write the output value
            Util.writeObject(method.getReturnType(), result, out);

            return out;
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
            RootContext.setComponentContext(oldContext);
        }
    }

    private static Method getMethod(Class c, String method, Class[] params) {
        try {
            return c.getMethod(method, params);
        } catch (NoSuchMethodException e) {
            throw (IllegalStateException) new IllegalStateException().initCause(e);
        }
    }
}
