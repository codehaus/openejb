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

import java.io.Serializable;
import java.util.Set;
import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;

import org.apache.geronimo.transaction.InstanceContext;
import org.apache.geronimo.transaction.UserTransactionImpl;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.apache.geronimo.core.service.Interceptor;
import org.openejb.EJBInstanceFactory;
import org.openejb.EJBInstanceFactoryImpl;
import org.openejb.timer.BasicTimerService;
import org.openejb.dispatch.SystemMethodIndices;
import org.openejb.dispatch.InterfaceMethodSignature;

/**
 * @version $Revision$ $Date$
 */
public class MDBInstanceContextFactory implements Serializable {
    private final Object containerId;
    private final EJBInstanceFactory factory;
    private final UserTransactionImpl userTransaction;
    private final Set unshareableResources;
    private final Set applicationManagedSecurityResources;
    private transient SystemMethodIndices systemMethodIndices;
    private transient Interceptor systemChain;
    private transient TransactionContextManager transactionContextManager;
    private transient BasicTimerService timerService;

    public MDBInstanceContextFactory(Object containerId, Class beanClass, UserTransactionImpl userTransaction, Set unshareableResources, Set applicationManagedSecurityResources) {
        this.containerId = containerId;
        this.userTransaction = userTransaction;
        this.factory = new EJBInstanceFactoryImpl(beanClass);
        this.unshareableResources = unshareableResources;
        this.applicationManagedSecurityResources = applicationManagedSecurityResources;
    }

    public void setSystemChain(Interceptor systemChain) {
        this.systemChain = systemChain;
    }

    public SystemMethodIndices setSignatures(InterfaceMethodSignature[] signatures) {
        systemMethodIndices = SystemMethodIndices.createSystemMethodIndices(signatures, "setMessageDrivenContext", MessageDrivenContext.class.getName(), null);
        return systemMethodIndices;
    }

    public void setTransactionContextManager(TransactionContextManager transactionContextManager) {
        this.transactionContextManager = transactionContextManager;
    }

    public void setTimerService(BasicTimerService timerService) {
        this.timerService = timerService;
    }


    public InstanceContext newInstance() throws Exception {
        return new MDBInstanceContext(containerId,
                (MessageDrivenBean) factory.newInstance(),
                transactionContextManager, userTransaction,
                systemMethodIndices, systemChain, unshareableResources,
                applicationManagedSecurityResources, timerService);
    }
}
