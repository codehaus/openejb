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
package org.openejb.nova.slsb;

import java.net.URI;
import javax.transaction.TransactionManager;

import org.apache.geronimo.cache.InstancePool;
import org.apache.geronimo.connector.outbound.connectiontracking.TrackedConnectionAssociator;
import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.naming.java.ComponentContextInterceptor;

import org.openejb.nova.AbstractEJBContainer;
import org.openejb.nova.ConnectionTrackingInterceptor;
import org.openejb.nova.EJBContainerConfiguration;
import org.openejb.nova.SystemExceptionInterceptor;
import org.openejb.nova.dispatch.DispatchInterceptor;
import org.openejb.nova.dispatch.MethodSignature;
import org.openejb.nova.dispatch.VirtualOperation;
import org.openejb.nova.security.EJBIdentityInterceptor;
import org.openejb.nova.security.EJBRunAsInterceptor;
import org.openejb.nova.security.EJBSecurityInterceptor;
import org.openejb.nova.security.PermissionManager;
import org.openejb.nova.security.PolicyContextHandlerEJBInterceptor;
import org.openejb.nova.transaction.TransactionContextInterceptor;
import org.openejb.nova.transaction.TransactionPolicyManager;
import org.openejb.nova.util.SoftLimitedInstancePool;

/**
 * @version $Revision$ $Date$
 */
public class StatelessContainer extends AbstractEJBContainer {
    private final VirtualOperation[] vtable;
    private final Interceptor interceptor;
    private final MethodSignature[] signatures;
    private final InstancePool pool;
    private final TransactionPolicyManager transactionPolicyManager;

    public StatelessContainer(EJBContainerConfiguration config, TransactionManager transactionManager, TrackedConnectionAssociator trackedConnectionAssociator) throws Exception {
        super(config, transactionManager, trackedConnectionAssociator);

        StatelessOperationFactory vopFactory = StatelessOperationFactory.newInstance(this);
        vtable = vopFactory.getVTable();
        signatures = vopFactory.getSignatures();
        transactionPolicyManager = new TransactionPolicyManager(config.transactionPolicySource, vopFactory.getSignatures());

        pool = new SoftLimitedInstancePool(new StatelessInstanceFactory(this), 1);

        // set up server side interceptors
        Interceptor firstInterceptor;
        firstInterceptor = new DispatchInterceptor(vtable);
        if (trackedConnectionAssociator != null) {
            firstInterceptor = new ConnectionTrackingInterceptor(firstInterceptor, trackedConnectionAssociator, config.unshareableResources);
        }
        firstInterceptor = new TransactionContextInterceptor(firstInterceptor, transactionManager, transactionPolicyManager);
        if (setIdentity) {
            firstInterceptor = new EJBIdentityInterceptor(firstInterceptor);
        }
        if (setSecurityInterceptor) {
            firstInterceptor = new EJBSecurityInterceptor(firstInterceptor, contextId, new PermissionManager(ejbName, vopFactory.getSignatures()));
        }
        if (runAs != null) {
            firstInterceptor = new EJBRunAsInterceptor(firstInterceptor, runAs);
        }
        if (setPolicyContextHandlerDataEJB) {
            firstInterceptor = new PolicyContextHandlerEJBInterceptor(firstInterceptor);
        }
        firstInterceptor = new StatelessInstanceInterceptor(firstInterceptor, pool);
        firstInterceptor = new ComponentContextInterceptor(firstInterceptor, componentContext);
        firstInterceptor = new SystemExceptionInterceptor(firstInterceptor, getEJBName());

        this.interceptor = firstInterceptor;
    }

    public void doStart() throws WaitingException, Exception {
        super.doStart();

        URI target;
        if (homeInterface != null) {
            // set up server side remoting endpoint
            target = startServerRemoting(interceptor);
        } else {
            target = null;
        }

        // set up client containers
        StatelessClientContainerFactory clientFactory = new StatelessClientContainerFactory(signatures, target, homeInterface, remoteInterface, interceptor, localHomeInterface, localInterface);
        remoteClientContainer = clientFactory.getRemoteClient();
        localClientContainer = clientFactory.getLocalClient();

    }

    public void doStop() throws WaitingException, Exception {
        stopServerRemoting();
        remoteClientContainer = null;
        localClientContainer = null;
        super.doStop();
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(StatelessContainer.class.getName(), AbstractEJBContainer.GBEAN_INFO);
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
