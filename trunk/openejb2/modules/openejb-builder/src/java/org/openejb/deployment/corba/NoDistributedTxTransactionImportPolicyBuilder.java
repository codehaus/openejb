/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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
package org.openejb.deployment.corba;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.openejb.corba.transaction.MappedServerTransactionPolicyConfig;
import org.openejb.corba.transaction.OperationTxPolicy;
import org.openejb.corba.transaction.ServerTransactionPolicyConfig;
import org.openejb.corba.transaction.nodistributedtransactions.NoDTxServerTransactionPolicies;
import org.openejb.corba.compiler.IiopOperation;
import org.openejb.corba.compiler.PortableStubCompiler;
import org.openejb.dispatch.InterfaceMethodSignature;
import org.openejb.transaction.TransactionPolicySource;
import org.openejb.transaction.TransactionPolicyType;

/**
 * @version $Rev:  $ $Date$
 */
public class NoDistributedTxTransactionImportPolicyBuilder implements TransactionImportPolicyBuilder {

    public Serializable buildTransactionImportPolicy(String methodIntf, Class intf, boolean isHomeMethod, TransactionPolicySource transactionPolicySource, ClassLoader classLoader) {
        Map policies = new HashMap();
        IiopOperation[] iiopOperations = PortableStubCompiler.createIiopOperations(intf);
        for (int i = 0; i < iiopOperations.length; i++) {
            IiopOperation iiopOperation = iiopOperations[i];
            InterfaceMethodSignature interfaceMethodSignature = new InterfaceMethodSignature(iiopOperation.getMethod(), isHomeMethod);
            TransactionPolicyType transactionPolicyType = transactionPolicySource.getTransactionPolicy(methodIntf, interfaceMethodSignature);
            OperationTxPolicy operationTxPolicy = NoDTxServerTransactionPolicies.getTransactionPolicy(transactionPolicyType);
            String IDLOperationName = iiopOperation.getName();
            policies.put(IDLOperationName, operationTxPolicy);
        }
        ServerTransactionPolicyConfig serverTransactionPolicyConfig = new MappedServerTransactionPolicyConfig(policies);

        return serverTransactionPolicyConfig;
    }

}
