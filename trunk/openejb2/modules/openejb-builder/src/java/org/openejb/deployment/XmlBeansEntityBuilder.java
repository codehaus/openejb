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

import java.net.URI;
import java.security.Permissions;
import java.util.Map;
import java.util.SortedMap;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.EJBModule;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContext;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.ClassLoading;
import org.apache.geronimo.naming.deployment.ENCConfigBuilder;
import org.apache.geronimo.security.deployment.SecurityConfiguration;
import org.apache.geronimo.security.jacc.ComponentPermissions;
import org.apache.geronimo.xbeans.geronimo.naming.GerEjbLocalRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerEjbRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerResourceEnvRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerResourceRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerServiceRefType;
import org.apache.geronimo.xbeans.j2ee.EjbJarType;
import org.apache.geronimo.xbeans.j2ee.EjbLocalRefType;
import org.apache.geronimo.xbeans.j2ee.EjbRefType;
import org.apache.geronimo.xbeans.j2ee.EnterpriseBeansType;
import org.apache.geronimo.xbeans.j2ee.EntityBeanType;
import org.apache.geronimo.xbeans.j2ee.EnvEntryType;
import org.apache.geronimo.xbeans.j2ee.MessageDestinationRefType;
import org.apache.geronimo.xbeans.j2ee.ResourceEnvRefType;
import org.apache.geronimo.xbeans.j2ee.ResourceRefType;
import org.apache.geronimo.xbeans.j2ee.ServiceRefType;
import org.openejb.BmpEjbDeployment;
import org.openejb.EJBComponentType;
import org.openejb.proxy.ProxyInfo;
import org.openejb.xbeans.ejbjar.OpenejbEntityBeanType;
import org.openejb.xbeans.ejbjar.OpenejbTssType;


public class XmlBeansEntityBuilder extends XmlBeanBuilder {
    protected final ObjectName defaultBmpContainerName;
    protected final ObjectName defaultCmpContainerName;

    public XmlBeansEntityBuilder(OpenEjbModuleBuilder builder, ObjectName defaultBmpContainerName, ObjectName defaultCmpContainerName) {
        super(builder);
        this.defaultBmpContainerName = defaultBmpContainerName;
        this.defaultCmpContainerName = defaultCmpContainerName;
    }

    public void buildBeans(EARContext earContext, J2eeContext moduleJ2eeContext, ClassLoader cl, EJBModule ejbModule, Map openejbBeans, ComponentPermissions componentPermissions, TransactionPolicyHelper transactionPolicyHelper, EnterpriseBeansType enterpriseBeans, String policyContextID) throws DeploymentException {
        // BMP Entity Beans
        EntityBeanType[] bmpEntityBeans = enterpriseBeans.getEntityArray();
        for (int i = 0; i < bmpEntityBeans.length; i++) {
            EntityBeanType entityBean = bmpEntityBeans[i];

            OpenejbEntityBeanType openejbEntityBean = (OpenejbEntityBeanType) openejbBeans.get(entityBean.getEjbName().getStringValue().trim());
            ObjectName entityObjectName = createEJBObjectName(moduleJ2eeContext, entityBean);

            GBeanData entityGBean = createBean(earContext, ejbModule, entityObjectName, entityBean, openejbEntityBean, componentPermissions, transactionPolicyHelper, cl, policyContextID);
            earContext.addGBean(entityGBean);
        }
    }

    public GBeanData createBean(EARContext earContext, EJBModule ejbModule, ObjectName containerObjectName, EntityBeanType entityBean, OpenejbEntityBeanType openejbEntityBean, ComponentPermissions componentPermissions, TransactionPolicyHelper transactionPolicyHelper, ClassLoader cl, String policyContextID) throws DeploymentException {
        String ejbName = entityBean.getEjbName().getStringValue().trim();

        EntityBuilder builder;
        if ("Bean".equals(entityBean.getPersistenceType().getStringValue().trim())) {
            builder = new BmpBuilder();
            builder.setEjbContainerName(defaultBmpContainerName);
        } else {
            CmpBuilder cmpBuilder = new CmpBuilder();
            cmpBuilder.setEjbContainerName(defaultCmpContainerName);
            cmpBuilder.setModuleCmpEngineName(ejbModule.getModuleCmpEngineName());
            if (entityBean.isSetCmpVersion() && "1.x".equals(getStringValue(entityBean.getCmpVersion()))) {
                cmpBuilder.setCmp2(false);
            } else {
                cmpBuilder.setCmp2(true);
            }
            builder = cmpBuilder;
        }
        builder.setContainerId(containerObjectName);
        builder.setEjbName(ejbName);

        builder.setHomeInterfaceName(OpenEjbModuleBuilder.getJ2eeStringValue(entityBean.getHome()));
        builder.setRemoteInterfaceName(OpenEjbModuleBuilder.getJ2eeStringValue(entityBean.getRemote()));
        builder.setLocalHomeInterfaceName(OpenEjbModuleBuilder.getJ2eeStringValue(entityBean.getLocalHome()));
        builder.setLocalInterfaceName(OpenEjbModuleBuilder.getJ2eeStringValue(entityBean.getLocal()));
        builder.setPrimaryKeyClassName(OpenEjbModuleBuilder.getJ2eeStringValue(entityBean.getPrimKeyClass()));
        builder.setBeanClassName(entityBean.getEjbClass().getStringValue());

        SortedMap transactionPolicies = transactionPolicyHelper.getTransactionPolicies(ejbName);
        builder.setTransactionPolicies(transactionPolicies);
        builder.setReentrant(entityBean.getReentrant().getBooleanValue());

        ObjectName tssBeanObjectName = getTssBeanObjectName(openejbEntityBean, earContext);
        builder.setTssBeanName(tssBeanObjectName);

        addSecurity(earContext, ejbName, builder, cl, ejbModule, entityBean, componentPermissions, policyContextID);

        processEnvironmentRefs(builder, earContext, ejbModule, entityBean, openejbEntityBean, cl);

        try {
            GBeanData gbean = builder.createConfiguration();
            return gbean;
        } catch (Throwable e) {
            throw new DeploymentException("Unable to initialize EJBContainer GBean: ejbName=" + ejbName, e);
        }
    }

