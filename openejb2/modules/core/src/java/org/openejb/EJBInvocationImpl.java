/* ====================================================================
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce this list of
 *    conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact openejb-group@openejb.sf.net.
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
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the OpenEJB Project.  For more information
 * please see <http://openejb.org/>.
 *
 * ====================================================================
 */
package org.openejb;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.apache.geronimo.core.service.SimpleInvocation;

import org.apache.geronimo.transaction.TransactionContext;

/**
 *
 *
 * @version $Revision$ $Date$
 */
public class EJBInvocationImpl extends SimpleInvocation implements EJBInvocation {
    // Fields are immutable, but not final due to readExternal
    private EJBInterfaceType type;
    private int index;
    private Object arguments[];
    private Object id;

    // Valid in server-side interceptor stack once an instance has been identified
    private transient EJBInstanceContext instanceContext;

    // Valid in server-side interceptor stack once a TransactionContext has been created
    private transient TransactionContext transactionContext;

    /**
     * No-arg constructor needed for Externalizable
     */
    public EJBInvocationImpl() {
    }

    public EJBInvocationImpl(EJBInterfaceType type, int index, Object[] arguments) {
        this.type = type;
        this.index = index;
        this.arguments = arguments;
        id = null;
    }

    public EJBInvocationImpl(EJBInterfaceType type, Object id, int index, Object[] arguments) {
        this.type = type;
        this.index = index;
        this.arguments = arguments;
        this.id = id;
    }

    public int getMethodIndex() {
        return index;
    }

    public EJBInterfaceType getType() {
        return type;
    }

    public Object[] getArguments() {
        return arguments;
    }

    public Object getId() {
        return id;
    }

    public EJBInstanceContext getEJBInstanceContext() {
        return instanceContext;
    }

    public void setEJBInstanceContext(EJBInstanceContext instanceContext) {
        this.instanceContext = instanceContext;
    }

    public TransactionContext getTransactionContext() {
        return transactionContext;
    }

    public void setTransactionContext(TransactionContext transactionContext) {
        this.transactionContext = transactionContext;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(type);
        out.writeInt(index);
        out.writeObject(arguments);
        out.writeObject(id);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        type = (EJBInterfaceType) in.readObject();
        index = in.readInt();
        arguments = (Object[]) in.readObject();
        id = in.readObject();
    }
}
