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
package org.openejb.deployment;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarFile;
import javax.management.ObjectName;
import javax.sql.DataSource;

import junit.extensions.TestDecorator;
import junit.framework.Protectable;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.j2ee.deployment.EARConfigBuilder;
import org.apache.geronimo.j2ee.management.impl.J2EEServerImpl;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.system.configuration.ExecutableConfigurationUtil;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.openejb.ContainerIndex;
import org.openejb.corba.compiler.OpenORBSkeletonGenerator;
import org.tranql.sql.jdbc.JDBCUtil;

/**
 * @version $Revision$ $Date$
 */
public class DeploymentTestSuite extends TestDecorator implements DeploymentTestContants {
    private final File moduleFile;

    private File tempDir;
    private Kernel kernel;
    private DataSource dataSource;
    private ClassLoader applicationClassLoader;

    protected DeploymentTestSuite(Class testClass, File moduleFile) {
        super(new TestSuite(testClass));
        this.moduleFile = moduleFile;
    }

    public Kernel getKernel() {
        return kernel;
    }

    public ClassLoader getApplicationClassLoader() {
        return applicationClassLoader;
    }

    public void run(final TestResult result) {
        Protectable p = new Protectable() {
            public void protect() throws Exception {
                try {
                    setUp();
                    basicRun(result);
                } finally {
                    tearDown();
                }
            }
        };
        result.runProtected(this, p);
    }

