/**
 *
 * Copyright 2005 The Apache Software Foundation
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
package org.openejb.entity.cmp;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Kernel;

import javax.transaction.TransactionManager;

/**
 * @version $Revision$ $Date$
 */
public final class TranqlModuleCmpEngineGBean {

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(TranqlModuleCmpEngineGBean.class, TranqlModuleCmpEngine.class, "ModuleCmpEngine");

        infoFactory.addAttribute("moduleSchema", ModuleSchema.class, true);
        infoFactory.addReference("transactionManager", TransactionManager.class, NameFactory.TRANSACTION_MANAGER);
        infoFactory.addReference("connectionFactory", ConnectionProxyFactory.class, NameFactory.JCA_CONNECTION_FACTORY);
        infoFactory.addAttribute("classLoader", ClassLoader.class, false);
        infoFactory.addAttribute("kernel", Kernel.class, false);

        infoFactory.setConstructor(new String[]{
            "moduleSchema",
            "transactionManager",
            "connectionFactory",
            "classLoader",
            "kernel",
        });

        infoFactory.addInterface(ModuleCmpEngine.class);

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
