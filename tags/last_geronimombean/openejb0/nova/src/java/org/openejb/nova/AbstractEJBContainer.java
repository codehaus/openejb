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
package org.openejb.nova;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;

import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.transaction.TransactionManager;
import javax.security.auth.Subject;

import org.apache.geronimo.cache.InstancePool;
import org.apache.geronimo.connector.outbound.connectiontracking.TrackedConnectionAssociator;
import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.ejb.metadata.TransactionDemarcation;
import org.apache.geronimo.kernel.service.GeronimoMBeanContext;
import org.apache.geronimo.kernel.service.GeronimoMBeanTarget;
import org.apache.geronimo.naming.java.ReadOnlyContext;
import org.apache.geronimo.remoting.DeMarshalingInterceptor;
import org.apache.geronimo.remoting.InterceptorRegistry;
import org.openejb.nova.dispatch.MethodHelper;
import org.openejb.nova.dispatch.MethodSignature;
import org.openejb.nova.dispatch.VirtualOperation;
import org.openejb.nova.transaction.EJBUserTransaction;
import org.openejb.nova.transaction.TxnPolicy;
import org.openejb.nova.transaction.ContainerPolicy;
import org.openejb.nova.deployment.TransactionPolicySource;

/**
 *
 *
 * @version $Revision$ $Date$
 */
