/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.openejb.deployment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.management.ObjectName;

import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelFactory;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationManagerImpl;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.DefaultArtifactManager;
import org.apache.geronimo.kernel.repository.DefaultArtifactResolver;
import org.apache.geronimo.kernel.repository.Environment;

/**
 * @version $Rev$ $Date$
 */
public class KernelHelper {
    public static final Environment DEFAULT_ENVIRONMENT = new Environment();
    public static final Environment ENVIRONMENT = new Environment();

    static {
        Map Properties = new HashMap();
        Properties.put(NameFactory.JSR77_BASE_NAME_PROPERTY, DeploymentHelper.BASE_NAME);
        Artifact defaultConfigId = Artifact.create("geronimo/server/1/car");
        DEFAULT_ENVIRONMENT.setConfigId(defaultConfigId);
        DEFAULT_ENVIRONMENT.addProperties(Properties);
        Artifact configId = Artifact.create("test/test/1/car");
        ENVIRONMENT.setConfigId(configId);
        ENVIRONMENT.addProperties(Properties);
    }

    public static Kernel getPreparedKernel() throws Exception {
        Kernel kernel = KernelFactory.newInstance().createKernel("bar");
        kernel.boot();
        GBeanData store = new GBeanData(JMXUtil.getObjectName("foo:j2eeType=ConfigurationStore,name=mock"), MockConfigStore.GBEAN_INFO);
        kernel.loadGBean(store, KernelHelper.class.getClassLoader());
        kernel.startGBean(store.getName());

        GBeanData artifactManager = new GBeanData(JMXUtil.getObjectName("foo:name=ArtifactManager"), DefaultArtifactManager.GBEAN_INFO);
        kernel.loadGBean(artifactManager, KernelHelper.class.getClassLoader());
        kernel.startGBean(artifactManager.getName());

        GBeanData artifactResolver = new GBeanData(JMXUtil.getObjectName("foo:name=ArtifactResolver"), DefaultArtifactResolver.GBEAN_INFO);
        artifactResolver.setReferencePattern("ArtifactManager", artifactManager.getName());
        kernel.loadGBean(artifactResolver, KernelHelper.class.getClassLoader());
        kernel.startGBean(artifactResolver.getName());

        ObjectName configurationManagerName = new ObjectName(":j2eeType=ConfigurationManager,name=Basic");
        GBeanData configurationManagerData = new GBeanData(configurationManagerName, ConfigurationManagerImpl.GBEAN_INFO);
        configurationManagerData.setReferencePattern("Stores", store.getName());
        configurationManagerData.setReferencePattern("ArtifactManager", artifactManager.getName());
        configurationManagerData.setReferencePattern("ArtifactResolver", artifactResolver.getName());
        kernel.loadGBean(configurationManagerData, KernelHelper.class.getClassLoader());
        kernel.startGBean(configurationManagerName);
        ConfigurationManager configurationManager = (ConfigurationManager) kernel.getProxyManager().createProxy(configurationManagerName, ConfigurationManager.class);

        Artifact artifact = DEFAULT_ENVIRONMENT.getConfigId();
        configurationManager.loadConfiguration(artifact);
        configurationManager.startConfiguration(artifact);

        return kernel;
    }


    public static class MockConfigStore implements ConfigurationStore {
        private static final Map locations = new HashMap();

        public MockConfigStore() {
        }

        public void install(ConfigurationData configurationData) throws IOException, InvalidConfigException {
        }

        public void uninstall(Artifact configID) throws NoSuchConfigException, IOException {
        }

        public GBeanData loadConfiguration(Artifact configId) throws NoSuchConfigException, IOException, InvalidConfigException {
            ObjectName configurationObjectName = Configuration.getConfigurationObjectName(configId);
            GBeanData configData = new GBeanData(configurationObjectName, Configuration.GBEAN_INFO);
            Environment environment = new Environment();
            environment.setConfigId(configId);
            environment.getProperties().put(NameFactory.JSR77_BASE_NAME_PROPERTY, "geronimo.test:J2EEServer=geronimo");
            configData.setAttribute("environment", environment);
            configData.setAttribute("gBeanState", NO_OBJECTS_OS);
            configData.setAttribute("configurationStore", this);

            return configData;
        }

        public boolean containsConfiguration(Artifact configID) {
            return true;
        }

        public String getObjectName() {
            return null;
        }

        public List listConfigurations() {
            return null;
        }

        public File createNewConfigurationDir(Artifact configId) {
            try {
                File file = DeploymentUtil.createTempDir();
                locations.put(configId, file);
                return file;
            } catch (IOException e) {
                return null;
            }
        }

        public URL resolve(Artifact configId, URI uri) throws NoSuchConfigException, MalformedURLException {
            File file = (File) locations.get(configId);
            if (file == null) {
//                throw new NoSuchConfigException("nothing for configid " + configId);
                return new File("foo").toURL();
            }
            return new URL(file.toURL(), uri.toString());
        }

        public final static GBeanInfo GBEAN_INFO;

        private static final byte[] NO_OBJECTS_OS;

        static {
            GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(MockConfigStore.class, NameFactory.CONFIGURATION_STORE);
            infoBuilder.addInterface(ConfigurationStore.class);
            GBEAN_INFO = infoBuilder.getBeanInfo();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.flush();
                NO_OBJECTS_OS = baos.toByteArray();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}