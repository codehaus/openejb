/**
 *
 * Copyright 2004 The Apache Software Foundation
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
package org.openejb;

import javax.transaction.TransactionManager;
import javax.management.ObjectName;

import org.apache.geronimo.naming.java.ReadOnlyContext;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.transaction.TrackedConnectionAssociator;

import org.apache.geronimo.transaction.UserTransactionImpl;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.openejb.deployment.TransactionPolicySource;

/**
 *
 *
 * @version $Revision$ $Date$
 */
public interface ContainerBuilder extends ResourceEnvironmentBuilder, SecureBuilder {
    ClassLoader getClassLoader();

    void setClassLoader(ClassLoader classLoader);

    String getContainerId();

    void setContainerId(String containerId);

    String getEJBName();

    void setEJBName(String ejbName);

    String getBeanClassName();

    void setBeanClassName(String beanClassName);

    String getHomeInterfaceName();

    void setHomeInterfaceName(String homeInterfaceName);

    String getRemoteInterfaceName();

    void setRemoteInterfaceName(String remoteInterfaceName);

    String getLocalHomeInterfaceName();

    void setLocalHomeInterfaceName(String localHomeInterfaceName);

    String getLocalInterfaceName();

    void setLocalInterfaceName(String localInterfaceName);

    String getServiceEndpointName();

    void setServiceEndpointName(String localInterfaceName);

    String getPrimaryKeyClassName();

    void setPrimaryKeyClassName(String primaryKeyClassName);

    ReadOnlyContext getComponentContext();

    void setComponentContext(ReadOnlyContext componentContext);

    UserTransactionImpl getUserTransaction();

    void setUserTransaction(UserTransactionImpl userTransaction);

    TransactionPolicySource getTransactionPolicySource();

    void setTransactionPolicySource(TransactionPolicySource transactionPolicySource);

    TransactionContextManager getTransactionContextManager();

    void setTransactionContextManager(TransactionContextManager transactionContextManager);

    TrackedConnectionAssociator getTrackedConnectionAssociator();

    void setTrackedConnectionAssociator(TrackedConnectionAssociator trackedConnectionAssociator);

    String[] getJndiNames();

    void setJndiNames(String[] jndiNames);

    String[] getLocalJndiNames();

    void setLocalJndiNames(String[] localJndiNames);

    EJBContainer createContainer() throws Exception;

    GBeanMBean createConfiguration() throws Exception;

    ObjectName getTransactedTimerName();

    void setTransactedTimerName(ObjectName transactedTimerName);

    ObjectName getNonTransactedTimerName();

    void setNonTransactedTimerName(ObjectName nonTransactedTimerName);

}