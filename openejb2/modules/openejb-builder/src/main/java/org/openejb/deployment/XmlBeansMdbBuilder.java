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

import java.beans.Introspector;
import java.lang.reflect.InvocationTargetException;
import java.security.Permissions;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.EJBModule;
import org.apache.geronimo.j2ee.deployment.RefContext;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.naming.deployment.ENCConfigBuilder;
import org.apache.geronimo.security.deployment.SecurityConfiguration;
import org.apache.geronimo.security.jacc.ComponentPermissions;
import org.apache.geronimo.xbeans.geronimo.naming.GerEjbLocalRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerEjbRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerGbeanRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerMessageDestinationType;
import org.apache.geronimo.xbeans.geronimo.naming.GerResourceEnvRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerResourceLocatorType;
import org.apache.geronimo.xbeans.geronimo.naming.GerResourceRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerServiceRefType;
import org.apache.geronimo.xbeans.j2ee.ActivationConfigPropertyType;
import org.apache.geronimo.xbeans.j2ee.EjbJarType;
import org.apache.geronimo.xbeans.j2ee.EjbLocalRefType;
import org.apache.geronimo.xbeans.j2ee.EjbRefType;
import org.apache.geronimo.xbeans.j2ee.EnterpriseBeansType;
import org.apache.geronimo.xbeans.j2ee.EnvEntryType;
import org.apache.geronimo.xbeans.j2ee.MessageDestinationRefType;
import org.apache.geronimo.xbeans.j2ee.MessageDrivenBeanType;
import org.apache.geronimo.xbeans.j2ee.ResourceEnvRefType;
import org.apache.geronimo.xbeans.j2ee.ResourceRefType;
import org.apache.geronimo.xbeans.j2ee.ServiceRefType;
import org.openejb.xbeans.ejbjar.OpenejbActivationConfigPropertyType;
import org.openejb.xbeans.ejbjar.OpenejbMessageDrivenBeanType;


public class XmlBeansMdbBuilder extends XmlBeanBuilder {
    private static final Log log = LogFactory.getLog(XmlBeansMdbBuilder.class);
    private final Kernel kernel;
    private final String defaultMdbEjbContainer;

    public XmlBeansMdbBuilder(OpenEjbModuleBuilder moduleBuilder, Kernel kernel, String defaultMdbEjbContainer) {
        super(moduleBuilder);
        this.kernel = kernel;
        this.defaultMdbEjbContainer = defaultMdbEjbContainer;
    }

    protected void buildBeans(EARContext earContext, AbstractName moduleBaseName, ClassLoader cl, EJBModule ejbModule, Map openejbBeans, TransactionPolicyHelper transactionPolicyHelper, EnterpriseBeansType enterpriseBeans, ComponentPermissions componentPermissions, String policyContextID) throws DeploymentException {
        // Message Driven Beans
        MessageDrivenBeanType[] messageDrivenBeans = enterpriseBeans.getMessageDrivenArray();
        for (int i = 0; i < messageDrivenBeans.length; i++) {
            MessageDrivenBeanType messageDrivenBean = messageDrivenBeans[i];

            OpenejbMessageDrivenBeanType openejbMessageDrivenBean = (OpenejbMessageDrivenBeanType) openejbBeans.get(messageDrivenBean.getEjbName().getStringValue().trim());
            if (openejbMessageDrivenBean == null) {
                throw new DeploymentException("No openejb deployment descriptor for mdb: " + messageDrivenBean.getEjbName().getStringValue() + ". Known beans: " + openejbBeans.keySet().toArray());
            }
            String ejbName = messageDrivenBean.getEjbName().getStringValue().trim();
            AbstractName messageDrivenAbstractName = earContext.getNaming().createChildName(moduleBaseName, ejbName, NameFactory.MESSAGE_DRIVEN_BEAN);
            AbstractName activationSpecName = earContext.getNaming().createChildName(messageDrivenAbstractName, ejbName, NameFactory.JCA_ACTIVATION_SPEC);

            String containerId = messageDrivenAbstractName.toString();
            addActivationSpecWrapperGBean(earContext,
                    activationSpecName,
                    openejbMessageDrivenBean.isSetActivationConfig() ? openejbMessageDrivenBean.getActivationConfig().getActivationConfigPropertyArray() : null,
                    messageDrivenBean.isSetActivationConfig() ? messageDrivenBean.getActivationConfig().getActivationConfigPropertyArray() : new ActivationConfigPropertyType[]{},
                    openejbMessageDrivenBean.getResourceAdapter(),
                    getMessagingType(messageDrivenBean),
                    containerId,
                    messageDrivenBean.isSetMessageDestinationLink() ? messageDrivenBean.getMessageDestinationLink().getStringValue() : null,
                    messageDrivenBean.isSetMessageDestinationType() ? messageDrivenBean.getMessageDestinationType().getStringValue() : null,
                    messageDrivenBean.getEjbName().getStringValue());
            GBeanData messageDrivenGBean = createBean(earContext, ejbModule, containerId, messageDrivenBean, openejbMessageDrivenBean, activationSpecName, transactionPolicyHelper, cl, componentPermissions, policyContextID);
            messageDrivenGBean.setAbstractName(messageDrivenAbstractName);
            try {
                earContext.addGBean(messageDrivenGBean);
            } catch (GBeanAlreadyExistsException e) {
                throw new DeploymentException("Could not add message driven bean to context", e);
            }
        }
    }

