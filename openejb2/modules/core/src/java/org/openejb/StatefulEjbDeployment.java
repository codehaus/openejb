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
package org.openejb;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import javax.security.auth.Subject;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.management.J2EEManagedObject;
import org.apache.geronimo.security.deploy.DefaultPrincipal;
import org.apache.geronimo.timer.PersistenceException;
import org.openejb.cache.InstanceCache;
import org.openejb.cache.SimpleInstanceCache;
import org.openejb.cluster.server.ClusteredEjbDeployment;
import org.openejb.cluster.server.ClusteredInstanceCache;
import org.openejb.cluster.server.ClusteredInstanceContextFactory;
import org.openejb.cluster.server.DefaultClusteredEjbDeployment;
import org.openejb.cluster.server.DefaultClusteredInstanceCache;
import org.openejb.cluster.server.EJBClusterManager;
import org.openejb.cluster.sfsb.ClusteredSFInstanceContextFactory;
import org.openejb.corba.TSSBean;
import org.openejb.dispatch.InterfaceMethodSignature;
import org.openejb.dispatch.MethodSignature;
import org.openejb.dispatch.VirtualOperation;
import org.openejb.proxy.ProxyInfo;
import org.openejb.sfsb.BusinessMethod;
import org.openejb.sfsb.CreateMethod;
import org.openejb.sfsb.RemoveMethod;
import org.openejb.sfsb.StatefulInstanceContextFactory;
import org.openejb.sfsb.StatefulInstanceFactory;


/**
 * @version $Revision$ $Date$
 */
public class StatefulEjbDeployment extends AbstractRpcDeployment implements ExtendedEjbDeployment, J2EEManagedObject {
    private final StatefulInstanceFactory instanceFactory;
    private final InstanceCache instanceCache;
    private final EJBClusterManager clusterManager;
    private final ClusteredEjbDeployment clusteredEJBContainer;
    private final MethodMap dispatchMethodMap;

    public StatefulEjbDeployment(String containerId,
            String ejbName,

            String homeInterfaceName,
            String remoteInterfaceName,
            String localHomeInterfaceName,
            String localInterfaceName,
            String beanClassName,
            ClassLoader classLoader,

            StatefulEjbContainer ejbContainer,

            String[] jndiNames,
            String[] localJndiNames,

            String policyContextId,
            DefaultPrincipal defaultPrincipal,
            Subject runAs,

            boolean beanManagedTransactions,
            SortedMap transactionPolicies,

            Map componentContext,

            Kernel kernel,

            TSSBean tssBean,

            // connector stuff
            Set unshareableResources,
            Set applicationManagedSecurityResources,

            // clustering stuff
            EJBClusterManager clusterManager) throws Exception {

        this(containerId,
                ejbName,


                loadClass(homeInterfaceName, classLoader, "home interface"),
                loadClass(remoteInterfaceName, classLoader, "remote interface"),
                loadClass(localHomeInterfaceName, classLoader, "local home interface"),
                loadClass(localInterfaceName, classLoader, "local interface"),
                loadClass(beanClassName, classLoader, "bean class"),
                classLoader,

                ejbContainer,

                jndiNames,
                localJndiNames,

                policyContextId,
                defaultPrincipal,
                runAs,

                beanManagedTransactions,
                transactionPolicies,

                componentContext,

                kernel,

                tssBean,

                unshareableResources,
                applicationManagedSecurityResources,

                clusterManager);
    }

    public StatefulEjbDeployment(String containerId,
            String ejbName,

            Class homeInterface,
            Class remoteInterface,
            Class localHomeInterface,
            Class localInterface,
            Class beanClass,
            ClassLoader classLoader,

            StatefulEjbContainer ejbContainer,

            String[] jndiNames,
            String[] localJndiNames,

            String policyContextId,
            DefaultPrincipal defaultPrincipal,
            Subject runAs,

            boolean beanManagedTransactions,
            SortedMap transactionPolicies,

            Map componentContext,

            Kernel kernel,

            TSSBean tssBean,

            // connector stuff
            Set unshareableResources,
            Set applicationManagedSecurityResources,

            // clustering stuff
            EJBClusterManager clusterManager) throws Exception {

        super(containerId,
                ejbName,

                new ProxyInfo(EJBComponentType.STATEFUL,
                        containerId,
                        homeInterface,
                        remoteInterface,
                        localHomeInterface,
                        localInterface,
                        null,
                        null),
                beanClass,
                classLoader,

                new RpcSignatureIndexBuilder(beanClass,
                        homeInterface,
                        remoteInterface,
                        localHomeInterface,
                        localInterface,
                        null),

                ejbContainer,

                jndiNames,
                localJndiNames,

                policyContextId,
                defaultPrincipal,
                runAs,

                beanManagedTransactions,
                transactionPolicies,

                componentContext,

                kernel,

                tssBean);

        dispatchMethodMap = buildDispatchMethodMap();

        this.clusterManager = clusterManager;

        // build the instance factory
        StatefulInstanceContextFactory contextFactory;
        if (clusterManager == null) {
            contextFactory = new StatefulInstanceContextFactory(this,
                    ejbContainer,
                    proxyFactory,
                    unshareableResources,
                    applicationManagedSecurityResources);
        } else {
            contextFactory = new ClusteredSFInstanceContextFactory(this,
                    ejbContainer,
                    proxyFactory,
                    unshareableResources,
                    applicationManagedSecurityResources);
        }

        instanceFactory = new StatefulInstanceFactory(contextFactory);

        // build the cache
        InstanceCache instanceCache = new SimpleInstanceCache();
        if (clusterManager != null) {
            instanceCache = new DefaultClusteredInstanceCache(instanceCache);
        }
        this.instanceCache = instanceCache;

        ClusteredEjbDeployment clusteredEJBContainer = null;
        if (clusterManager != null) {
            clusteredEJBContainer = new DefaultClusteredEjbDeployment(this,
                    (ClusteredInstanceCache) instanceCache,
                    (ClusteredInstanceContextFactory) contextFactory);
        }
        this.clusteredEJBContainer = clusteredEJBContainer;
    }

