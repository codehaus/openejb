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
package org.openejb.deployment.entity;

import java.util.HashSet;
import javax.ejb.EJBObject;

import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.repository.Dependency;
import org.apache.geronimo.kernel.repository.ImportType;
import org.openejb.deployment.BmpBuilder;
import org.openejb.deployment.DeploymentHelper;

/**
 * @version $Revision$ $Date$
 */
public class BasicBMPEntityContainerTest extends DeploymentHelper {
    public void testSimpleConfig() throws Throwable {
        MockHome home = (MockHome) kernel.getAttribute(CONTAINER_NAME, "ejbHome");
        assertEquals(5 + 1, home.intMethod(5));

        EJBObject ejbObject1 = home.findByPrimaryKey(new Integer(1));
        assertEquals(new Integer(1), ejbObject1.getPrimaryKey());
        assertTrue(ejbObject1.isIdentical(ejbObject1));

        EJBObject ejbObject2 = home.findByPrimaryKey(new Integer(2));

        assertEquals(new Integer(2), ejbObject2.getPrimaryKey());
        assertTrue(ejbObject2.isIdentical(ejbObject2));

        assertFalse(ejbObject1.isIdentical(ejbObject2));
        assertFalse(ejbObject2.isIdentical(ejbObject1));
    }

    public void testRemoteInvoke() throws Exception {
        MockHome home = (MockHome) kernel.getAttribute(CONTAINER_NAME, "ejbHome");
        assertEquals(2, home.intMethod(1));

        MockRemote remote = home.findByPrimaryKey(new Integer(1));
        assertEquals(3, remote.intMethod(1));
    }

    public void testLocalInvoke() throws Exception {
        MockLocalHome home = (MockLocalHome) kernel.getAttribute(CONTAINER_NAME, "ejbLocalHome");

        assertEquals(2, home.intMethod(1));

        MockLocal local = home.findByPrimaryKey(new Integer(1));
        assertEquals(3, local.intMethod(1));
        assertEquals(1, local.getIntField());
    }

    public void testLocalCreate() throws Exception {
        MockLocalHome home = (MockLocalHome) kernel.getAttribute(CONTAINER_NAME, "ejbLocalHome");
        MockLocal local = home.create(new Integer(1), null);
        assertEquals(new Integer(1), local.getPrimaryKey());
    }

    public void testTimeout() throws Exception {
        MockLocalHome localHome = (MockLocalHome) kernel.getAttribute(CONTAINER_NAME, "ejbLocalHome");
        MockLocal local = localHome.create(new Integer(1), null);
        local.startTimer();
        Thread.sleep(400L);
        int timeoutCount = local.getTimeoutCount();
        assertEquals(1, timeoutCount);
    }

    public void testLocalRemove() throws Exception {
        MockLocalHome home = (MockLocalHome) kernel.getAttribute(CONTAINER_NAME, "ejbLocalHome");
        home.remove(new Integer(1));

        MockLocal local = home.create(new Integer(1), null);
        local.remove();
    }

    protected void setUp() throws Exception {
        super.setUp();

        BmpBuilder builder = new BmpBuilder();
        builder.setContainerId(CONTAINER_NAME.toString());
        builder.setEjbName("MockEJB");
        builder.setBeanClassName(MockBMPEJB.class.getName());
        builder.setHomeInterfaceName(MockHome.class.getName());
        builder.setLocalHomeInterfaceName(MockLocalHome.class.getName());
        builder.setRemoteInterfaceName(MockRemote.class.getName());
        builder.setLocalInterfaceName(MockLocal.class.getName());
        builder.setPrimaryKeyClassName(Integer.class.getName());

        builder.setEjbContainerName(bmpEjbContainerName);

        builder.setJndiNames(new String[0]);
        builder.setLocalJndiNames(new String[0]);
        builder.setUnshareableResources(new HashSet());

        GBeanData deployment = builder.createConfiguration();

        //start the ejb container
        ConfigurationData configurationData = new ConfigurationData(TEST_CONFIGURATION_ID, kernel.getNaming());
        configurationData.getEnvironment().addDependency(new Dependency(BOOTSTRAP_ID, ImportType.ALL));
        configurationData.addGBean(deployment);
        ConfigurationManager configurationManager = ConfigurationUtil.getConfigurationManager(kernel);
        configurationManager.loadConfiguration(configurationData);
        configurationManager.startConfiguration(TEST_CONFIGURATION_ID);
    }
}
