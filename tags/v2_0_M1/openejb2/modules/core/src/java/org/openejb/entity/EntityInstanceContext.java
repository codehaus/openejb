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

import javax.ejb.EntityBean;
import javax.ejb.EntityContext;

import org.apache.geronimo.connector.outbound.connectiontracking.defaultimpl.DefaultComponentContext;
import org.apache.geronimo.transaction.TransactionContext;

import org.openejb.EJBInstanceContext;
import org.openejb.EJBOperation;
import org.openejb.proxy.EJBProxyFactory;

/**
 *
 *
 * @version $Revision$ $Date$
 */
public abstract class EntityInstanceContext extends DefaultComponentContext implements EJBInstanceContext {
    private final Object containerId;
    private final EntityContextImpl entityContext;
    private final EJBProxyFactory proxyFactory;
    private Object id;
    private boolean stateValid;

    public EntityInstanceContext(Object containerId, EJBProxyFactory proxyFactory) {
        this.containerId = containerId;
        this.proxyFactory = proxyFactory;
        entityContext = new EntityContextImpl(this);
    }

    public EJBProxyFactory getProxyFactory() {
        return proxyFactory;
    }

    public Object getContainerId() {
        return containerId;
    }

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public void setOperation(EJBOperation operation) {
        entityContext.setState(operation);
    }

    public EntityContext getEntityContext() {
        return entityContext;
    }

    public void setTransactionContext(TransactionContext transactionContext) {
    }

    public boolean isStateValid() {
        return stateValid;
    }

    public void setStateValid(boolean stateValid) {
        this.stateValid = stateValid;
    }

    public void associate() throws Exception {
        if (id != null && !stateValid) {
            try {
                setOperation(EJBOperation.EJBLOAD);
                ((EntityBean) getInstance()).ejbLoad();
            } finally {
                setOperation(EJBOperation.INACTIVE);
            }
            stateValid = true;
        }
    }

    public void beforeCommit() throws Exception {
    }

    public void flush() throws Exception {
        if (id != null) {
            assert (stateValid) : "Trying to invoke ejbStore for invalid instance";
            try {
                setOperation(EJBOperation.EJBLOAD);
                ((EntityBean) getInstance()).ejbStore();
            } finally {
                setOperation(EJBOperation.INACTIVE);
            }
        }
    }

    public void afterCommit(boolean status) {
    }
}
