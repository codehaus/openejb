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
package org.openejb.nova.mdb;

import java.lang.reflect.Method;
import java.util.Map;
import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.UnavailableException;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.xa.XAResource;

import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.ejb.metadata.TransactionDemarcation;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GConstructorInfo;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.naming.java.ComponentContextInterceptor;

import org.openejb.nova.AbstractEJBContainer;
import org.openejb.nova.ConnectionTrackingInterceptor;
import org.openejb.nova.EJBContainerConfiguration;
import org.openejb.nova.SystemExceptionInterceptor;
import org.openejb.nova.EJBInvocationType;
import org.openejb.nova.dispatch.DispatchInterceptor;
import org.openejb.nova.dispatch.MethodHelper;
import org.openejb.nova.dispatch.MethodSignature;
import org.openejb.nova.security.EJBIdentityInterceptor;
import org.openejb.nova.security.EJBRunAsInterceptor;
import org.openejb.nova.security.EJBSecurityInterceptor;
import org.openejb.nova.security.PolicyContextHandlerEJBInterceptor;
import org.openejb.nova.transaction.TransactionContextInterceptor;
import org.openejb.nova.transaction.TxnPolicy;
import org.openejb.nova.transaction.ContainerPolicy;
import org.openejb.nova.util.SoftLimitedInstancePool;

/**
 * @version $Revision$ $Date$
 */
public class MDBContainer extends AbstractEJBContainer implements MessageEndpointFactory {
    private final ActivationSpec activationSpec;
    private final Class messageEndpointInterface;
    private MDBLocalClientContainer messageClientContainer;

    public MDBContainer(EJBContainerConfiguration config, ActivationSpec activationSpec) throws Exception {
        super(config);
        this.activationSpec = activationSpec;

        messageEndpointInterface = Thread.currentThread().getContextClassLoader().loadClass(config.messageEndpointInterfaceName);
    }

    public Class getMessageEndpointInterface() {
        return messageEndpointInterface;
    }

    public void doStart() throws WaitingException, Exception {
        super.doStart();

        MDBOperationFactory vopFactory = MDBOperationFactory.newInstance(beanClass);
        vtable = vopFactory.getVTable();
        buildMDBTransactionPolicyMap(vopFactory.getSignatures());

        pool = new SoftLimitedInstancePool(new MDBInstanceFactory(this), 1);

        // set up server side interceptors
        Interceptor firstInterceptor;
        firstInterceptor = new DispatchInterceptor(vtable);
        if (trackedConnectionAssociator != null) {
            firstInterceptor = new ConnectionTrackingInterceptor(firstInterceptor, trackedConnectionAssociator, unshareableResources);
        }
        firstInterceptor = new TransactionContextInterceptor(firstInterceptor, txnManager, transactionPolicy);
        if (setIdentity) {
            firstInterceptor = new EJBIdentityInterceptor(firstInterceptor);
        }
        if (setSecurityInterceptor) {
            firstInterceptor = new EJBSecurityInterceptor(firstInterceptor, contextId, MethodHelper.generatePermissions(ejbName, vopFactory.getSignatures()));
        }
        if (runAs != null) {
            firstInterceptor = new EJBRunAsInterceptor(firstInterceptor, runAs);
        }
        if (setPolicyContextHandlerDataEJB) {
            firstInterceptor = new PolicyContextHandlerEJBInterceptor(firstInterceptor);
        }
        firstInterceptor = new MDBInstanceInterceptor(firstInterceptor, pool);
        firstInterceptor = new ComponentContextInterceptor(firstInterceptor, componentContext);
        firstInterceptor = new SystemExceptionInterceptor(firstInterceptor, getEJBName());
        firstInterceptor = new MDBClassLoaderInterceptor(firstInterceptor, classLoader, -1, -1);

        // set up client containers
        MDBClientContainerFactory clientFactory = new MDBClientContainerFactory(vopFactory, firstInterceptor, messageEndpointInterface);
        messageClientContainer = clientFactory.getMessageClientContainer();
        //buildMDBMethodMap(vopFactory.getSignatures());

        try {
            // Setup the endpoint.
            getAdapter().endpointActivation(this, activationSpec);
        } catch (ResourceException e) {
            throw new RuntimeException("The resource adapter did not accept the activation of the MDB endpoint", e);
        }
    }

    public void doStop() throws WaitingException, Exception {
        // Deactivate the endpoint.
        getAdapter().endpointDeactivation(this, activationSpec);

        localClientContainer = null;
        pool = null;
        super.doStop();
    }

    /**
     * @see javax.resource.spi.endpoint.MessageEndpointFactory#createEndpoint(javax.transaction.xa.XAResource)
     */
    public MessageEndpoint createEndpoint(XAResource adapterXAResource) throws UnavailableException {
        return messageClientContainer.getMessageEndpoint(adapterXAResource);
    }

    /**
     * @see javax.resource.spi.endpoint.MessageEndpointFactory#isDeliveryTransacted(java.lang.reflect.Method)
     */
    public boolean isDeliveryTransacted(Method method) throws NoSuchMethodException {
        // TODO: need to see if the method is Supports or Required.
        return MDBContainer.this.txnDemarcation == TransactionDemarcation.CONTAINER;

    }

    private void buildMDBTransactionPolicyMap(MethodSignature[] signatures) {
        TxnPolicy[] localPolicies = new TxnPolicy[signatures.length];
        Map localMethodMap = MethodHelper.getObjectMethodMap(signatures, messageEndpointInterface);
        mapPolicies("Local", localMethodMap, localPolicies);
        transactionPolicy[EJBInvocationType.LOCAL.getTransactionPolicyKey()] = localPolicies;
        transactionPolicy[EJBInvocationType.MESSAGE_ENDPOINT.getTransactionPolicyKey()] =
                new TxnPolicy[]{ContainerPolicy.BeforeDelivery, ContainerPolicy.AfterDelivery};
    }

    private ResourceAdapter getAdapter() {
        if (activationSpec.getResourceAdapter() == null) {
            throw new IllegalStateException("Attempting to use activation spec when it is not activated");
        }
        return activationSpec.getResourceAdapter();
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(MDBContainer.class.getName(), AbstractEJBContainer.GBEAN_INFO);

        infoFactory.setConstructor(new GConstructorInfo(
                new String[]{"EJBContainerConfiguration", "ActivationSpec"},
                new Class[]{EJBContainerConfiguration.class, ActivationSpec.class}));

        infoFactory.addAttribute(new GAttributeInfo("ActivationSpec", true));
        GBEAN_INFO = infoFactory.getBeanInfo();
    }


    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