    public ObjectName createEJBObjectName(J2eeContext moduleJ2eeContext, EntityBeanType entityBean) throws DeploymentException {
        String ejbName = entityBean.getEjbName().getStringValue();
        try {
            return NameFactory.getComponentName(null, null, null, null, null, ejbName, NameFactory.ENTITY_BEAN, moduleJ2eeContext);
        } catch (MalformedObjectNameException e) {
            throw new DeploymentException("Could not construct ejb object name: " + ejbName, e);
        }
    }

    public void processEnvironmentRefs(EntityBuilder builder, EARContext earContext, EJBModule ejbModule, EntityBeanType entityBean, OpenejbEntityBeanType openejbEntityBean, ClassLoader cl) throws DeploymentException {
        // env entries
        EnvEntryType[] envEntries = entityBean.getEnvEntryArray();

        // ejb refs
        EjbRefType[] ejbRefs = entityBean.getEjbRefArray();
        GerEjbRefType[] openejbEjbRefs = null;

        EjbLocalRefType[] ejbLocalRefs = entityBean.getEjbLocalRefArray();
        GerEjbLocalRefType[] openejbEjbLocalRefs = null;

        // resource refs
        ResourceRefType[] resourceRefs = entityBean.getResourceRefArray();
        GerResourceRefType[] openejbResourceRefs = null;

        // resource env refs
        ResourceEnvRefType[] resourceEnvRefs = entityBean.getResourceEnvRefArray();
        GerResourceEnvRefType[] openejbResourceEnvRefs = null;

        ServiceRefType[] serviceRefs = entityBean.getServiceRefArray();
        GerServiceRefType[] openejbServiceRefs = null;

        if (openejbEntityBean != null) {
            openejbEjbRefs = openejbEntityBean.getEjbRefArray();
            openejbEjbLocalRefs = openejbEntityBean.getEjbLocalRefArray();
            openejbResourceRefs = openejbEntityBean.getResourceRefArray();
            openejbResourceEnvRefs = openejbEntityBean.getResourceEnvRefArray();
            openejbServiceRefs = openejbEntityBean.getServiceRefArray();
            builder.setJndiNames(openejbEntityBean.getJndiNameArray());
            builder.setLocalJndiNames(openejbEntityBean.getLocalJndiNameArray());
        } else {
            String ejbName = entityBean.getEjbName().getStringValue().trim();
            builder.setJndiNames(new String[]{ejbName});
            builder.setLocalJndiNames(new String[]{"local/" + ejbName});
        }

        MessageDestinationRefType[] messageDestinationRefs = entityBean.getMessageDestinationRefArray();

        Map context = ENCConfigBuilder.buildComponentContext(earContext, null, ejbModule, null, envEntries, ejbRefs, openejbEjbRefs, ejbLocalRefs, openejbEjbLocalRefs, resourceRefs, openejbResourceRefs, resourceEnvRefs, openejbResourceEnvRefs, messageDestinationRefs, serviceRefs, openejbServiceRefs, cl);
        builder.setComponentContext(context);
        ENCConfigBuilder.setResourceEnvironment(earContext, ejbModule.getModuleURI(), builder, resourceRefs, openejbResourceRefs);
    }

