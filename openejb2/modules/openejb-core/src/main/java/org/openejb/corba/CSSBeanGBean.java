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
package org.openejb.corba;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.openejb.corba.security.config.css.CSSConfig;
import org.omg.CORBA.ORB;

import java.util.ArrayList;
import java.util.Properties;
import java.net.URI;

import EDU.oswego.cs.dl.util.concurrent.Executor;

/**
 * @version $Revision$ $Date$
 */
public final class CSSBeanGBean {

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(CSSBeanGBean.class, CSSBean.class, NameFactory.CORBA_CSS);

        infoFactory.addAttribute("configAdapter", String.class, true);
        infoFactory.addAttribute("description", String.class, true);
        infoFactory.addAttribute("nssConfig", CSSConfig.class, true);
        infoFactory.addAttribute("cssConfig", CSSConfig.class, true);
        infoFactory.addAttribute("ORB", ORB.class, false);
        infoFactory.addAttribute("nssArgs", ArrayList.class, true);
        infoFactory.addAttribute("cssArgs", ArrayList.class, true);
        infoFactory.addAttribute("nssProps", Properties.class, true);
        infoFactory.addAttribute("cssProps", Properties.class, true);
        infoFactory.addOperation("getHome", new Class[]{URI.class, String.class});

        infoFactory.addReference("ThreadPool", Executor.class, NameFactory.GERONIMO_SERVICE);
        infoFactory.addReference("TransactionContextManager", TransactionContextManager.class, NameFactory.TRANSACTION_CONTEXT_MANAGER);
        infoFactory.addAttribute("classLoader", ClassLoader.class, false);

        infoFactory.setConstructor(new String[]{"configAdapter", "ThreadPool", "TransactionContextManager", "classLoader"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