    public VirtualOperation getVirtualOperation(int methodIndex) {
        VirtualOperation vop = (VirtualOperation) dispatchMethodMap.get(methodIndex);
        return vop;
    }

    private MethodMap buildDispatchMethodMap() throws Exception {
        Class beanClass = getBeanClass();

        MethodMap dispatchMethodMap = new MethodMap(signatures);

        MethodSignature removeSignature = new MethodSignature("ejbRemove");
        RemoveMethod removeVop = new RemoveMethod(beanClass, removeSignature);
        for (Iterator iterator = dispatchMethodMap.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            InterfaceMethodSignature methodSignature = (InterfaceMethodSignature) entry.getKey();
            String methodName = methodSignature.getMethodName();
            if (methodSignature.isHomeMethod()) {
                if (methodName.startsWith("create")) {
                    String baseName = methodName.substring(6);
                    MethodSignature createSignature = new MethodSignature("ejbCreate" + baseName, methodSignature.getParameterTypes());
                    entry.setValue(new CreateMethod(beanClass, createSignature));
                } else if (methodName.startsWith("remove")) {
                    entry.setValue(removeVop);
                }
            } else {
                if (methodName.startsWith("remove") && methodSignature.getParameterTypes().length == 0) {
                    entry.setValue(removeVop);
                } else if (!methodName.startsWith("ejb") &&
                        !methodName.equals("setSessionContext") &&
                        !methodName.equals("afterBegin") &&
                        !methodName.equals("beforeCompletion") &&
                        !methodName.equals("afterCompletion")) {
                    MethodSignature signature = new MethodSignature(methodName, methodSignature.getParameterTypes());
                    entry.setValue(new BusinessMethod(beanClass, signature));
                }
            }
        }
        return dispatchMethodMap;

    }

    public boolean isBeanManagedTransactions() {
        return beanManagedTransactions;
    }

    public StatefulInstanceFactory getInstanceFactory() {
        return instanceFactory;
    }

    public InstanceCache getInstanceCache() {
        return instanceCache;
    }

    public void doStart() throws Exception {
        super.doStart();

        if (clusterManager != null) {
            clusterManager.addEJBContainer(clusteredEJBContainer);
        }
    }

    protected void destroy() throws PersistenceException {
        if (clusterManager != null) {
            clusterManager.removeEJBContainer(clusteredEJBContainer);
        }

        super.destroy();
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(StatefulEjbDeployment.class, NameFactory.STATEFUL_SESSION_BEAN);

        infoFactory.addAttribute("objectName", String.class, false);
        infoFactory.addAttribute("ejbName", String.class, true);

        infoFactory.addAttribute("homeInterfaceName", String.class, true);
        infoFactory.addAttribute("remoteInterfaceName", String.class, true);
        infoFactory.addAttribute("localHomeInterfaceName", String.class, true);
        infoFactory.addAttribute("localInterfaceName", String.class, true);
        infoFactory.addAttribute("beanClassName", String.class, true);
        infoFactory.addAttribute("classLoader", ClassLoader.class, false);

        infoFactory.addReference("ejbContainer", StatefulEjbContainer.class, "StatefulEjbContainer");

        infoFactory.addAttribute("jndiNames", String[].class, true);
        infoFactory.addAttribute("localJndiNames", String[].class, true);

        infoFactory.addAttribute("policyContextId", String.class, true);
        infoFactory.addAttribute("defaultPrincipal", DefaultPrincipal.class, true);
        infoFactory.addAttribute("runAs", Subject.class, true);

        infoFactory.addAttribute("beanManagedTransactions", boolean.class, true);
        infoFactory.addAttribute("transactionPolicies", SortedMap.class, true);

        infoFactory.addAttribute("componentContextMap", Map.class, true);

        infoFactory.addAttribute("kernel", Kernel.class, false);

        infoFactory.addReference("TSSBean", TSSBean.class);

        infoFactory.addAttribute("unshareableResources", Set.class, true);
        infoFactory.addAttribute("applicationManagedSecurityResources", Set.class, true);

        infoFactory.addReference("EJBClusterManager", EJBClusterManager.class);

        infoFactory.setConstructor(new String[]{
                "objectName",
                "ejbName",

                "homeInterfaceName",
                "remoteInterfaceName",
                "localHomeInterfaceName",
                "localInterfaceName",
                "beanClassName",
                "classLoader",

                "ejbContainer",

                "jndiNames",
                "localJndiNames",

                "policyContextId",
                "defaultPrincipal",
                "runAs",

                "beanManagedTransactions",
                "transactionPolicies",

                "componentContextMap",

                "kernel",

                "TSSBean",

                "unshareableResources",
                "applicationManagedSecurityResources",

                "EJBClusterManager",
        });

        infoFactory.addInterface(StatefulEjbDeployment.class);

        GBEAN_INFO = infoFactory.getBeanInfo();
    }


    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
