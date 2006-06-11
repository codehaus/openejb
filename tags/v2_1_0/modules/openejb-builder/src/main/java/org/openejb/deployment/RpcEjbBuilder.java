/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact info@openejb.org.
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
 * Copyright 2005 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.deployment;

import java.util.Set;
import java.util.SortedMap;
import java.util.Map;
import javax.management.ObjectName;
import javax.security.auth.Subject;

import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.naming.deployment.ResourceEnvironmentBuilder;
import org.apache.geronimo.security.deploy.DefaultPrincipal;

/**
 * @version $Revision$ $Date$
 */
public abstract class RpcEjbBuilder implements ResourceEnvironmentBuilder, SecureBuilder {
    protected ObjectName containerId;
    private String ejbName;
    protected String homeInterfaceName;
    protected String remoteInterfaceName;
    protected String localHomeInterfaceName;
    protected String localInterfaceName;
    private String beanClassName;
    private ObjectName ejbContainerName;
    private String[] jndiNames;
    private String[] localJndiNames;
    private String policyContextId;
    private DefaultPrincipal defaultPrincipal;
    private Subject runAs;
    private SortedMap transactionPolicies;
    private Map componentContext;
    private ObjectName tssBeanName;
    private Set unshareableResources;
    private Set applicationManagedSecurityResources;

    public void setContainerId(ObjectName containerId) {
        this.containerId = containerId;
    }

    public void setEjbName(String ejbName) {
        this.ejbName = ejbName;
    }

    public void setBeanClassName(String beanClassName) {
        this.beanClassName = beanClassName;
    }

    public String getHomeInterfaceName() {
        return homeInterfaceName;
    }

    public void setHomeInterfaceName(String homeInterfaceName) {
        this.homeInterfaceName = homeInterfaceName;
    }

    public String getRemoteInterfaceName() {
        return remoteInterfaceName;
    }

    public void setRemoteInterfaceName(String remoteInterfaceName) {
        this.remoteInterfaceName = remoteInterfaceName;
    }

    public String getLocalHomeInterfaceName() {
        return localHomeInterfaceName;
    }

    public void setLocalHomeInterfaceName(String localHomeInterfaceName) {
        this.localHomeInterfaceName = localHomeInterfaceName;
    }

    public String getLocalInterfaceName() {
        return localInterfaceName;
    }

    public void setLocalInterfaceName(String localInterfaceName) {
        this.localInterfaceName = localInterfaceName;
    }

    public void setEjbContainerName(ObjectName ejbContainerName) {
        this.ejbContainerName = ejbContainerName;
    }

    public void setJndiNames(String[] jndiNames) {
        this.jndiNames = jndiNames;
    }

    public void setLocalJndiNames(String[] localJndiNames) {
        this.localJndiNames = localJndiNames;
    }

    public String getPolicyContextId() {
        return policyContextId;
    }

    public void setPolicyContextId(String policyContextId) {
        this.policyContextId = policyContextId;
    }

    public boolean isSecurityEnabled() {
        throw new UnsupportedOperationException();
    }

    public void setSecurityEnabled(boolean securityEnabled) {
    }

    public boolean isDoAsCurrentCaller() {
        throw new UnsupportedOperationException();

    }

    public void setDoAsCurrentCaller(boolean doAsCurrentCaller) {
    }

    public boolean isUseContextHandler() {
        throw new UnsupportedOperationException();
    }

    public void setUseContextHandler(boolean useContextHandler) {
        throw new UnsupportedOperationException();
    }

    public DefaultPrincipal getDefaultPrincipal() {
        return defaultPrincipal;
    }

    public void setDefaultPrincipal(DefaultPrincipal defaultPrincipal) {
        this.defaultPrincipal = defaultPrincipal;
    }

    public Subject getRunAs() {
        return runAs;
    }

    public void setRunAs(Subject runAs) {
        this.runAs = runAs;
    }

    public void setTssBeanName(ObjectName tssBeanName) {
        this.tssBeanName = tssBeanName;
    }

    public void setComponentContext(Map componentContext) {
        this.componentContext = componentContext;
    }

    public void setTransactionPolicies(SortedMap transactionPolicies) {
        this.transactionPolicies = transactionPolicies;
    }

    public Set getUnshareableResources() {
        return unshareableResources;
    }

    public void setUnshareableResources(Set unshareableResources) {
        this.unshareableResources = unshareableResources;
    }

    public Set getApplicationManagedSecurityResources() {
        return applicationManagedSecurityResources;
    }

    public void setApplicationManagedSecurityResources(Set applicationManagedSecurityResources) {
        this.applicationManagedSecurityResources = applicationManagedSecurityResources;
    }

    public GBeanData createConfiguration() throws Exception {
        GBeanData gbean = new GBeanData(containerId, getTargetGBeanInfo());

        gbean.setAttribute("ejbName", ejbName);

        gbean.setAttribute("homeInterfaceName", homeInterfaceName);
        gbean.setAttribute("remoteInterfaceName", remoteInterfaceName);
        gbean.setAttribute("localHomeInterfaceName", localHomeInterfaceName);
        gbean.setAttribute("localInterfaceName", localInterfaceName);
        gbean.setAttribute("beanClassName", beanClassName);

        gbean.setAttribute("jndiNames", jndiNames);
        gbean.setAttribute("localJndiNames", localJndiNames);

        gbean.setReferencePattern("ejbContainer", ejbContainerName);

        gbean.setAttribute("policyContextId", policyContextId);
        gbean.setAttribute("defaultPrincipal", defaultPrincipal);
        gbean.setAttribute("runAs", runAs);

        gbean.setAttribute("transactionPolicies", transactionPolicies);

        gbean.setAttribute("componentContextMap", componentContext);

        if (tssBeanName != null) {
            gbean.setReferencePattern("TSSBean", tssBeanName);
        }

        gbean.setAttribute("unshareableResources", unshareableResources);
        gbean.setAttribute("applicationManagedSecurityResources", applicationManagedSecurityResources);

        return gbean;
    }

    protected abstract GBeanInfo getTargetGBeanInfo();
}
