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
package org.openejb.mdb;

import java.util.Set;

import javax.ejb.MessageDrivenBean;

import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.apache.geronimo.transaction.context.UserTransactionImpl;
import org.openejb.AbstractInstanceContext;
import org.openejb.EJBOperation;
import org.openejb.EJBInvocation;
import org.openejb.cache.InstancePool;
import org.openejb.dispatch.SystemMethodIndices;
import org.openejb.timer.BasicTimerService;

/**
 * Wrapper for a MDB.
 *
 * @version $Revision$ $Date$
 */
public final class MDBInstanceContext extends AbstractInstanceContext {
    private final MDBContext mdbContext;
    private final EJBInvocation setContextInvocation;
    private final EJBInvocation unsetContextInvocation;
    private final EJBInvocation ejbCreateInvocation;
    private final EJBInvocation ejbRemoveInvocation;
    private InstancePool pool;

    public MDBInstanceContext(Object containerId, MessageDrivenBean instance, TransactionContextManager transactionContextManager, UserTransactionImpl userTransaction, SystemMethodIndices systemMethodIndices, Interceptor systemChain, Set unshareableResources, Set applicationManagedSecurityResources, BasicTimerService timerService) {
        super(containerId, instance, systemChain, null, timerService, unshareableResources, applicationManagedSecurityResources);
        this.mdbContext = new MDBContext(this, transactionContextManager, userTransaction);
        ejbCreateInvocation = systemMethodIndices.getEJBCreateInvocation(this);
        ejbRemoveInvocation = systemMethodIndices.getEJBRemoveInvocation(this);
        setContextInvocation = systemMethodIndices.getSetContextInvocation(this, mdbContext);
        unsetContextInvocation = systemMethodIndices.getSetContextInvocation(this, null);
    }

    public Object getId() {
        return null;
    }

    public void flush() {
        throw new AssertionError("Cannot flush a MDB Context");
    }

    public InstancePool getPool() {
        return pool;
    }

    public void setPool(InstancePool pool) {
        this.pool = pool;
    }

    public void die() {
        if (pool != null) {
            pool.remove(this);
            pool = null;
        }
        super.die();
    }

    public void exit() {
        if (pool != null) {
            pool.release(this);
        }
        super.exit();
    }

    public MDBContext getMessageDrivenContext() {
        return mdbContext;
    }

    public void setOperation(EJBOperation operation) {
        mdbContext.setState(operation);
    }

    public boolean setTimerState(EJBOperation operation) {
        return mdbContext.setTimerState(operation);
    }

    public void setContext() throws Throwable {
        if (isDead()) {
            throw new IllegalStateException("Context is dead: container=" + getContainerId() + ", id=" + getId());
        }
        systemChain.invoke(setContextInvocation);
    }

    public void unsetContext() throws Throwable {
        if (isDead()) {
            throw new IllegalStateException("Context is dead: container=" + getContainerId() + ", id=" + getId());
        }
        systemChain.invoke(unsetContextInvocation);
    }

    public void ejbCreate() throws Throwable {
        if (isDead()) {
            throw new IllegalStateException("Context is dead: container=" + getContainerId() + ", id=" + getId());
        }
        assert(getInstance() != null);
        systemChain.invoke(ejbCreateInvocation);
    }

    public void ejbRemove() throws Throwable {
        if (isDead()) {
            throw new IllegalStateException("Context is dead: container=" + getContainerId() + ", id=" + getId());
        }
        assert(getInstance() != null);
        systemChain.invoke(ejbRemoveInvocation);
    }
}