    public void initContext(ClassLoader cl, EnterpriseBeansType enterpriseBeans) throws DeploymentException {
        // Message Driven Beans
        // the only relevant action is to check that the messagingType is available.
        MessageDrivenBeanType[] messageDrivenBeans = enterpriseBeans.getMessageDrivenArray();
        for (int i = 0; i < messageDrivenBeans.length; i++) {
            MessageDrivenBeanType messageDrivenBean = messageDrivenBeans[i];
            String messagingType = OpenEjbModuleBuilder.getJ2eeStringValue(messageDrivenBean.getMessagingType());
            if (messagingType != null) { // That is, not JMS
                try {
                    cl.loadClass(messagingType);
                } catch (ClassNotFoundException e) {
                    throw new DeploymentException("could not load messaging type: " + messagingType, e);
                }
            }
        }
    }

    private GBeanData createBean(EARContext earContext,
            EJBModule ejbModule,
            String containerId,
            MessageDrivenBeanType messageDrivenBean,
            OpenejbMessageDrivenBeanType openejbMessageDrivenBean,
            AbstractName activationSpecWrapperName,
            TransactionPolicyHelper transactionPolicyHelper,
            ClassLoader cl,
            ComponentPermissions componentPermissions,
            String policyContextID) throws DeploymentException {

        if (openejbMessageDrivenBean == null) {
            throw new DeploymentException("openejb-jar.xml required to deploy an mdb");
        }

        String ejbName = messageDrivenBean.getEjbName().getStringValue().trim();

        MdbBuilder builder = new MdbBuilder();
        builder.setContainerId(containerId);
        builder.setEjbName(ejbName);

        builder.setEndpointInterfaceName(getMessagingType(messageDrivenBean));
        builder.setBeanClassName(messageDrivenBean.getEjbClass().getStringValue().trim());

        builder.setActivationSpecName(activationSpecWrapperName);

        builder.setEjbContainerName(defaultMdbEjbContainer);

        SecurityConfiguration securityConfiguration = (SecurityConfiguration) earContext.getSecurityConfiguration();
        if (securityConfiguration != null) {
            Permissions toBeChecked = new Permissions();
            XmlBeansSecurityBuilder xmlBeansSecurityBuilder = new XmlBeansSecurityBuilder();
            String defaultRole = securityConfiguration.getDefaultRole();
            xmlBeansSecurityBuilder.addComponentPermissions(defaultRole,
                    toBeChecked,
                    ((EjbJarType) ejbModule.getSpecDD()).getAssemblyDescriptor(),
                    ejbName,
                    null, componentPermissions);

            xmlBeansSecurityBuilder.setDetails(messageDrivenBean.getSecurityIdentity(), securityConfiguration, policyContextID, builder);
        }

        boolean beanManagedTransactions = new Boolean("Bean".equals(messageDrivenBean.getTransactionType().getStringValue().trim())).booleanValue();
        builder.setBeanManagedTransactions(beanManagedTransactions);

        if (!beanManagedTransactions) {
            SortedMap transactionPolicies = transactionPolicyHelper.getTransactionPolicies(ejbName);
            builder.setTransactionPolicies(transactionPolicies);
        }

        processEnvironmentRefs(builder, earContext, ejbModule, messageDrivenBean, openejbMessageDrivenBean, cl);

        try {
            GBeanData gbean = builder.createConfiguration();
            gbean.setReferencePattern("ActivationSpecWrapper", activationSpecWrapperName);
            return gbean;
        } catch (Throwable e) {
            throw new DeploymentException("Unable to initialize EJBContainer GBean: ejbName" + ejbName, e);
        }
    }

