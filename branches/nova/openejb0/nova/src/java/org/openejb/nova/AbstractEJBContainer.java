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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.security.auth.Subject;
import javax.transaction.TransactionManager;

import org.apache.geronimo.connector.outbound.connectiontracking.TrackedConnectionAssociator;
import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.ejb.metadata.TransactionDemarcation;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBean;
import org.apache.geronimo.gbean.GBeanContext;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GConstructorInfo;
import org.apache.geronimo.gbean.GOperationInfo;
import org.apache.geronimo.gbean.GReferenceInfo;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.naming.java.ReadOnlyContext;
import org.apache.geronimo.remoting.DeMarshalingInterceptor;
import org.apache.geronimo.remoting.InterceptorRegistry;

import org.openejb.nova.deployment.TransactionPolicySource;
import org.openejb.nova.dispatch.MethodHelper;
import org.openejb.nova.dispatch.MethodSignature;
import org.openejb.nova.security.SubjectIdExtractInterceptor;
import org.openejb.nova.transaction.EJBUserTransaction;
import org.openejb.nova.transaction.TxnPolicy;

/**
 * @version $Revision$ $Date$
 */
public abstract class AbstractEJBContainer implements EJBContainer, GBean {

    protected final URI uri;
    protected final String ejbName;

    protected final TransactionDemarcation txnDemarcation;
    protected final TransactionManager transactionManager;
    protected final TrackedConnectionAssociator trackedConnectionAssociator;
    protected final ReadOnlyContext componentContext;
    protected final EJBUserTransaction userTransaction;
    protected final Set unshareableResources;
    protected final TransactionPolicySource transactionPolicySource;
    protected final String contextId;
    protected final Subject runAs;
    protected final boolean setSecurityInterceptor;
    protected final boolean setPolicyContextHandlerDataEJB;
    protected final boolean setIdentity;


    protected final ClassLoader classLoader;
    protected final Class beanClass;

    protected final Class homeInterface;
    protected final Class remoteInterface;

    protected final Class localHomeInterface;
    protected final Class localInterface;

    protected TxnPolicy[][] transactionPolicy = new TxnPolicy[EJBInvocationType.getMaxTransactionPolicyKey() + 1][];

    protected EJBRemoteClientContainer remoteClientContainer;
    protected EJBLocalClientContainer localClientContainer;
    private Long remoteId;

    public AbstractEJBContainer(EJBContainerConfiguration config, TransactionManager transactionManager, TrackedConnectionAssociator trackedConnectionAssociator) throws Exception {
        this.transactionManager = transactionManager;
        this.trackedConnectionAssociator = trackedConnectionAssociator;

        // copy over all the config stuff
        uri = config.uri;
        ejbName = config.ejbName;
        txnDemarcation = config.txnDemarcation;
        userTransaction = config.userTransaction;
        componentContext = config.componentContext;
        unshareableResources = config.unshareableResources;
        transactionPolicySource = config.transactionPolicySource;
        contextId = config.contextId;
        runAs = config.runAs;
        setSecurityInterceptor = config.setSecurityInterceptor;
        setPolicyContextHandlerDataEJB = config.setPolicyContextHandlerDataEJB;
        setIdentity = config.setIdentity;

        // load all the classes
        classLoader = Thread.currentThread().getContextClassLoader();
        beanClass = classLoader.loadClass(config.beanClassName);
        if (config.homeInterfaceName != null) {
            homeInterface = classLoader.loadClass(config.homeInterfaceName);
            remoteInterface = classLoader.loadClass(config.remoteInterfaceName);
        } else {
            homeInterface = null;
            remoteInterface = null;
        }
        if (config.localHomeInterfaceName != null) {
            localHomeInterface = classLoader.loadClass(config.localHomeInterfaceName);
            localInterface = classLoader.loadClass(config.localInterfaceName);
        } else {
            localHomeInterface = null;
            localInterface = null;
        }

        // initialize the user transaction
        if (userTransaction != null) {
            userTransaction.setUp(transactionManager, trackedConnectionAssociator);
        }
    }

    public void setGBeanContext(GBeanContext context) {
    }

    public void doStart() throws WaitingException, Exception {
        if (userTransaction != null) {
            userTransaction.setOnline(true);
        }
    }

    public void doStop() throws WaitingException, Exception {
        if (userTransaction != null) {
            userTransaction.setOnline(false);
        }
    }

    public void doFail() {
        if (userTransaction != null) {
            userTransaction.setOnline(false);
        }
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

    protected URI startServerRemoting(Interceptor firstInterceptor) {
        // set up server side remoting endpoint
        if (setSecurityInterceptor) {
            firstInterceptor = new SubjectIdExtractInterceptor(firstInterceptor);
        }
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

    //TODO can the method map start out with MethodSignatures instead of methods?
    public void mapPolicies(String intfName, Map methodMap, TxnPolicy[] policies) {
        for (Iterator iterator = methodMap.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            MethodSignature signature = new MethodSignature((Method) entry.getKey());
            Integer index = (Integer) entry.getValue();
            TxnPolicy policy = transactionPolicySource.getTransactionPolicy(intfName, signature);
            policies[index.intValue()] = policy;
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory("OpenEJB Nova EJB Container (CMP Entity Beans)",
                AbstractEJBContainer.class.getName());
        /**
         * Default constructor; takes onl a EJBContainerConfiguration Object.
         * Any containers that wish to have different constructors (CMPContainer and MDBContainer have multiple argument
         * constructors), must override by setting their own constructor at GBEAN_INFO initialisation.
         */
        infoFactory.setConstructor(new GConstructorInfo(
                new String[]{"EJBContainerConfiguration", "TransactionManager", "TrackedConnectionAssociator"},
                new Class[]{EJBContainerConfiguration.class, TransactionManager.class, TrackedConnectionAssociator.class}));

        infoFactory.addAttribute(new GAttributeInfo("EJBContainerConfiguration", true));

        infoFactory.addReference(new GReferenceInfo("TransactionManager", TransactionManager.class.getName()));
        infoFactory.addReference(new GReferenceInfo("TrackedConnectionAssociator", TrackedConnectionAssociator.class.getName()));

        /**
         *	TODO: Dain informs me at some point we'll make these attributes, but currently JNDI Referencer can't support it in the way we want.
         */

        infoFactory.addOperation(new GOperationInfo("getComponentContext"));
        infoFactory.addOperation(new GOperationInfo("getDemarcation"));
        infoFactory.addOperation(new GOperationInfo("getEJBHome"));
        infoFactory.addOperation(new GOperationInfo("getEJBLocalHome"));
        infoFactory.addOperation(new GOperationInfo("getEJBLocalObject", new String[]{Object.class.getName()}));
        infoFactory.addOperation(new GOperationInfo("getEJBName"));
        infoFactory.addOperation(new GOperationInfo("getEJBObject", new String[]{Object.class.getName()}));
        infoFactory.addOperation(new GOperationInfo("getUserTransaction"));

        GBEAN_INFO = infoFactory.getBeanInfo();
    }


    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}


