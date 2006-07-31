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
package org.openejb;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.Set;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.naming.Context;
import javax.security.auth.Subject;

import org.apache.geronimo.interceptor.InvocationResult;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.security.deploy.DefaultPrincipal;
import org.apache.geronimo.timer.PersistenceException;
import org.openejb.client.EJBObjectHandler;
import org.openejb.client.EJBObjectProxy;
import org.openejb.corba.TSSBean;
import org.openejb.corba.transaction.MappedServerTransactionPolicyConfig;
import org.openejb.corba.transaction.OperationTxPolicy;
import org.openejb.corba.transaction.ServerTransactionPolicyConfig;
import org.openejb.corba.transaction.nodistributedtransactions.NoDTxServerTransactionPolicies;
import org.openejb.corba.util.Util;
import org.openejb.proxy.EJBProxyFactory;
import org.openejb.proxy.ProxyInfo;
import org.openejb.transaction.TransactionPolicyType;

/**
 * @version $Revision$ $Date$
 */
public abstract class AbstractRpcDeployment extends AbstractEjbDeployment implements RpcEjbDeployment {

    private final ProxyInfo proxyInfo;

    private final String[] jndiNames;
    private final String[] localJndiNames;

    protected final EJBProxyFactory proxyFactory;
    private final TSSBean tssBean;


    public AbstractRpcDeployment(String containerId,
            String ejbName,

            ProxyInfo proxyInfo,
            Class beanClass,
            ClassLoader classLoader,
            SignatureIndexBuilder signatureIndexBuilder,

            EjbContainer ejbContainer,

            String[] jndiNames,
            String[] localJndiNames,

            boolean securityEnabled,
            String policyContextId,
            DefaultPrincipal defaultPrincipal,
            Subject runAs,

            boolean beanManagedTransactions,
            SortedMap transactionPolicies,

            Map componentContext,

            Kernel kernel,

            TSSBean tssBean,

            Set unshareableResources,
            Set applicationManagedSecurityResources) throws Exception {

        super(containerId,
                ejbName,
                beanClass,
                classLoader,
                signatureIndexBuilder,
                ejbContainer,
                securityEnabled,
                policyContextId,
                defaultPrincipal,
                runAs,
                beanManagedTransactions,
                transactionPolicies,
                componentContext,
                kernel, unshareableResources, applicationManagedSecurityResources);
        assert (containerId != null);
        assert (ejbName != null && ejbName.length() > 0);
        assert (classLoader != null);

        // load the bean classes
        this.proxyInfo = proxyInfo;

        if (jndiNames == null) {
            jndiNames = new String[0];
        }
        this.jndiNames = jndiNames;
        if (localJndiNames == null) {
            localJndiNames = new String[0];
        }
        this.localJndiNames = localJndiNames;

        this.tssBean = tssBean;

        // create the proxy factory
        // NOTE: this can't be called until the signaure array has been built and the proxy info has been set
        proxyFactory = new EJBProxyFactory(this);

        // TODO maybe there is a more suitable place to do this.  Maybe not.
        setupJndi();
    }

    public ProxyInfo getProxyInfo() {
        return proxyInfo;
    }

    public String[] getJndiNames() {
        return copyNames(jndiNames);
    }

    public String[] getLocalJndiNames() {
        return copyNames(localJndiNames);
    }

    public EJBHome getEjbHome() {
        return proxyFactory.getEJBHome();
    }

    public EJBObject getEjbObject(Object primaryKey) {
        return proxyFactory.getEJBObject(primaryKey);
    }

    public EJBLocalHome getEjbLocalHome() {
        return proxyFactory.getEJBLocalHome();
    }

    public EJBLocalObject getEjbLocalObject(Object primaryKey) {
        return proxyFactory.getEJBLocalObject(primaryKey);
    }

    public int getMethodIndex(Method method) {
        return proxyFactory.getMethodIndex(method);
    }