    private String getMessagingType(MessageDrivenBeanType messageDrivenBean) {
        String messageInterfaceType;
        if (messageDrivenBean.isSetMessagingType()) {
            messageInterfaceType = messageDrivenBean.getMessagingType().getStringValue().trim();
        } else {
            messageInterfaceType = "javax.jms.MessageListener";
        }
        return messageInterfaceType;
    }

    private void addActivationSpecWrapperGBean(EARContext earContext,
            AbstractName activationSpecName,
            OpenejbActivationConfigPropertyType[] openejbActivationConfigProperties,
            ActivationConfigPropertyType[] activationConfigProperties,
            GerResourceLocatorType resourceAdapter,
            String messageListenerInterfaceName,
            String containerId,
            String destinationLink,
            String destinationType,
            String ejbName) throws DeploymentException {
        RefContext refContext = earContext.getRefContext();
        AbstractNameQuery resourceAdapterInstanceQuery = getResourceAdapterNameQuery(resourceAdapter, NameFactory.JCA_RESOURCE_ADAPTER);
        GBeanData activationSpecInfo = refContext.getActivationSpecInfo(resourceAdapterInstanceQuery, messageListenerInterfaceName, earContext.getConfiguration());

        if (activationSpecInfo == null) {
            throw new DeploymentException("no activation spec found for resource adapter: " + resourceAdapterInstanceQuery + " and message listener type: " + messageListenerInterfaceName);
        }
        activationSpecInfo = new GBeanData(activationSpecInfo);
        activationSpecInfo.setAttribute("containerId", containerId);
        activationSpecInfo.setReferencePattern("ResourceAdapterWrapper", resourceAdapterInstanceQuery);
        String activationSpecClass = (String) activationSpecInfo.getAttribute("activationSpecClass");
        Object testSpec;
        try {
            testSpec = earContext.getClassLoader().loadClass(activationSpecClass).newInstance();
        } catch (Exception e) {
            throw new DeploymentException("Unable to load JMS ActivationSpec class "+activationSpecClass+" for message-driven bean "+ejbName+" in "+containerId);
        }
        Map specValues = new HashMap();
        // 1. Lowest Priority.  Set any properties we can from the generic elements on the message-driven-bean (mostly provider-specific stuff)
        if(activationSpecClass.equals("org.activemq.ra.ActiveMQActivationSpec")) {
            if(destinationLink != null) {
                String physicalName = getActiveMQPhysicalNameForLink(destinationLink, refContext, earContext.getConfiguration(), ejbName);
                if(physicalName != null) {
                    specValues.put("destination", physicalName);
                }
            }
        }
        if(destinationType != null) {
            specValues.put("destinationType", destinationType);
        }
        // 2. Medium Priority.  Set any properties explicitly included in ejb-jar.xml
        for (int i = 0; i < activationConfigProperties.length; i++) {
            ActivationConfigPropertyType activationConfigProperty = activationConfigProperties[i];
            String propertyName = activationConfigProperty.getActivationConfigPropertyName().getStringValue().trim();
            String propertyValue = activationConfigProperty.getActivationConfigPropertyValue().isNil() ? null : activationConfigProperty.getActivationConfigPropertyValue().getStringValue().trim();
            specValues.put(propertyName, propertyValue);
        }
        // 3. Highest Priority.  Set any properties configured in openejb-jar.xml
        if (openejbActivationConfigProperties != null) {
            for (int i = 0; i < openejbActivationConfigProperties.length; i++) {
                OpenejbActivationConfigPropertyType activationConfigProperty = openejbActivationConfigProperties[i];
                String propertyName = activationConfigProperty.getActivationConfigPropertyName();
                if (propertyName != null) {
                    propertyName = propertyName.trim();
                }
                String propertyValue = activationConfigProperty.getActivationConfigPropertyValue();
                if (propertyValue != null) {
                    propertyValue = propertyValue.trim();
                }
                String name = Introspector.decapitalize(propertyName);
                specValues.put(name, propertyValue);
            }
        }
        // 4. Apply Settings
        for (Iterator it = specValues.keySet().iterator(); it.hasNext();) {
            String name = (String) it.next();
            String value = (String) specValues.get(name);
            if(activationSpecInfo.getGBeanInfo().getAttribute(name) != null) {
                try {
                    activationSpecInfo.setAttribute(name, value);
                    testSpec.getClass().getMethod("set"+Character.toUpperCase(name.charAt(0))+name.substring(1), new Class[]{String.class}).invoke(testSpec, new Object[]{value});
                } catch (Exception e) {
                    throw new DeploymentException("Could not set property: " + name + " to value: " + value + " on activationSpec: " + activationSpecClass + " for message-driven bean "+ejbName, e);
                }
            } else {
                throw new DeploymentException("Invalid activation-config-property; JMS ActivationSpec "+activationSpecClass+" does not have property '"+name+"' for message-driven bean "+ejbName+" in "+containerId);
            }
        }
        // 5. Validate
        try {
            testSpec.getClass().getMethod("validate", new Class[0]).invoke(testSpec, new Object[0]);
        } catch (InvocationTargetException e) {
            Throwable chained = e.getCause();
            if(chained.getClass().getName().equals("javax.resource.spi.InvalidPropertyException")) {
                throw new DeploymentException("JMS settings for message-driven bean "+ejbName+" are not valid: "+chained.getMessage(), chained);
//                throw new DeploymentException((e.getInvalidPropertyDescriptors().length == 0 ? "" : e.getInvalidPropertyDescriptors().length+" ") +
//                        "JMS settings for message-driven bean "+ejbName+" are not valid: "+e.getMessage(), e);
            } else if(chained instanceof UnsupportedOperationException) {
                log.warn("JMS ActivationSpec for message-driven bean "+ejbName+" does not support validation.  Unable to tell whether settings for MDB are correct during deployment.  It may die at runtime, sorry.");
            } else {
                throw new DeploymentException("Unexpected exception while validation JMS settings on "+ejbName, e);
            }
        } catch (Exception e) {
            throw new DeploymentException("Unexpected exception while validation JMS settings on "+ejbName, e);
        }

        activationSpecInfo.setAbstractName(activationSpecName);
        try {
            earContext.addGBean(activationSpecInfo);
        } catch (GBeanAlreadyExistsException e) {
            throw new DeploymentException("Could not add activation spec gbean to context", e);
        }
    }

