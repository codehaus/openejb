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

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.service.EnvironmentBuilder;
import org.apache.geronimo.deployment.service.ServiceConfigBuilder;
import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.deployment.xbeans.ArtifactType;
import org.apache.geronimo.deployment.xbeans.EnvironmentType;
import org.apache.geronimo.deployment.xbeans.GbeanType;
import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.EJBModule;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.ModuleBuilder;
import org.apache.geronimo.j2ee.deployment.WebServiceBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.naming.deployment.ENCConfigBuilder;
import org.apache.geronimo.schema.NamespaceElementConverter;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.geronimo.security.deployment.SecurityBuilder;
import org.apache.geronimo.security.deployment.SecurityConfiguration;
import org.apache.geronimo.security.jacc.ComponentPermissions;
import org.apache.geronimo.xbeans.geronimo.naming.GerMessageDestinationType;
import org.apache.geronimo.xbeans.geronimo.naming.GerResourceLocatorType;
import org.apache.geronimo.xbeans.j2ee.AssemblyDescriptorType;
import org.apache.geronimo.xbeans.j2ee.EjbJarDocument;
import org.apache.geronimo.xbeans.j2ee.EjbJarType;
import org.apache.geronimo.xbeans.j2ee.EnterpriseBeansType;
import org.apache.geronimo.xbeans.j2ee.EntityBeanType;
import org.apache.geronimo.xbeans.j2ee.MessageDestinationType;
import org.apache.geronimo.xbeans.j2ee.MessageDrivenBeanType;
import org.apache.geronimo.xbeans.j2ee.SessionBeanType;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.openejb.EJBModuleImpl;
import org.openejb.deployment.corba.NoDistributedTxTransactionImportPolicyBuilder;
import org.openejb.deployment.corba.TransactionImportPolicyBuilder;
import org.openejb.deployment.pkgen.TranQLPKGenBuilder;
import org.openejb.proxy.EJBProxyFactory;
import org.openejb.xbeans.ejbjar.OpenejbEntityBeanType;
import org.openejb.xbeans.ejbjar.OpenejbMessageDrivenBeanType;
import org.openejb.xbeans.ejbjar.OpenejbOpenejbJarDocument;
import org.openejb.xbeans.ejbjar.OpenejbOpenejbJarType;
import org.openejb.xbeans.ejbjar.OpenejbSessionBeanType;
import org.openejb.xbeans.pkgen.EjbKeyGeneratorDocument;
import org.tranql.cache.GlobalSchema;
import org.tranql.ejb.EJBSchema;
import org.tranql.ejb.TransactionManagerDelegate;
import org.tranql.sql.DataSourceDelegate;
import org.tranql.sql.SQLSchema;

import javax.management.MalformedObjectNameException;
import javax.xml.namespace.QName;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.Permissions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;


/**
 * Master builder for processing EJB JAR deployments and creating the
 * correspinding runtime objects (GBeans, etc.).
 *
 * @version $Revision$ $Date$
 */
public class OpenEJBModuleBuilder implements ModuleBuilder {

    private final Environment defaultEnvironment;
    private final AbstractNameQuery listener;
    private final CMPEntityBuilder cmpEntityBuilder;
    private final SessionBuilder sessionBuilder;
    private final EntityBuilder entityBuilder;
    private final MdbBuilder mdbBuilder;
    private final WebServiceBuilder webServiceBuilder;
    private final TransactionImportPolicyBuilder transactionImportPolicyBuilder;
    private static QName OPENEJBJAR_QNAME = OpenejbOpenejbJarDocument.type.getDocumentElementName();
    private static final String OPENEJBJAR_NAMESPACE = OPENEJBJAR_QNAME.getNamespaceURI();

    static {
        Map conversions = new HashMap();
        QName name = EjbKeyGeneratorDocument.type.getDocumentElementName();
        conversions.put(name.getLocalPart(), new NamespaceElementConverter(name.getNamespaceURI()));
        SchemaConversionUtils.registerNamespaceConversions(conversions);
    }

