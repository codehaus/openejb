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
 * Copyright 2001 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.server.ejbd;

import java.io.IOException;
import java.io.ObjectInput;
import java.lang.reflect.Method;

import org.apache.geronimo.core.service.InvocationKey;
import org.apache.geronimo.transaction.context.TransactionContext;
import org.openejb.EJBContainer;
import org.openejb.EJBInstanceContext;
import org.openejb.EJBInterfaceType;
import org.openejb.EJBInvocation;
import org.openejb.EJBInvocationImpl;
import org.openejb.client.EJBRequest;
import org.openejb.proxy.EJBProxyFactory;

public class EJBInvocationStream extends EJBRequest implements EJBInvocation {

    private ObjectInput in;

    private final EJBInvocation invocationState = new EJBInvocationImpl();
    private EJBInterfaceType interfaceType;

    private int methodIndex = -1;

    public EJBInvocationStream() {
        super();
    }

    public EJBInvocationStream(int requestMethod) {
        super(requestMethod);
    }


    public Object[] getArguments() {
        return getMethodParameters();
    }

    public Object getId() {
        return getPrimaryKey();
    }

    public int getMethodIndex() {
        return methodIndex;
    }

    public EJBInterfaceType getType() {
        return interfaceType;
    }

    public Class getMethodClass() {
        checkState();
        return super.getMethodClass();
    }

    public Method getMethodInstance() {
        checkState();
        return super.getMethodInstance();
    }

    public String getMethodName() {
        checkState();
        return super.getMethodName();
    }

    public Object[] getMethodParameters() {
        checkState();
        return super.getMethodParameters();
    }

    public Class[] getMethodParamTypes() {
        checkState();
        return super.getMethodParamTypes();
    }

    public Object getPrimaryKey() {
        checkState();
        return super.getPrimaryKey();
    }

    public void readExternal(ObjectInput in)
    throws IOException, ClassNotFoundException {
        clearState();

        this.in = in;

        readRequestMethod(in);

        readContainerId(in);

        readClientIdentity(in);

        switch (super.getRequestMethod()){
            case EJB_HOME_CREATE:
            case EJB_HOME_FIND:
            case EJB_HOME_GET_EJB_META_DATA:
            case EJB_HOME_GET_HOME_HANDLE:
            case EJB_HOME_REMOVE_BY_HANDLE:
            case EJB_HOME_REMOVE_BY_PKEY:
                interfaceType = EJBInterfaceType.HOME; break;
            default:
                interfaceType = EJBInterfaceType.REMOTE;
        }
//        finishReadExternal();
   }

    private void checkState(){
        if (super.getMethodInstance() == null){
            try {
                finishReadExternal();
            } catch (IOException e) {
                IllegalStateException ise = new IllegalStateException("Invalid EJBRequest stream.");
                ise.initCause(e);
                throw ise;
            } catch (ClassNotFoundException e) {
//                IllegalAccessError iae = new IllegalAccessError("Class only accessible from classloader of an EJBContainer.");
                RuntimeException iae = new RuntimeException(e);
//                iae.initCause(e);
                throw iae;
            }
        }
    }

    private void finishReadExternal()
    throws IOException, ClassNotFoundException {
        readPrimaryKey(in);

        readMethod(in);

        readMethodParameters(in);

        loadMethodInstance();
    }

    public Object get(InvocationKey arg0) {
        return invocationState.get(arg0);
    }

    public void put(InvocationKey arg0, Object arg1) {
        invocationState.put(arg0, arg1);
    }

    public void setEJBInstanceContext(EJBInstanceContext instanceContext) {
        invocationState.setEJBInstanceContext(instanceContext);
    }

    public void setTransactionContext(TransactionContext transactionContext) {
        invocationState.setTransactionContext(transactionContext);
    }

    public EJBInstanceContext getEJBInstanceContext() {
        return invocationState.getEJBInstanceContext();
    }

    public TransactionContext getTransactionContext() {
        return invocationState.getTransactionContext();
    }

    public void setMethodIndex(int methodIndex) {
        this.methodIndex = methodIndex;
    }
}