    private String getActiveMQPhysicalNameForLink(String link, RefContext context, Configuration earConfig, String ejbName) throws DeploymentException {
        GerMessageDestinationType destination = (GerMessageDestinationType) context.getMessageDestination(link);
        String linkName = link;
        String moduleURI = null;
        if (destination != null) {
            if (destination.isSetAdminObjectLink()) {
                if (destination.isSetAdminObjectModule()) {
                    moduleURI = destination.getAdminObjectModule().trim();
                }
                linkName = destination.getAdminObjectLink().trim();
            }
        } else {
            //well, we know for sure an admin object is not going to be defined in a modules that can have a message-destination
            int pos = linkName.indexOf('#');
            if (pos > -1) {
                //AMM -- see comment in ENCConfigBuilder.addMessageDestinationRefs
                //moduleURI = linkName.substring(0, pos);
                linkName = linkName.substring(pos + 1);
            }
        }
        AbstractNameQuery adminObjectQuery = ENCConfigBuilder.buildAbstractNameQuery(null, moduleURI, linkName, NameFactory.JCA_ADMIN_OBJECT, NameFactory.RESOURCE_ADAPTER_MODULE);
        try {
            AbstractName adminObjectName;
            try {
                adminObjectName = earConfig.findGBean(adminObjectQuery);
            } catch (GBeanNotFoundException e) {
                // There is no matching admin object, so this must be a destination that's created on the fly
                log.debug("MDB "+ejbName+" uses message-destination-link "+linkName+" which I can't find an AdminObject for, so I assume a destination named "+linkName+" will be created automatically");
                return link;
            }
            String physical = null;
            try { // See if the admin object is in the same EAR (or at least dependency tree?)
                GBeanData data = earConfig.findGBeanData(new AbstractNameQuery(adminObjectName));
                physical = (String) data.getAttribute("PhysicalName");
            } catch (GBeanNotFoundException e) { // If not, try the server
                try {
                    physical = (String) kernel.getAttribute(adminObjectName, "PhysicalName");
                } catch (GBeanNotFoundException e2) {
                    // There is an admin object, but it's not running so we can't get its physical name
                    // In this case, the user must set it as an activation config parameter
                    log.warn("Unable to identify physical destination name for JMS destination "+linkName+" for " +
                            "message-driven bean "+ejbName+".  This is not expected, but not necessarily a " +
                            "disaster either.  If you get a deployment error after this, it means you must " +
                            "configure the destination in the activation-config section for this MDB in " +
                            "openejb-jar.xml.");
                }
            }
            if(physical != null) {
                log.debug("MDB "+ejbName+" uses message-destination-link "+linkName+" which I tracked down to the physical destination name "+physical);
                return physical;
            }
        } catch (Exception e) {
            log.error("Error while looking up physical destination name for destination "+linkName+
                    " for message-driven bean "+ejbName, e);
        }
        return null;
    }

