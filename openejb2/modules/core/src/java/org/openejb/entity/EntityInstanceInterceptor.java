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
package org.openejb.entity;

import java.rmi.NoSuchObjectException;
import javax.ejb.NoSuchEntityException;
import javax.ejb.NoSuchObjectLocalException;

import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.core.service.Invocation;
import org.apache.geronimo.core.service.InvocationResult;
import org.apache.geronimo.transaction.InstanceContext;
import org.apache.geronimo.transaction.context.TransactionContext;
import org.openejb.EJBInvocation;
import org.openejb.NotReentrantException;
import org.openejb.NotReentrantLocalException;
import org.openejb.cache.InstancePool;

/**
 * Simple Instance Interceptor that does not cache instances in the ready state
 * but passivates between each invocation.
 *
 * @version $Revision$ $Date$
 */
public final class EntityInstanceInterceptor implements Interceptor {
    private final Interceptor next;
    private final Object containerId;
    private final InstancePool pool;
    private final boolean reentrant;

    public EntityInstanceInterceptor(Interceptor next, Object containerId, InstancePool pool, boolean reentrant) {
        this.next = next;
        this.containerId = containerId;
        this.pool = pool;
        this.reentrant = reentrant;
    }

    public InvocationResult invoke(final Invocation invocation) throws Throwable {
        EJBInvocation ejbInvocation = (EJBInvocation) invocation;

        // initialize the context and set it into the invocation
        EntityInstanceContext ctx = getInstanceContext(ejbInvocation);
        ejbInvocation.setEJBInstanceContext(ctx);

        // check reentrancy
        if (!reentrant && ctx.isInCall()) {
            if (ejbInvocation.getType().isLocal()) {
                throw new NotReentrantLocalException("" + containerId);
            } else {
                throw new NotReentrantException("" + containerId);
            }
        }

        TransactionContext transactionContext = ejbInvocation.getTransactionContext();
        InstanceContext oldContext = null;
        try {
            oldContext = transactionContext.beginInvocation(ctx);
        } catch (NoSuchEntityException e) {
            if (ejbInvocation.getType().isLocal()) {
                throw new NoSuchObjectLocalException().initCause(e);
            } else {
                throw new NoSuchObjectException(e.getMessage());
            }
        }
        try {
            InvocationResult result = next.invoke(invocation);
            return result;
        } catch(Throwable t) {
            // we must kill the instance when a system exception is thrown
            ctx.die();
            transactionContext.unassociate(ctx);
            throw t;
        } finally {
            ejbInvocation.getTransactionContext().endInvocation(oldContext);
            ejbInvocation.setEJBInstanceContext(null);
        }
    }

    private EntityInstanceContext getInstanceContext(EJBInvocation ejbInvocation) throws Throwable {
        TransactionContext transactionContext = ejbInvocation.getTransactionContext();
        Object id = ejbInvocation.getId();
        EntityInstanceContext ctx = null;

        // if we have an id then check if there is already a context associated with the transaction
        if ( id != null) {
            ctx = (EntityInstanceContext) transactionContext.getContext(containerId, id);
            // if we have a dead context, the cached context was discarded, so we need clean it up and get a new one
            if (ctx != null && ctx.isDead()) {
                transactionContext.unassociate(ctx);
                ctx = null;
            }
        }

        // if we didn't find an existing context, create a new one.
        if (ctx == null) {
            ctx = (EntityInstanceContext) pool.acquire();
            ctx.setId(id);
            ctx.setPool(pool);
            ctx.setTransactionContext(transactionContext);
        }
        return ctx;
    }
}
