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
package org.openejb.sfsb;

import java.util.Set;
import javax.ejb.SessionBean;
import javax.ejb.SessionSynchronization;

import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.transaction.UserTransactionImpl;
import org.apache.geronimo.transaction.context.BeanTransactionContext;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openejb.AbstractInstanceContext;
import org.openejb.EJBOperation;
import org.openejb.EJBInvocation;
import org.openejb.dispatch.SystemMethodIndices;
import org.openejb.proxy.EJBProxyFactory;

/**
 *
 *
 * @version $Revision$ $Date$
 */
public class StatefulInstanceContext extends AbstractInstanceContext {
    private static final Log log = LogFactory.getLog(StatefulInstanceContext.class);
    private final Object containerId;
    private final Object id;
    private final StatefulSessionContext statefulContext;
    private final EJBInvocation afterBeginInvocation;
    private final EJBInvocation beforeCompletionInvocation;
    private boolean dead = false;
    private BeanTransactionContext preexistingContext;
    private EJBOperation operation;
    private final SystemMethodIndices systemMethodIndices;

    public StatefulInstanceContext(Object containerId, EJBProxyFactory proxyFactory, SessionBean instance, Object id, TransactionContextManager transactionContextManager, UserTransactionImpl userTransaction, SystemMethodIndices systemMethodIndices, Interceptor systemChain, Set unshareableResources, Set applicationManagedSecurityResources) {
        //currently stateful beans have no timer service.
        super(systemChain, unshareableResources, applicationManagedSecurityResources, instance, proxyFactory, null);
        this.containerId = containerId;
        this.id = id;
        statefulContext = new StatefulSessionContext(this, transactionContextManager, userTransaction);
        this.systemMethodIndices = systemMethodIndices;
        setContextInvocation = systemMethodIndices.getSetContextInvocation(this, statefulContext);
        unsetContextInvocation = systemMethodIndices.getSetContextInvocation(this, null);
        if (instance instanceof SessionSynchronization) {
            afterBeginInvocation = systemMethodIndices.getAfterBeginInvocation(this);
            beforeCompletionInvocation = systemMethodIndices.getBeforeCompletionInvocation(this);
        } else {
            afterBeginInvocation = null;
            beforeCompletionInvocation = null;
        }
    }

    public Object getContainerId() {
        return containerId;
    }

    public EJBOperation getOperation() {
        return operation;
    }
    public void setOperation(EJBOperation operation) {
        statefulContext.setState(operation);
        this.operation = operation;
    }

    public boolean setTimerState(EJBOperation operation) {
        return statefulContext.setTimerState(operation);
    }

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        // @todo remove setId from the EJBInstanceContext interface
        throw new UnsupportedOperationException();
    }

    public BeanTransactionContext getPreexistingContext() {
        return preexistingContext;
    }

    public void setPreexistingContext(BeanTransactionContext preexistingContext) {
        this.preexistingContext = preexistingContext;
    }

    public void die() {
        if (preexistingContext != null) {
            try {
                preexistingContext.rollback();
            } catch (Exception e) {
                log.warn("Unable to roll back", e);
            }
            preexistingContext = null;
        }
        dead = true;
    }

    public boolean isDead() {
        return dead;
    }

    public StatefulSessionContext getSessionContext() {
        return statefulContext;
    }

    public void associate() throws Throwable {
        if (getInstance() instanceof SessionSynchronization) {
            assert(getInstance() != null);
            systemChain.invoke(afterBeginInvocation);
        }
    }

    public void beforeCommit() throws Throwable {
        if (getInstance() instanceof SessionSynchronization) {
            assert(getInstance() != null);
            systemChain.invoke(beforeCompletionInvocation);
        }
    }

    public void afterCommit(boolean committed) throws Throwable {
        if (!dead) {
            // @todo fix me
//            container.getInstanceCache().putInactive(id, this);
        }
        if (getInstance() instanceof SessionSynchronization) {
            assert(getInstance() != null);
            systemChain.invoke(systemMethodIndices.getAfterCompletionInvocation(this, committed));
        }
    }
}