    public OpenEJBModuleBuilder(Environment defaultEnvironment, AbstractNameQuery listener, Object webServiceLinkTemplate, WebServiceBuilder webServiceBuilder, Kernel kernel) throws GBeanNotFoundException {
        this(defaultEnvironment, listener, getLinkData(kernel, webServiceLinkTemplate), webServiceBuilder);
    }

    public OpenEJBModuleBuilder(Environment defaultEnvironment, AbstractNameQuery listener, GBeanData linkTemplate, WebServiceBuilder webServiceBuilder) {
        this.defaultEnvironment = defaultEnvironment;
        this.listener = listener;
        this.transactionImportPolicyBuilder = new NoDistributedTxTransactionImportPolicyBuilder();
        this.cmpEntityBuilder = new CMPEntityBuilder(this);
        this.sessionBuilder = new SessionBuilder(this, linkTemplate, webServiceBuilder);
        this.entityBuilder = new EntityBuilder(this);
        this.mdbBuilder = new MdbBuilder(this);
        this.webServiceBuilder = webServiceBuilder;
    }

    private static GBeanData getLinkData(Kernel kernel, Object webServiceLinkTemplate) throws GBeanNotFoundException {
        AbstractName webServiceLinkTemplateName = kernel.getProxyManager().getProxyTarget(webServiceLinkTemplate);
        return kernel.getGBeanData(webServiceLinkTemplateName);
    }

    public TransactionImportPolicyBuilder getTransactionImportPolicyBuilder() {
        return transactionImportPolicyBuilder;
    }

    public Module createModule(File plan, JarFile moduleFile) throws DeploymentException {
        return createModule(plan, moduleFile, "ejb", null, true, null);
    }

    public Module createModule(Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, Environment environment, Object moduleContextInfo, AbstractName earName) throws DeploymentException {
        return createModule(plan, moduleFile, targetPath, specDDUrl, false, earName);
    }

    private Module createModule(Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, boolean standAlone, AbstractName earName) throws DeploymentException {
        assert moduleFile != null: "moduleFile is null";
        assert targetPath != null: "targetPath is null";
        assert !targetPath.endsWith("/"): "targetPath must not end with a '/'";

        String specDD;
        EjbJarType ejbJar;
        try {
            if (specDDUrl == null) {
                specDDUrl = DeploymentUtil.createJarURL(moduleFile, "META-INF/ejb-jar.xml");
            }

            // read in the entire specDD as a string, we need this for getDeploymentDescriptor
            // on the J2ee management object
            specDD = DeploymentUtil.readAll(specDDUrl);
        } catch (Exception e) {
            return null;
        }
        //there is a file named ejb-jar.xml in META-INF.  If we can't process it, it is an error.
        try {
            // parse it
            EjbJarDocument ejbJarDoc = SchemaConversionUtils.convertToEJBSchema(XmlBeansUtil.parse(specDD));
            ejbJar = ejbJarDoc.getEjbJar();
        } catch (XmlException e) {
            throw new DeploymentException("Error parsing ejb-jar.xml", e);
        }

        OpenejbOpenejbJarType openejbJar = getOpenejbJar(plan, moduleFile, standAlone, targetPath, ejbJar);
        if (openejbJar == null)
        { // Avoid NPE GERONIMO-1220; todo: remove this if we can work around the requirement for a plan
            throw new DeploymentException("Currently a Geronimo deployment plan is required for an EJB module.  Please provide a plan as a deployer argument or packaged in the EJB JAR at META-INF/openejb-jar.xml");
        }

        EnvironmentType environmentType = openejbJar.getEnvironment();
        Environment environment = EnvironmentBuilder.buildEnvironment(environmentType, defaultEnvironment);
        AbstractName moduleName;
        if (earName == null) {
            try {
                moduleName = NameFactory.buildModuleName(environment.getProperties(), environment.getConfigId(), ConfigurationModuleType.EJB, null);
            } catch (MalformedObjectNameException e) {
                throw new DeploymentException("Could not construct standalone ejb module name", e);
            }
        } else {
            moduleName = NameFactory.getChildName(earName, NameFactory.EJB_MODULE, targetPath, null);
        }

        return new EJBModule(standAlone, moduleName, environment, moduleFile, targetPath, ejbJar, openejbJar, specDD);
    }

