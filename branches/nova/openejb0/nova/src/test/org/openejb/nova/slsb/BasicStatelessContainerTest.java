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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.geronimo.common.StopWatch;
import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTrackingCoordinator;
import org.apache.geronimo.ejb.metadata.TransactionDemarcation;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.transaction.TransactionManagerProxy;

import junit.framework.TestCase;
import org.openejb.nova.EJBContainerConfiguration;
import org.openejb.nova.MockTransactionManager;
import org.openejb.nova.deployment.TransactionPolicySource;
import org.openejb.nova.dispatch.MethodSignature;
import org.openejb.nova.transaction.ContainerPolicy;
import org.openejb.nova.transaction.TxnPolicy;

/**
 * @version $Revision$ $Date$
 */
public class BasicStatelessContainerTest extends TestCase {
    private static final ObjectName CONTAINER_NAME = JMXUtil.getObjectName("geronimo.test:ejb=Mock");
    private static final ObjectName TM_NAME = JMXUtil.getObjectName("geronimo.test:role=TransactionManager");
    private static final ObjectName TCA_NAME = JMXUtil.getObjectName("geronimo.test:role=TrackedConnectionAssociator");
    private EJBContainerConfiguration config;
    private Kernel kernel;
    private GBeanMBean container;
    private ObjectName containerName;
    private Set containerPatterns;
    private MBeanServer mbServer;

    public void testRemoteInvocation() throws Throwable {
        MockHome home = (MockHome) mbServer.invoke(containerName, "getEJBHome", null, null);
        MockRemote remote = home.create();
        assertEquals(2, remote.intMethod(1));
    }

    public void testLocalInvocation() throws Throwable {
        MockLocalHome home = (MockLocalHome) mbServer.invoke(containerName, "getEJBLocalHome", null, null);
        MockLocal remote = home.create();
        assertEquals(2, remote.intMethod(1));
        assertEquals(2, remote.intMethod(1));
        remote.remove();
    }

    public void testRemoteSpeed() throws Throwable {
        MockHome home = (MockHome) mbServer.invoke(containerName, "getEJBHome", null, null);
        MockRemote remote = home.create();
        remote.intMethod(1);
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        for (int i = 0; i < 1000; i++) {
            remote.intMethod(1);
        }
        stopWatch.stop();
        System.out.println("Remote: " + stopWatch.getTime());
    }

    public void XtestLocalSpeed() throws Throwable {
        MockLocalHome home = (MockLocalHome) mbServer.invoke(containerName, "getEJBLocalHome", null, null);

        MockLocal local = home.create();
        Integer integer = new Integer(1);
        local.integerMethod(integer);
        int COUNT = 10000;
        for (int i = 0; i < COUNT; i++) {
            local.integerMethod(integer);
        }

        COUNT = 100000;
        long start = System.currentTimeMillis();
        for (int i = 0; i < COUNT; i++) {
            local.integerMethod(integer);
        }
        long end = System.currentTimeMillis();
        System.out.println("Per local call w/out security: " + ((end - start) * 1000000.0 / COUNT) + "ns");
    }

/*
    public void XtestLocalSpeed2() throws Throwable {
        int index = 0;
        EJBInvocationImpl invocation = new EJBInvocationImpl(EJBInvocationType.REMOTE, index, new Object[]{new Integer(1)});
        InvocationResult result = container.invoke(invocation);
        assertEquals(new Integer(2), result.getResult());

        for (int i = 0; i < 1000000; i++) {
            container.invoke(invocation);
        }

        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            container.invoke(invocation);
        }
        long end = System.currentTimeMillis();
        System.out.println("Local Direct: " + (end - start));
    }
*/

    protected void setUp() throws Exception {
        super.setUp();

        config = new EJBContainerConfiguration();
        config.uri = new URI("async", null, "localhost", 3434, "/JMX", null, CONTAINER_NAME.toString());
        config.beanClassName = MockEJB.class.getName();
        config.homeInterfaceName = MockHome.class.getName();
        config.localHomeInterfaceName = MockLocalHome.class.getName();
        config.remoteInterfaceName = MockRemote.class.getName();
        config.localInterfaceName = MockLocal.class.getName();
        config.txnDemarcation = TransactionDemarcation.CONTAINER;
        config.unshareableResources = new HashSet();
        config.transactionPolicySource = new TransactionPolicySource() {
            public TxnPolicy getTransactionPolicy(String methodIntf, MethodSignature signature) {
                return ContainerPolicy.Required;
            }
        };

        containerName = CONTAINER_NAME;
        containerPatterns = new HashSet();
        containerPatterns.add(containerName);

        kernel = new Kernel("statelessSessionTest");
        kernel.boot();
        mbServer = kernel.getMBeanServer();

        GBeanMBean transactionManager = new GBeanMBean(TransactionManagerProxy.GBEAN_INFO);
        transactionManager.setAttribute("Delegate", new MockTransactionManager());
        start(TM_NAME, transactionManager);

        GBeanMBean trackedConnectionAssociator = new GBeanMBean(ConnectionTrackingCoordinator.GBEAN_INFO);
        start(TCA_NAME, trackedConnectionAssociator);

        container = new GBeanMBean(StatelessContainer.GBEAN_INFO);
        container.setAttribute("EJBContainerConfiguration", config);
        container.setReferencePatterns("TransactionManager", Collections.singleton(TM_NAME));
        container.setReferencePatterns("TrackedConnectionAssociator", Collections.singleton(TCA_NAME));
        start(containerName, container);

    }

    private void start(ObjectName name, Object instance) throws Exception {
        mbServer.registerMBean(instance, name);
        mbServer.invoke(name, "start", null, null);
    }

    private void stop(ObjectName name) throws Exception {
        mbServer.invoke(name, "stop", null, null);
        mbServer.unregisterMBean(name);
    }


    protected void tearDown() throws Exception {
        stop(containerName);
        stop(TM_NAME);
        stop(TCA_NAME);
        kernel.shutdown();
    }
}