    public void initContext(EARContext earContext, J2eeContext moduleJ2eeContext, URI moduleUri, ClassLoader cl, EnterpriseBeansType enterpriseBeans) throws DeploymentException {
        EntityBeanType[] entityBeans = enterpriseBeans.getEntityArray();
        for (int i = 0; i < entityBeans.length; i++) {
            EntityBeanType entityBean = entityBeans[i];

            ObjectName entityObjectName = createEJBObjectName(moduleJ2eeContext, entityBean);
            GBeanData gbean = new GBeanData(entityObjectName, BmpEjbDeployment.GBEAN_INFO);

            Class homeInterface = null;
            Class remoteInterface = null;
            Class localHomeInterface = null;
            Class localObjectInterface = null;

            // ejb-ref
            if (entityBean.isSetRemote()) {
                String remote = entityBean.getRemote().getStringValue().trim();
                remoteInterface = ENCConfigBuilder.assureEJBObjectInterface(remote, cl);

                String home = entityBean.getHome().getStringValue().trim();
                homeInterface = ENCConfigBuilder.assureEJBHomeInterface(home, cl);
            }

            // ejb-local-ref
            if (entityBean.isSetLocal()) {
                String local = entityBean.getLocal().getStringValue().trim();
                localObjectInterface = ENCConfigBuilder.assureEJBLocalObjectInterface(local, cl);

                String localHome = entityBean.getLocalHome().getStringValue().trim();
                localHomeInterface = ENCConfigBuilder.assureEJBLocalHomeInterface(localHome, cl);
            }
            int componentType = entityBean.getPersistenceType().getStringValue().trim().equals("Bean") ? EJBComponentType.BMP_ENTITY : EJBComponentType.CMP_ENTITY;
            String className = entityBean.getPrimKeyClass().getStringValue().trim();
            Class primaryKeyClass = null;
            try {
                primaryKeyClass = ClassLoading.loadClass(className, cl);
            } catch (ClassNotFoundException e) {
                throw new DeploymentException("Could not load primary key class: " + className + " for entity: " + entityObjectName);
            }
            ProxyInfo proxyInfo = new ProxyInfo(componentType,
                    entityObjectName.getCanonicalName(),
                    homeInterface,
                    remoteInterface,
                    localHomeInterface,
                    localObjectInterface,
                    null,
                    primaryKeyClass);
            gbean.setAttribute("proxyInfo", proxyInfo);
            earContext.addGBean(gbean);
        }
    }

    protected void addSecurity(EARContext earContext, String ejbName, EntityBuilder builder, ClassLoader cl, EJBModule ejbModule, EntityBeanType entityBean, ComponentPermissions componentPermissions, String policyContextID) throws DeploymentException {
        SecurityConfiguration securityConfiguration = earContext.getSecurityConfiguration();
        if (securityConfiguration != null) {
            Permissions toBeChecked = new Permissions();
            XmlBeansSecurityBuilder xmlBeansSecurityBuilder = new XmlBeansSecurityBuilder();
            xmlBeansSecurityBuilder.addToPermissions(toBeChecked, ejbName, "Home", builder.getHomeInterfaceName(), cl);
            xmlBeansSecurityBuilder.addToPermissions(toBeChecked, ejbName, "LocalHome", builder.getLocalHomeInterfaceName(), cl);
            xmlBeansSecurityBuilder.addToPermissions(toBeChecked, ejbName, "Remote", builder.getRemoteInterfaceName(), cl);
            xmlBeansSecurityBuilder.addToPermissions(toBeChecked, ejbName, "Local", builder.getLocalInterfaceName(), cl);
            String defaultRole = securityConfiguration.getDefaultRole();
            xmlBeansSecurityBuilder.addComponentPermissions(defaultRole,
                    toBeChecked,
                    ((EjbJarType) ejbModule.getSpecDD()).getAssemblyDescriptor(),
                    ejbName,
                    entityBean.getSecurityRoleRefArray(), componentPermissions);

            xmlBeansSecurityBuilder.setDetails(entityBean.getSecurityIdentity(), securityConfiguration, policyContextID, builder);
        }
    }

    protected ObjectName getTssBeanObjectName(OpenejbEntityBeanType openejbEntityBean, EARContext earContext) throws DeploymentException {
        ObjectName tssBeanObjectName = null;
        if (openejbEntityBean != null) {
            if (openejbEntityBean.isSetTssTargetName()) {
                String tssName = openejbEntityBean.getTssTargetName().trim();
                try {
                    tssBeanObjectName = ObjectName.getInstance(tssName);
                } catch (MalformedObjectNameException e) {
                    throw new DeploymentException("Invalid object name for tss bean", e);
                }
            } else if (openejbEntityBean.isSetTssLink()) {
                String tssBeanLink = openejbEntityBean.getTssLink().trim();
                //todo check this is correct
                URI moduleURI = URI.create("");
                String moduleType = null;
                tssBeanObjectName = earContext.getRefContext().locateComponentName(tssBeanLink, moduleURI, moduleType, NameFactory.CORBA_TSS, earContext.getJ2eeContext(), earContext, "TSS GBean");
            } else if (openejbEntityBean.isSetTss()) {
                OpenejbTssType tss = openejbEntityBean.getTss();
                try {
                    tssBeanObjectName = NameFactory.getComponentName(getStringValue(tss.getDomain()),
                            getStringValue(tss.getServer()),
                            getStringValue(tss.getApplication()),
                            getStringValue(tss.getModule()),
                            getStringValue(tss.getName()),
                            getStringValue(NameFactory.CORBA_TSS),
                            earContext.getJ2eeContext());
                } catch (MalformedObjectNameException e) {
                    throw new DeploymentException("Invalid object name for tss bean", e);
                }
            }
        }
        return tssBeanObjectName;
    }
}