    OpenejbOpenejbJarType getOpenejbJar(Object plan, JarFile moduleFile, boolean standAlone, String targetPath, EjbJarType ejbJar) throws DeploymentException {
        OpenejbOpenejbJarType openejbJar;
        XmlObject rawPlan = null;
        try {
            // load the openejb-jar.xml from either the supplied plan or from the earFile
            try {
                if (plan instanceof XmlObject) {
                    rawPlan = (XmlObject) plan;
                } else {
                    if (plan != null) {
                        rawPlan = XmlBeansUtil.parse(((File) plan).toURL());
                    } else {
                        URL path = DeploymentUtil.createJarURL(moduleFile, "META-INF/openejb-jar.xml");
                        rawPlan = XmlBeansUtil.parse(path);
                    }
                }
            } catch (IOException e) {
                //no plan, create a default
            }

            // if we got one extract, adjust, and validate it otherwise create a default one
            if (rawPlan != null) {
                openejbJar = (OpenejbOpenejbJarType) SchemaConversionUtils.fixGeronimoSchema(rawPlan, OPENEJBJAR_QNAME, OpenejbOpenejbJarType.type);
            } else {
                String path;
                if (standAlone) {
                    // default configId is based on the moduleFile name
                    path = new File(moduleFile.getName()).getName();
                } else {
                    // default configId is based on the module uri from the application.xml
                    path = targetPath;
                }
                openejbJar = createDefaultPlan(path, ejbJar);
            }
        } catch (XmlException e) {
            throw new DeploymentException(e);
        }
        return openejbJar;
    }

    private OpenejbOpenejbJarType createDefaultPlan(String name, EjbJarType ejbJar) {
        String id = ejbJar.getId();
        if (id == null) {
            id = name;
            if (id.endsWith(".jar")) {
                id = id.substring(0, id.length() - 4);
            }
            if (id.endsWith("/")) {
                id = id.substring(0, id.length() - 1);
            }
        }

        OpenejbOpenejbJarType openejbEjbJar = OpenejbOpenejbJarType.Factory.newInstance();
        EnvironmentType environmentType = openejbEjbJar.addNewEnvironment();
        ArtifactType artifact = environmentType.addNewConfigId();
        //TODO this version is incomplete.
        artifact.setGroupId("unknown");
        artifact.setArtifactId(id);
        artifact.setVersion("1");
        artifact.setType("car");
        openejbEjbJar.addNewEnterpriseBeans();
        return openejbEjbJar;
    }

    public void installModule(JarFile earFile, EARContext earContext, Module module, ConfigurationStore configurationStore, Repository repository) throws DeploymentException {
        JarFile moduleFile = module.getModuleFile();
        try {
            // extract the ejbJar file into a standalone packed jar file and add the contents to the output
            earContext.addIncludeAsPackedJar(URI.create(module.getTargetPath()), moduleFile);
        } catch (IOException e) {
            throw new DeploymentException("Unable to copy ejb module jar into configuration: " + moduleFile.getName());
        }

    }

    public void initContext(EARContext earContext, Module module, ClassLoader cl) throws DeploymentException {
        AbstractName moduleBaseName = module.getModuleName();
        URI moduleUri = module.getModuleURI();

        EJBModule ejbModule = (EJBModule) module;
        EjbJarType ejbJar = (EjbJarType) ejbModule.getSpecDD();

        if (ejbJar.isSetAssemblyDescriptor()) {
            AssemblyDescriptorType assemblyDescriptor = ejbJar.getAssemblyDescriptor();

            MessageDestinationType[] messageDestinations = assemblyDescriptor.getMessageDestinationArray();
            OpenejbOpenejbJarType openejbJar = (OpenejbOpenejbJarType) module.getVendorDD();
            GerMessageDestinationType[] gerMessageDestinations = openejbJar.getMessageDestinationArray();

            ENCConfigBuilder.registerMessageDestinations(earContext.getRefContext(), module.getName(), messageDestinations, gerMessageDestinations);
        }

        EnterpriseBeansType enterpriseBeans = ejbJar.getEnterpriseBeans();

        sessionBuilder.initContext(earContext, moduleBaseName, moduleUri, cl, enterpriseBeans);
        entityBuilder.initContext(earContext, moduleBaseName, moduleUri, cl, enterpriseBeans);
        mdbBuilder.initContext(cl, enterpriseBeans);

    }

