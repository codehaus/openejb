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

import java.util.Map;
import java.util.HashMap;

import org.apache.geronimo.interceptor.InvocationResult;
import org.apache.geronimo.interceptor.SimpleInvocationResult;
import org.apache.geronimo.interceptor.InvocationKey;

import org.openejb.transaction.EjbTransactionContext;

/**
 *
 *
 * @version $Revision$ $Date$
 */
public class EjbInvocationImpl implements EjbInvocation {

    private final Map data = new HashMap();
    private final EJBInterfaceType type;
    private final int index;
    private final Object[] arguments;
    private final Object id;

    // The deployment that we are invoking, this is set in the deployment before sending the invocation to the interceptor stack
    private ExtendedEjbDeployment ejbDeployment;

    // Valid in server-side interceptor stack once an instance has been identified
    private EJBInstanceContext instanceContext;

    // Valid in server-side interceptor stack once a TransactionContext has been created
    private EjbTransactionContext ejbTransactionContext;

    public EjbInvocationImpl(EJBInterfaceType type, int index, Object[] arguments) {
        assert type != null : "Interface type may not be null";
        assert index >= 0 : "Invalid method index: "+index;
        this.type = type;
        this.index = index;
        this.arguments = arguments;
        id = null;
    }

    public EjbInvocationImpl(EJBInterfaceType type, Object id, int index, Object[] arguments) {
        assert type != null : "Interface type may not be null";
        assert index >= 0 : "Invalid method index: "+index;
        this.type = type;
        this.index = index;
        this.arguments = arguments;
        this.id = id;
    }

    public EjbInvocationImpl(int index, Object[] arguments, EJBInstanceContext instanceContext) {
        assert index >= 0 : "Invalid method index: "+index;
        assert instanceContext != null;
        this.type = EJBInterfaceType.LIFECYCLE;
        this.index = index;
        this.arguments = arguments;
        this.id = null;
        this.instanceContext = instanceContext;
    }

    public Object get(InvocationKey key) {
        if(data==null) {
            return null;
        }
        return data.get(key);
    }

    public void put(InvocationKey key, Object value) {
        data.put(key, value);
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

    public ExtendedEjbDeployment getEjbDeployment() {
        return ejbDeployment;
    }

    public void setEjbDeployment(ExtendedEjbDeployment ejbDeployment) {
        this.ejbDeployment = ejbDeployment;
    }

    public EJBInstanceContext getEJBInstanceContext() {
        return instanceContext;
    }

    public void setEJBInstanceContext(EJBInstanceContext instanceContext) {
        this.instanceContext = instanceContext;
    }

    public EjbTransactionContext getEjbTransactionData() {
        return ejbTransactionContext;
    }

    public void setEjbTransactionData(EjbTransactionContext ejbTransactionContext) {
        this.ejbTransactionContext = ejbTransactionContext;
    }

    public InvocationResult createResult(Object object) {
        return new SimpleInvocationResult(true, object);
    }

    public InvocationResult createExceptionResult(Exception exception) {
        return new SimpleInvocationResult(false, exception);
    }
}
