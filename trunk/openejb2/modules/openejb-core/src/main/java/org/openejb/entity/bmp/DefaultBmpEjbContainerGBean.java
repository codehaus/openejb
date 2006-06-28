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
package org.openejb.entity.bmp;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.apache.geronimo.transaction.TrackedConnectionAssociator;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.timer.PersistentTimer;
import org.openejb.BmpEjbContainer;

/**
 * @version $Revision$ $Date$
 */
public final class DefaultBmpEjbContainerGBean {

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(DefaultBmpEjbContainerGBean.class, DefaultBmpEjbContainer.class, "BmpEjbContainer");

        infoFactory.addReference("TransactionContextManager", TransactionContextManager.class, NameFactory.TRANSACTION_CONTEXT_MANAGER);
        infoFactory.addReference("TrackedConnectionAssociator", TrackedConnectionAssociator.class, NameFactory.JCA_CONNECTION_TRACKER);
        infoFactory.addReference("TransactedTimer", PersistentTimer.class, NameFactory.GERONIMO_SERVICE);
        infoFactory.addReference("NontransactedTimer", PersistentTimer.class, NameFactory.GERONIMO_SERVICE);
        infoFactory.addAttribute("securityEnabled", boolean.class, true);
        infoFactory.addAttribute("doAsCurrentCaller", boolean.class, true);
        infoFactory.addAttribute("useContextHandler", boolean.class, true);
        infoFactory.setConstructor(new String[]{
            "TransactionContextManager",
            "TrackedConnectionAssociator",
            "TransactedTimer",
            "NontransactedTimer",
            "securityEnabled",
            "doAsCurrentCaller",
            "useContextHandler"});

        infoFactory.addInterface(BmpEjbContainer.class);

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