    public CMPEntityBuilder getCmpEntityBuilder() {
        return cmpEntityBuilder;
    }

    public EntityBuilder getBmpEntityBuilder() {
        return entityBuilder;
    }

    public MdbBuilder getMdbBuilder() {
        return mdbBuilder;
    }

    public SessionBuilder getSessionBuilder() {
        return sessionBuilder;
    }

    /**
     * Does the meaty work of processing the deployment information and
     * creating GBeans for all the EJBs in the JAR, etc.
     */
    public void addGBeans(EARContext earContext, Module module, ClassLoader cl, Repository repository) throws DeploymentException {
        AbstractName moduleBaseName = module.getModuleName();

        DataSourceDelegate delegate = new DataSourceDelegate();
        TransactionManagerDelegate tmDelegate = new TransactionManagerDelegate();

        // Handle automatic PK generation -- we want to use the same builder for all CMP entities
        TranQLPKGenBuilder pkgen = new TranQLPKGenBuilder();

        EJBModule ejbModule = (EJBModule) module;
        OpenejbOpenejbJarType openejbEjbJar = (OpenejbOpenejbJarType) module.getVendorDD();
        EjbJarType ejbJar = (EjbJarType) module.getSpecDD();

        // @todo need a better schema name
        Schemata schemata = cmpEntityBuilder.buildSchemata(earContext, moduleBaseName, ejbModule.getName(), ejbJar, openejbEjbJar, cl, pkgen, delegate, tmDelegate);
        EJBSchema ejbSchema = schemata.getEjbSchema();
        SQLSchema sqlSchema = schemata.getSqlSchema();
        GlobalSchema globalSchema = schemata.getGlobalSchema();

        GbeanType[] gbeans = openejbEjbJar.getGbeanArray();
        ServiceConfigBuilder.addGBeans(gbeans, cl, moduleBaseName, earContext);

        GBeanData ejbModuleGBeanData = new GBeanData(moduleBaseName, EJBModuleImpl.GBEAN_INFO);
        try {
            ejbModuleGBeanData.setReferencePattern("J2EEServer", earContext.getServerObjectName());
            if (!earContext.getJ2EEApplicationName().equals("null")) {
                ejbModuleGBeanData.setReferencePattern("J2EEApplication", earContext.getApplicationName());
            }

            ejbModuleGBeanData.setAttribute("deploymentDescriptor", module.getOriginalSpecDD());

            GerResourceLocatorType connectionFactoryLocator = openejbEjbJar.getCmpConnectionFactory();
            if (connectionFactoryLocator != null) {
                AbstractNameQuery connectionFactoryNameQuery = getResourceContainerId(connectionFactoryLocator, earContext);
                //TODO this uses connection factory rather than datasource for the type.
                ejbModuleGBeanData.setReferencePattern("ConnectionFactory", connectionFactoryNameQuery);
                ejbModuleGBeanData.setAttribute("Delegate", delegate);
            } else if (!ejbSchema.getEntities().isEmpty()) {
                throw new DeploymentException("A cmp-connection-factory element must be specified as CMP EntityBeans are defined.");
            }

            ejbModuleGBeanData.setReferencePattern("TransactionContextManager", earContext.getTransactionContextManagerObjectName());
            ejbModuleGBeanData.setAttribute("TMDelegate", tmDelegate);
            earContext.addGBean(ejbModuleGBeanData);
        } catch (Exception e) {
            throw new DeploymentException("Unable to initialize EJBModule GBean " + ejbModuleGBeanData.getName(), e);
        }

        EnterpriseBeansType enterpriseBeans = ejbJar.getEnterpriseBeans();
        Set beans = new HashSet();
        EntityBeanType[] ebs = enterpriseBeans.getEntityArray();
        for (int i = 0; i < ebs.length; i++) {
            beans.add(ebs[i].getEjbName().getStringValue().trim());
        }
        SessionBeanType[] sbs = enterpriseBeans.getSessionArray();
        for (int i = 0; i < sbs.length; i++) {
            beans.add(sbs[i].getEjbName().getStringValue().trim());
        }
        MessageDrivenBeanType[] mbs = enterpriseBeans.getMessageDrivenArray();
        for (int i = 0; i < mbs.length; i++) {
            beans.add(mbs[i].getEjbName().getStringValue().trim());
        }

        // create an index of the openejb ejb configurations by ejb-name
        Map openejbBeans = new HashMap();
        List badBeans = new ArrayList();
        //overridden web service locations
        Map correctedPortLocations = new HashMap();

        OpenejbSessionBeanType[] openejbSessionBeans = openejbEjbJar.getEnterpriseBeans().getSessionArray();
        for (int i = 0; i < openejbSessionBeans.length; i++) {
            OpenejbSessionBeanType sessionBean = openejbSessionBeans[i];
            if (beans.contains(sessionBean.getEjbName())) {
                openejbBeans.put(sessionBean.getEjbName(), sessionBean);
                if (sessionBean.isSetWebServiceAddress()) {
                    String location = sessionBean.getWebServiceAddress().trim();
                    correctedPortLocations.put(sessionBean.getEjbName(), location);
                }
            } else {
                badBeans.add(sessionBean.getEjbName());
            }
        }
        OpenejbEntityBeanType[] openejbEntityBeans = openejbEjbJar.getEnterpriseBeans().getEntityArray();
        for (int i = 0; i < openejbEntityBeans.length; i++) {
            OpenejbEntityBeanType entityBean = openejbEntityBeans[i];
            if (beans.contains(entityBean.getEjbName())) {
                openejbBeans.put(entityBean.getEjbName(), entityBean);
            } else {
                badBeans.add(entityBean.getEjbName());
            }
        }
        OpenejbMessageDrivenBeanType[] openejbMessageDrivenBeans = openejbEjbJar.getEnterpriseBeans().getMessageDrivenArray();
        for (int i = 0; i < openejbMessageDrivenBeans.length; i++) {
            OpenejbMessageDrivenBeanType messageDrivenBean = openejbMessageDrivenBeans[i];
            if (beans.contains(messageDrivenBean.getEjbName())) {
                openejbBeans.put(messageDrivenBean.getEjbName(), messageDrivenBean);
            } else {
                badBeans.add(messageDrivenBean.getEjbName());
            }
        }

        if (badBeans.size() > 0) {
            if (badBeans.size() == 1) {
                throw new DeploymentException("EJB '" + badBeans.get(0) + "' is described in OpenEJB deployment plan but does not exist in META-INF/ejb-jar.xml");
            }
            StringBuffer buf = new StringBuffer();
            buf.append("The following EJBs are described in the OpenEJB deployment plan but do not exist in META-INF/ejb-jar.xml: ");
            for (int i = 0; i < badBeans.size(); i++) {
                if (i > 0) buf.append(", ");
                buf.append(badBeans.get(i));
            }
            throw new DeploymentException(buf.toString());
        }

        Map portInfoMap = Collections.EMPTY_MAP;
        JarFile jarFile = ejbModule.getModuleFile();
        URL wsDDUrl;
        try {
            wsDDUrl = DeploymentUtil.createJarURL(jarFile, "META-INF/webservices.xml");
            portInfoMap = webServiceBuilder.parseWebServiceDescriptor(wsDDUrl, jarFile, true, correctedPortLocations);
        } catch (MalformedURLException e) {
            //there is no webservices file
        }


        TransactionPolicyHelper transactionPolicyHelper;
        if (ejbJar.isSetAssemblyDescriptor()) {
            transactionPolicyHelper = new TransactionPolicyHelper(ejbJar.getAssemblyDescriptor().getContainerTransactionArray());
        } else {
            transactionPolicyHelper = new TransactionPolicyHelper();
        }

        /**
         * Build the security configuration.  Attempt to auto generate role mappings.
         */
        if (openejbEjbJar.isSetSecurity()) {
            SecurityConfiguration securityConfiguration = SecurityBuilder.buildSecurityConfiguration(openejbEjbJar.getSecurity(), cl);
            earContext.setSecurityConfiguration(securityConfiguration);
        }


        ComponentPermissions componentPermissions = new ComponentPermissions(new Permissions(), new Permissions(), new HashMap());
        //TODO go back to the commented version when possible
//          String contextID = ejbModuleObjectName.getCanonicalName();
        String policyContextID = moduleBaseName.toString().replaceAll("[,: ]", "_");

        sessionBuilder.buildBeans(earContext, moduleBaseName, cl, ejbModule, componentPermissions, openejbBeans, transactionPolicyHelper, enterpriseBeans, listener, policyContextID, portInfoMap);

        entityBuilder.buildBeans(earContext, moduleBaseName, cl, ejbModule, openejbBeans, componentPermissions, transactionPolicyHelper, enterpriseBeans, policyContextID);

        cmpEntityBuilder.buildBeans(earContext, moduleBaseName, cl, ejbModule, ejbSchema, sqlSchema, globalSchema, openejbBeans, transactionPolicyHelper, enterpriseBeans, tmDelegate, componentPermissions, policyContextID);

        mdbBuilder.buildBeans(earContext, moduleBaseName, cl, ejbModule, openejbBeans, transactionPolicyHelper, enterpriseBeans, componentPermissions, policyContextID);

        earContext.addSecurityContext(policyContextID, componentPermissions);
    }