    private static AbstractNameQuery getResourceAdapterNameQuery(GerResourceLocatorType resourceLocator, String type) {
        if (resourceLocator.isSetResourceLink()) {
            return ENCConfigBuilder.buildAbstractNameQuery(null, null, resourceLocator.getResourceLink().trim(), type, NameFactory.RESOURCE_ADAPTER_MODULE);
        }
        //construct name from components
        return ENCConfigBuilder.buildAbstractNameQuery(resourceLocator.getPattern(), type, NameFactory.RESOURCE_ADAPTER_MODULE, null);
    }

    protected void processEnvironmentRefs(MdbBuilder builder, EARContext earContext, EJBModule ejbModule, MessageDrivenBeanType messageDrivenBean, OpenejbMessageDrivenBeanType openejbMessageDrivenBean, ClassLoader cl) throws DeploymentException {
        // env entries
        EnvEntryType[] envEntries = messageDrivenBean.getEnvEntryArray();

        // ejb refs
        EjbRefType[] ejbRefs = messageDrivenBean.getEjbRefArray();
        GerEjbRefType[] openejbEjbRefs = null;

        EjbLocalRefType[] ejbLocalRefs = messageDrivenBean.getEjbLocalRefArray();
        GerEjbLocalRefType[] openejbEjbLocalRefs = null;

        // resource refs
        ResourceRefType[] resourceRefs = messageDrivenBean.getResourceRefArray();
        GerResourceRefType[] openejbResourceRefs = null;

        // resource env refs
        ResourceEnvRefType[] resourceEnvRefs = messageDrivenBean.getResourceEnvRefArray();
        GerResourceEnvRefType[] openejbResourceEnvRefs = null;

        ServiceRefType[] serviceRefs = messageDrivenBean.getServiceRefArray();
        GerServiceRefType[] openejbServiceRefs = null;

        GerGbeanRefType[] gBeanRefs = null;

        //get arrays from openejb plan if present
        if (openejbMessageDrivenBean != null) {
            openejbEjbRefs = openejbMessageDrivenBean.getEjbRefArray();
            openejbEjbLocalRefs = openejbMessageDrivenBean.getEjbLocalRefArray();
            openejbResourceRefs = openejbMessageDrivenBean.getResourceRefArray();
            openejbResourceEnvRefs = openejbMessageDrivenBean.getResourceEnvRefArray();
            openejbServiceRefs = openejbMessageDrivenBean.getServiceRefArray();
            gBeanRefs = openejbMessageDrivenBean.getGbeanRefArray();
        }

        MessageDestinationRefType[] messageDestinationRefs = messageDrivenBean.getMessageDestinationRefArray();

        Map context = ENCConfigBuilder.buildComponentContext(earContext,
                null,
                ejbModule,
                null,
                envEntries,
                ejbRefs, openejbEjbRefs,
                ejbLocalRefs, openejbEjbLocalRefs,
                resourceRefs, openejbResourceRefs,
                resourceEnvRefs, openejbResourceEnvRefs,
                messageDestinationRefs,
                serviceRefs, openejbServiceRefs,
                gBeanRefs,
                cl);
        builder.setComponentContext(context);
        ENCConfigBuilder.setResourceEnvironment(builder, resourceRefs, openejbResourceRefs);
    }
}