    private void setUp() throws Exception {
        ClassLoader testClassLoader = getClass().getClassLoader();
        String str = System.getProperty(javax.naming.Context.URL_PKG_PREFIXES);
        if (str == null) {
            str = ":org.apache.geronimo.naming";
        } else {
            str = str + ":org.apache.geronimo.naming";
        }
        System.setProperty(javax.naming.Context.URL_PKG_PREFIXES, str);

        kernel = DeploymentHelper.setUpKernelWithTransactionManager();
        DeploymentHelper.setUpTimer(kernel);

        ObjectName serverInfoObjectName = ObjectName.getInstance(DOMAIN_NAME + ":type=ServerInfo");
        GBeanData serverInfoGBean = new GBeanData(serverInfoObjectName, ServerInfo.GBEAN_INFO);
        serverInfoGBean.setAttribute("baseDirectory", ".");
        kernel.loadGBean(serverInfoGBean, testClassLoader);
        kernel.startGBean(serverInfoObjectName);
        assertRunning(kernel, serverInfoObjectName);

        ObjectName j2eeServerObjectName = ObjectName.getInstance(DOMAIN_NAME + ":j2eeType=J2EEServer,name=" + SERVER_NAME);
        GBeanData j2eeServerGBean = new GBeanData(j2eeServerObjectName, J2EEServerImpl.GBEAN_INFO);
        j2eeServerGBean.setReferencePatterns("ServerInfo", Collections.singleton(serverInfoObjectName));
        kernel.loadGBean(j2eeServerGBean, testClassLoader);
        kernel.startGBean(j2eeServerObjectName);
        assertRunning(kernel, j2eeServerObjectName);

        //load mock resource adapter for mdb
        DeploymentHelper.setUpResourceAdapter(kernel);

        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        ClassLoader cl = new URLClassLoader(new URL[]{moduleFile.toURL()}, oldCl);

        Thread.currentThread().setContextClassLoader(cl);

        try {
            ObjectName listener = null;
            OpenORBSkeletonGenerator skeletonGenerator = new OpenORBSkeletonGenerator(cl);
            skeletonGenerator.doStart();
            OpenEJBModuleBuilder moduleBuilder = new OpenEJBModuleBuilder(KernelHelper.DEFAULT_PARENTID, listener, null);

            tempDir = DeploymentUtil.createTempDir();
            EARConfigBuilder earConfigBuilder = new EARConfigBuilder(KernelHelper.DEFAULT_PARENTID,
                    DeploymentHelper.TRANSACTIONCONTEXTMANAGER_NAME,
                    DeploymentHelper.TRACKEDCONNECTIONASSOCIATOR_NAME,
                    DeploymentHelper.TRANSACTIONALTIMER_NAME,
                    DeploymentHelper.NONTRANSACTIONALTIMER_NAME,
                    null,
                    null, // repository
                    moduleBuilder,
                    moduleBuilder,
                    null,// web
                    null, resourceReferenceBuilder, // connector
                    null, // app client
                    serviceReferenceBuilder,
                    kernel
            );

            JarFile jarFile = null;
            ConfigurationData configurationData = null;
            try {
                jarFile =DeploymentUtil.createJarFile(moduleFile);
                Object plan = earConfigBuilder.getDeploymentPlan(null, jarFile);
                configurationData = earConfigBuilder.buildConfiguration(plan, jarFile, tempDir);
            } finally {
                if (jarFile != null) {
                    jarFile.close();
                }
            }

            // start the configuration
            GBeanData config = ExecutableConfigurationUtil.getConfigurationGBeanData(configurationData);
            config.setName(CONFIGURATION_OBJECT_NAME);
            config.setAttribute("baseURL", tempDir.toURL());
            config.setAttribute("parentId", KernelHelper.DEFAULT_PARENTID);

            ObjectName containerIndexObjectName = ObjectName.getInstance(DOMAIN_NAME + ":type=ContainerIndex");
            GBeanData containerIndexGBean = new GBeanData(containerIndexObjectName, ContainerIndex.GBEAN_INFO);
            Set ejbContainerNames = new HashSet();
            ejbContainerNames.add(ObjectName.getInstance(DOMAIN_NAME + ":j2eeType=StatelessSessionBean,*"));
            ejbContainerNames.add(ObjectName.getInstance(DOMAIN_NAME + ":j2eeType=StatefulSessionBean,*"));
            ejbContainerNames.add(ObjectName.getInstance(DOMAIN_NAME + ":j2eeType=EntityBean,*"));
            containerIndexGBean.setReferencePatterns("EJBContainers", ejbContainerNames);
            kernel.loadGBean(containerIndexGBean, cl);
            kernel.startGBean(containerIndexObjectName);
            assertRunning(kernel, containerIndexObjectName);

            GBeanData connectionProxyFactoryGBean = new GBeanData(CONNECTION_OBJECT_NAME, MockConnectionProxyFactory.GBEAN_INFO);
            kernel.loadGBean(connectionProxyFactoryGBean, cl);
            kernel.startGBean(CONNECTION_OBJECT_NAME);
            assertRunning(kernel, CONNECTION_OBJECT_NAME);

            dataSource = (DataSource) kernel.invoke(CONNECTION_OBJECT_NAME, "$getResource");
            Connection connection = null;
            Statement statement = null;
            try {
                connection = dataSource.getConnection();
                statement = connection.createStatement();
                statement.execute("CREATE TABLE SIMPLECMP(ID INTEGER, FIRSTNAME VARCHAR(50), LASTNAME VARCHAR(50))");
                statement.execute("CREATE TABLE PKGENCMP(ID INTEGER, FIRSTNAME VARCHAR(50), LASTNAME VARCHAR(50))");
                statement.execute("CREATE TABLE PKGENCMP_SEQ(NAME VARCHAR(50), VALUE INTEGER)");
                statement.execute("CREATE TABLE PKGENCMP2(ID INTEGER, FIRSTNAME VARCHAR(50), LASTNAME VARCHAR(50))");
            } finally {
                JDBCUtil.close(statement);
                JDBCUtil.close(connection);
            }

            // load the configuration
            kernel.loadGBean(config, cl);

            // start the configuration
            kernel.startRecursiveGBean(CONFIGURATION_OBJECT_NAME);

            assertRunning(kernel, CONFIGURATION_OBJECT_NAME);
            applicationClassLoader = (ClassLoader) kernel.getAttribute(CONFIGURATION_OBJECT_NAME, "configurationClassLoader");
        } catch (Error e) {
            DeploymentUtil.recursiveDelete(tempDir);
            throw e;
        } catch (Exception e) {
            DeploymentUtil.recursiveDelete(tempDir);
            throw e;
        } finally {
            Thread.currentThread().setContextClassLoader(oldCl);
        }
    }

    private void tearDown() throws Exception {
        try {
            kernel.stopGBean(CONFIGURATION_OBJECT_NAME);
        } catch (GBeanNotFoundException ignored) {
        }
        try {
            kernel.stopGBean(CONNECTION_OBJECT_NAME);
        } catch (GBeanNotFoundException ignored) {
        }
        DeploymentUtil.recursiveDelete(tempDir);

        try {
            DeploymentHelper.tearDownAdapter(kernel);
        } catch (Exception ignored) {
        }

        try {
            kernel.shutdown();
        } catch (Exception ignored) {
        }
        kernel = null;

        if (dataSource != null) {
            Connection connection = null;
            Statement statement = null;
            try {
                connection = dataSource.getConnection();
                statement = connection.createStatement();
                statement.execute("SHUTDOWN");
            } finally {
                JDBCUtil.close(statement);
                JDBCUtil.close(connection);
                dataSource = null;
            }
        }
    }

    private static void assertRunning(Kernel kernel, ObjectName objectName) throws Exception {
        assertEquals("should be running: " + objectName, State.RUNNING_INDEX, kernel.getGBeanState(objectName));
    }
}