    public String getSchemaNamespace() {
        return OPENEJBJAR_NAMESPACE;
    }

    private static AbstractNameQuery getResourceContainerId(GerResourceLocatorType resourceLocator, EARContext earContext) throws GBeanNotFoundException {
        AbstractNameQuery resourceQuery;
        if (resourceLocator.isSetResourceLink()) {
            resourceQuery = ENCConfigBuilder.buildAbstractNameQuery(null, NameFactory.JCA_MANAGED_CONNECTION_FACTORY, resourceLocator.getResourceLink().trim());
        } else {
            //construct name from components
            resourceQuery = ENCConfigBuilder.buildAbstractNameQuery(resourceLocator.getPattern(), NameFactory.JCA_MANAGED_CONNECTION_FACTORY);
        }
        Configuration configuration = earContext.getConfiguration();
        //throws GBeanNotFoundException if not satisfied
        configuration.findGBean(resourceQuery);
        return resourceQuery;
    }


    public Object createEJBProxyFactory(String containerId, boolean isSessionBean, String remoteInterfaceName, String homeInterfaceName, String localInterfaceName, String localHomeInterfaceName, ClassLoader cl) throws DeploymentException {
        Class remoteInterface = loadClass(cl, remoteInterfaceName);
        Class homeInterface = loadClass(cl, homeInterfaceName);
        Class localInterface = loadClass(cl, localInterfaceName);
        Class localHomeInterface = loadClass(cl, localHomeInterfaceName);
        return new EJBProxyFactory(containerId, isSessionBean, remoteInterface, homeInterface, localInterface, localHomeInterface);
    }

    private Class loadClass(ClassLoader cl, String name) throws DeploymentException {
        if (name == null) {
            return null;
        }
        try {
            return cl.loadClass(name);
        } catch (ClassNotFoundException e) {
            throw new DeploymentException("Unable to load Class: " + name);
        }
    }

    protected static String getJ2eeStringValue(org.apache.geronimo.xbeans.j2ee.String string) {
        if (string == null) {
            return null;
        }
        return string.getStringValue();
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(OpenEJBModuleBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addAttribute("defaultEnvironment", Environment.class, true);
        infoBuilder.addAttribute("listener", AbstractNameQuery.class, true);
        infoBuilder.addReference("WebServiceLinkTemplate", Object.class, NameFactory.WEB_SERVICE_LINK);
        infoBuilder.addReference("WebServiceBuilder", WebServiceBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addInterface(ModuleBuilder.class);
        infoBuilder.addAttribute("kernel", Kernel.class, false);

        infoBuilder.setConstructor(new String[]{"defaultEnvironment", "listener", "WebServiceLinkTemplate", "WebServiceBuilder", "kernel"});
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