    public Object invoke(Method method, Object[] args, Object primKey) throws Throwable {
        EJBInterfaceType invocationType = null;
        int index = getMethodIndex(method);

        Class serviceEndpointInterface = getProxyInfo().getServiceEndpointInterface();

        Class clazz = method.getDeclaringClass();
        if (EJBHome.class.isAssignableFrom(clazz)) {
            invocationType = EJBInterfaceType.HOME;
        } else if (EJBObject.class.isAssignableFrom(clazz)) {
            invocationType = EJBInterfaceType.REMOTE;
        } else if (serviceEndpointInterface != null && serviceEndpointInterface.isAssignableFrom(clazz)) {
            invocationType = EJBInterfaceType.WEB_SERVICE;
        } else {
            throw new IllegalArgumentException("Legacy invoke interface only supports remote and service-endpoint interfaces: " + clazz);
        }

        // extract the primary key from home ejb remove invocations
        if (invocationType == EJBInterfaceType.HOME && method.getName().equals("remove")) {
            primKey = args[0];
            if (primKey instanceof Handle) {
                Handle handle = (Handle) primKey;
                EJBObjectProxy ejbObject = (EJBObjectProxy) handle.getEJBObject();
                EJBObjectHandler handler = ejbObject.getEJBObjectHandler();
                primKey = handler.getRegistryId();
            }
        }

        EjbInvocation invocation = new EjbInvocationImpl(invocationType, primKey, index, args);

        InvocationResult result = null;
        try {
            result = invoke(invocation);
        } catch (Throwable t) {
            RemoteException re;
            if (t instanceof RemoteException) {
                re = (RemoteException) t;
            } else {
                re = new RemoteException("The bean encountered a non-application exception. method", t);
            }
            throw new InvalidateReferenceException(re);
        }

        if (result.isException()) {
            throw new ApplicationException(result.getException());
        } else {
            return result.getResult();
        }
    }

    public Serializable getHomeTxPolicyConfig() {
        if (proxyInfo.getHomeInterface() == null) {
            return null;
        }
        Serializable policy = buildTransactionImportPolicy(EJBInterfaceType.HOME, proxyInfo.getHomeInterface(), true);
        return policy;
    }

    public Serializable getRemoteTxPolicyConfig() {
        if (proxyInfo.getRemoteInterface() == null) {
            return null;
        }
        Serializable policy = buildTransactionImportPolicy(EJBInterfaceType.REMOTE, proxyInfo.getRemoteInterface(), false);
        return policy;
    }

    public Serializable buildTransactionImportPolicy(EJBInterfaceType methodIntf, Class intf, boolean isHomeMethod) {

        Map policies = new HashMap();

        Map methodToOperation = Util.mapMethodToOperation(intf);
        for (Iterator iterator = methodToOperation.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            Method method = (Method) entry.getKey();
            String operation = (String) entry.getValue();

            int index = getMethodIndex(method);
            TransactionPolicyType transactionPolicyType = transactionPolicyManager.getTransactionPolicyType(methodIntf, index);
            OperationTxPolicy operationTxPolicy = NoDTxServerTransactionPolicies.getTransactionPolicy(transactionPolicyType);
            policies.put(operation, operationTxPolicy);
        }
        ServerTransactionPolicyConfig serverTransactionPolicyConfig = new MappedServerTransactionPolicyConfig(policies);

        return serverTransactionPolicyConfig;
    }

    public void doStart() throws Exception {
        super.doStart();
        if (tssBean != null) {
            tssBean.registerContainer(this);
        }
    }

    protected void destroy() throws PersistenceException {
        if (tssBean != null) {
            tssBean.unregisterContainer(this);
        }
        super.destroy();
    }

    private static String[] copyNames(String[] names) {
        if (names == null) {
            return null;
        }
        int length = names.length;
        String[] copy = new String[length];
        System.arraycopy(names, 0, copy, 0, length);
        return copy;
    }

    private void setupJndi() {
        /* Add Geronimo JNDI service ///////////////////// */
        String str = System.getProperty(Context.URL_PKG_PREFIXES);
        if (str == null) {
            str = ":org.apache.geronimo.naming";
        } else {
            if (str.indexOf(":org.apache.geronimo.naming") < 0) {
                str = str + ":org.apache.geronimo.naming";
            }
        }
        System.setProperty(Context.URL_PKG_PREFIXES, str);
    }
}
