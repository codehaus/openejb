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
package org.openejb.slsb;

import java.rmi.RemoteException;
import java.util.HashSet;
import javax.ejb.EJBException;
import javax.ejb.EJBHome;
import javax.ejb.EJBMetaData;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.RemoveException;
import javax.rmi.PortableRemoteObject;

import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTrackingCoordinator;
import org.apache.geronimo.naming.java.ReadOnlyContext;
import org.apache.geronimo.transaction.context.TransactionContextManager;

import junit.framework.TestCase;
import junit.framework.AssertionFailedError;
import org.openejb.EJBContainer;
import org.openejb.MockTransactionManager;
import org.openejb.deployment.TransactionPolicySource;
import org.openejb.dispatch.InterfaceMethodSignature;
import org.openejb.transaction.ContainerPolicy;
import org.openejb.transaction.TransactionPolicy;

/**
 *
 *
 *
 * @version $Revision$ $Date$
 */
public class StatelessClientContainerTest extends TestCase {
    private EJBContainer container;

    public void testMetadata() throws Exception {
        EJBMetaData metaData = container.getEJBHome().getEJBMetaData();
        assertTrue(metaData.isSession());
        assertTrue(metaData.isStatelessSession());
        assertEquals(MockHome.class, metaData.getHomeInterfaceClass());
        assertEquals(MockRemote.class, metaData.getRemoteInterfaceClass());
        EJBHome home = metaData.getEJBHome();
        assertTrue(home instanceof MockHome);
        try {
            PortableRemoteObject.narrow(home, MockHome.class);
        } catch (ClassCastException e) {
            fail("Unable to narrow home interface");
        }
        try {
            metaData.getPrimaryKeyClass();
            fail("Expected EJBException, but no exception was thrown");
        } catch (EJBException e) {
            // OK
        } catch (AssertionFailedError e) {
            throw e;
        } catch (Throwable t) {
            fail("Expected EJBException, but got " + t.getClass().getName());
        }
    }

    public void testHomeInterface() throws Exception {
        MockHome home = (MockHome) container.getEJBHome();
        assertTrue(home.create() instanceof MockRemote);
        try {
            home.remove(new Integer(1));
            fail("Expected RemoveException, but no exception was thrown");
        } catch (RemoveException e) {
            // OK
        } catch (AssertionFailedError e) {
            throw e;
        } catch (Throwable t) {
            fail("Expected RemoveException, but got " + t.getClass().getName());
        }
        try {
            home.remove(new Handle() {
                public EJBObject getEJBObject() throws RemoteException {
                    return null;
                }
            });
            fail("Expected RemoteException, but no exception was thrown");
        } catch (RemoteException e) {
            // OK
        } catch (AssertionFailedError e) {
            throw e;
        } catch (Throwable t) {
            fail("Expected RemoteException, but got " + t.getClass().getName());
        }
    }

    public void testLocalHomeInterface() {
        MockLocalHome localHome = (MockLocalHome) container.getEJBLocalHome();
        try {
            localHome.remove(new Integer(1));
            fail("Expected RemoveException, but no exception was thrown");
        } catch (RemoveException e) {
            // OK
        } catch (AssertionFailedError e) {
            throw e;
        } catch (Throwable t) {
            fail("Expected RemoveExceptions, but got " + t.getClass().getName());
        }
    }

    public void testObjectInterface() throws Exception {
        MockHome home = (MockHome) container.getEJBHome();
        MockRemote remote = home.create();
        assertTrue(remote.isIdentical(remote));
        assertTrue(remote.isIdentical(home.create()));
        try {
            remote.getPrimaryKey();
            fail("Expected RemoteException, but no exception was thrown");
        } catch (RemoteException e) {
            // OK
        } catch (AssertionFailedError e) {
            throw e;
        } catch (Throwable t) {
            fail("Expected RemoteException, but got " + t.getClass().getName());
        }
        remote.remove();
    }

    public void testLocalInterface() throws Exception {
        MockLocalHome localHome = (MockLocalHome) container.getEJBLocalHome();
        MockLocal local = localHome.create();
        assertTrue(local.isIdentical(local));
        assertTrue(local.isIdentical(localHome.create()));
        try {
            local.getPrimaryKey();
            fail("Expected EJBException, but no exception was thrown");
        } catch (EJBException e) {
             //OK
        } catch (AssertionFailedError e) {
            throw e;
        } catch (Throwable t) {
            fail("Expected EJBException, but got " + t.getClass().getName());
        }
        local.remove();
    }

    public void testInvocation() throws Exception {
        MockHome home = (MockHome) container.getEJBHome();
        MockRemote remote = home.create();
        assertEquals(2, remote.intMethod(1));
        try {
            remote.appException();
            fail("Expected AppException, but no exception was thrown");
        } catch (AppException e) {
            // OK
        }
        try {
            remote.sysException();
            fail("Expected RemoteException, but no exception was thrown");
        } catch (RemoteException e) {
            // OK
        }
    }

    protected void setUp() throws Exception {
        super.setUp();
        StatelessContainerBuilder builder = new StatelessContainerBuilder();
        builder.setClassLoader(this.getClass().getClassLoader());
        builder.setContainerId("MockEJB");
        builder.setEJBName("MockEJB");
        builder.setBeanClassName(MockEJB.class.getName());
        builder.setHomeInterfaceName(MockHome.class.getName());
        builder.setLocalHomeInterfaceName(MockLocalHome.class.getName());
        builder.setRemoteInterfaceName(MockRemote.class.getName());
        builder.setLocalInterfaceName(MockLocal.class.getName());
        builder.setJndiNames(new String[0]);
        builder.setLocalJndiNames(new String[0]);
        builder.setUnshareableResources(new HashSet());
        builder.setTransactionPolicySource(new TransactionPolicySource() {
            public TransactionPolicy getTransactionPolicy(String methodIntf, InterfaceMethodSignature signature) {
                return ContainerPolicy.Required;
            }
        });
        builder.setComponentContext(new ReadOnlyContext());
        builder.setTransactionContextManager(new TransactionContextManager(new MockTransactionManager()));
        builder.setTrackedConnectionAssociator(new ConnectionTrackingCoordinator());
        container = builder.createContainer();
    }
}