public abstract class AbstractEJBContainer
        implements EJBContainer, GeronimoMBeanTarget {

    protected final URI uri;
    protected final String ejbName;
    protected final String ejbClassName;
    protected final String homeClassName;
    protected final String localHomeClassName;
    protected final String remoteClassName;
    protected final String localClassName;
    protected final String messageEndpointClassName;
    protected final TransactionDemarcation txnDemarcation;
    protected TransactionManager txnManager;         //not final until Endpoints can be Constructor args.
    protected TrackedConnectionAssociator trackedConnectionAssociator; //not final until Endpoints can be Constructor args.
    protected final ReadOnlyContext componentContext;
    protected final EJBUserTransaction userTransaction;
    protected final Set unshareableResources;
    protected final TransactionPolicySource transactionPolicySource;
    protected final String contextId;
    protected final Subject runAs;
    protected final boolean setSecurityInterceptor;
    protected final boolean setPolicyContextHandlerDataEJB;
    protected final boolean setIdentity;


    protected ClassLoader classLoader;
    protected Class beanClass;
    protected VirtualOperation[] vtable;

    protected EJBRemoteClientContainer remoteClientContainer;
    protected Class homeInterface;
    protected Class remoteInterface;

    protected EJBLocalClientContainer localClientContainer;
    protected Class localHomeInterface;
    protected Class localInterface;

    protected Class messageEndpointInterface;

    protected InstancePool pool;
    private Long remoteId;
    protected TxnPolicy[][] transactionPolicy = new TxnPolicy[EJBInvocationType.getMaxTransactionPolicyKey() + 1][];

    public AbstractEJBContainer(EJBContainerConfiguration config) {
        uri = config.uri;
        ejbName = config.ejbName;
        ejbClassName = config.beanClassName;
        homeClassName = config.homeInterfaceName;
        remoteClassName = config.remoteInterfaceName;
        localHomeClassName = config.localHomeInterfaceName;
        localClassName = config.localInterfaceName;
        messageEndpointClassName = config.messageEndpointInterfaceName;
        txnDemarcation = config.txnDemarcation;
        txnManager = config.txnManager;
        userTransaction = config.userTransaction;
        componentContext = config.componentContext;
        trackedConnectionAssociator = config.trackedConnectionAssociator;
        unshareableResources = config.unshareableResources;
        transactionPolicySource = config.transactionPolicySource;
        contextId = config.contextId;
        runAs = config.runAs;
        setSecurityInterceptor = config.setSecurityInterceptor;
        setPolicyContextHandlerDataEJB = config.setPolicyContextHandlerDataEJB;
        setIdentity = config.setIdentity;
    }

    public void setTransactionManager(TransactionManager txnManager) {
        this.txnManager = txnManager;
    }

    public void setTrackedConnectionAssociator(TrackedConnectionAssociator trackedConnectionAssociator) {
        this.trackedConnectionAssociator = trackedConnectionAssociator;
    }

    public void setMBeanContext(GeronimoMBeanContext context) {
    }

    public boolean canStart() {
        return true;
    }

    /* Start the Component
     * @see org.apache.geronimo.core.service.AbstractManagedObject#doStart()
     */
    public void doStart() {
        //super.doStart();
        classLoader = Thread.currentThread().getContextClassLoader();
        System.out.println("classloader="+classLoader);
        try {
            if (userTransaction != null) {
                userTransaction.setUp(txnManager, trackedConnectionAssociator);
            }
            beanClass = classLoader.loadClass(ejbClassName);

            if (homeClassName != null) {
                homeInterface = classLoader.loadClass(homeClassName);
                remoteInterface = classLoader.loadClass(remoteClassName);
            } else {
                homeInterface = null;
                remoteInterface = null;
            }
            if (localHomeClassName != null) {
                localHomeInterface = classLoader.loadClass(localHomeClassName);
                localInterface = classLoader.loadClass(localClassName);
            } else {
                localHomeInterface = null;
                localInterface = null;
            }
            if (messageEndpointClassName != null) {
                messageEndpointInterface = classLoader.loadClass(messageEndpointClassName);
            }
        } catch (ClassNotFoundException e) {
            throw new AssertionError(e);
        }
    }

    public boolean canStop() {
        return true;
    }

    /* Stop the Component
     * @see org.apache.geronimo.core.service.AbstractManagedObject#doStop()
     */
    public void doStop() {
        homeInterface = null;
        remoteInterface = null;
        localHomeInterface = null;
        localInterface = null;
        messageEndpointInterface = null;
        beanClass = null;
        if (userTransaction != null) {
            userTransaction.setUp(null, trackedConnectionAssociator);
        }
        //super.doStop();
    }

    public void doFail() {
    }

    public String getEJBName() {
        return ejbName;
    }
    
    public Class getBeanClass() {
        return beanClass;
    }

    public Class getHomeInterface() {
        return homeInterface;
    }

    public Class getRemoteInterface() {
        return remoteInterface;
    }

    public Class getLocalHomeInterface() {
        return localHomeInterface;
    }

    public Class getLocalInterface() {
        return localInterface;
    }

    public EJBHome getEJBHome() {
        return remoteClientContainer.getEJBHome();
    }

    public EJBObject getEJBObject(Object primaryKey) {
        return remoteClientContainer.getEJBObject(primaryKey);
    }

    public EJBLocalHome getEJBLocalHome() {
        return localClientContainer.getEJBLocalHome();
    }

    public EJBLocalObject getEJBLocalObject(Object primaryKey) {
        return localClientContainer.getEJBLocalObject(primaryKey);
    }

    public TransactionDemarcation getDemarcation() {
        return txnDemarcation;
    }

    public EJBUserTransaction getUserTransaction() {
        return userTransaction;
    }

    public ReadOnlyContext getComponentContext() {
        return componentContext;
    }

    /**
     * Return the name of this EJB's implementation class
     * @return the name of this EJB's implementation class
     */
    public String getBeanClassName() {
        return ejbClassName;
    }

    /**
     * Return the name of this EJB's home interface class
     * @return the name of this EJB's home interface class
     */
    public String getHomeClassName() {
        return homeClassName;
    }

    /**
     * Return the name of this EJB's remote component interface class
     * @return the name of this EJB's remote component interface class
     */
    public String getRemoteClassName() {
        return remoteClassName;
    }

    /**
     * Return the name of this EJB's local home class
     * @return the name of this EJB's local home class
     */
    public String getLocalHomeClassName() {
        return localHomeClassName;
    }

    /**
     * Return the name of this EJB's local component interface class
     * @return the name of this EJB's local component interface class
     */
    public String getLocalClassName() {
        return localClassName;
    }

    public String getMessageEndpointClassName() {
        return messageEndpointClassName;
    }

    protected URI startServerRemoting(Interceptor firstInterceptor) {
        // set up server side remoting endpoint
        DeMarshalingInterceptor demarshaller = new DeMarshalingInterceptor(firstInterceptor, classLoader);
        remoteId = InterceptorRegistry.instance.register(demarshaller);
        return uri.resolve("#" + remoteId);
    }

    protected void stopServerRemoting() {
        InterceptorRegistry.instance.unregister(remoteId);
    }

    protected void buildTransactionPolicyMap(MethodSignature[] signatures) {
        if (homeInterface != null) {
            TxnPolicy[] remotePolicies = new TxnPolicy[signatures.length];
            Map homeMethodMap = MethodHelper.getHomeMethodMap(signatures, homeInterface);
            mapPolicies("Home", homeMethodMap, remotePolicies);
            Map remoteMethodMap = MethodHelper.getObjectMethodMap(signatures, remoteInterface);
            mapPolicies("Remote", remoteMethodMap, remotePolicies);
            transactionPolicy[EJBInvocationType.REMOTE.getTransactionPolicyKey()] = remotePolicies;
        }
        if (localHomeInterface != null) {
            TxnPolicy[] localPolicies = new TxnPolicy[signatures.length];
            Map localHomeMethodMap = MethodHelper.getHomeMethodMap(signatures, localHomeInterface);
            mapPolicies("LocalHome", localHomeMethodMap, localPolicies);
            Map localMethodMap = MethodHelper.getObjectMethodMap(signatures, localInterface);
            mapPolicies("Local", localMethodMap, localPolicies);
            transactionPolicy[EJBInvocationType.LOCAL.getTransactionPolicyKey()] = localPolicies;
        }
    }

    protected void buildMDBTransactionPolicyMap(MethodSignature[] signatures) {
        TxnPolicy[] localPolicies = new TxnPolicy[signatures.length];
        Map localMethodMap = MethodHelper.getObjectMethodMap(signatures, messageEndpointInterface);
        mapPolicies("Local", localMethodMap, localPolicies);
        transactionPolicy[EJBInvocationType.LOCAL.getTransactionPolicyKey()] = localPolicies;
        transactionPolicy[EJBInvocationType.MESSAGE_ENDPOINT.getTransactionPolicyKey()] =
                new TxnPolicy[] {ContainerPolicy.BeforeDelivery, ContainerPolicy.AfterDelivery};
    }

    //TODO can the method map start out with MethodSignatures instead of methods?
    private void mapPolicies(String intfName, Map methodMap, TxnPolicy[] policies) {
        for (Iterator iterator = methodMap.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            MethodSignature signature = new MethodSignature((Method)entry.getKey());
            Integer index = (Integer)entry.getValue();
            TxnPolicy policy = transactionPolicySource.getTransactionPolicy(intfName, signature);
            policies[index.intValue()] = policy;
        }
    }
}

