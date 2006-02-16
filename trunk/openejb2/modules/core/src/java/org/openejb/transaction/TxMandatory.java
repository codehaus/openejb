/**
 *
 * Copyright 2005 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.openejb.transaction;

import org.apache.geronimo.interceptor.InvocationResult;
import org.apache.geronimo.interceptor.Interceptor;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.apache.geronimo.transaction.context.TransactionContext;
import org.openejb.EjbInvocation;

import javax.ejb.TransactionRequiredLocalException;
import javax.ejb.TransactionRolledbackLocalException;
import javax.transaction.TransactionRequiredException;
import javax.transaction.TransactionRolledbackException;

/**
 * Mandatory
 *
 * The Container must invoke an enterprise Bean method whose transaction
 * attribute is set to Mandatory in a client's transaction context. The client
 * is required to call with a transaction context.
 *
 * - If the client calls with a transaction context, the container invokes the
 *   enterprise Bean's method in the client's transaction context.
 *
 * - If the client calls without a transaction context, the Container throws
 *   the javax.transaction.TransactionRequiredException exception if the
 *   client is a remote client, or the
 *   javax.ejb.TransactionRequiredLocalException if the client is a local
 *   client.
 *
 */
final class TxMandatory implements TransactionPolicy {
    public InvocationResult invoke(Interceptor interceptor, EjbInvocation ejbInvocation, TransactionContextManager transactionContextManager) throws Throwable {
        TransactionContext callerContext = transactionContextManager.getContext();

        // If we don't have a transaction, throw an exception
        if (callerContext == null || !callerContext.isInheritable()) {
            if (ejbInvocation.getType().isLocal()) {
                throw new TransactionRequiredLocalException();
            } else {
                throw new TransactionRequiredException();
            }
        }

        try {
            ejbInvocation.setTransactionContext(callerContext);
            return interceptor.invoke(ejbInvocation);
        } catch (Throwable t) {
            callerContext.setRollbackOnly();
            if (ejbInvocation.getType().isLocal()) {
                throw new TransactionRolledbackLocalException().initCause(t);
            } else {
                // can't set an initCause on a TransactionRolledbackException
                throw new TransactionRolledbackException(t.getMessage());
            }
        } finally {
            ejbInvocation.setTransactionContext(null);
        }
    }
    public String toString() {
        return "Mandatory";
    }

    private Object readResolve() {
        return ContainerPolicy.Mandatory;
    }
}
