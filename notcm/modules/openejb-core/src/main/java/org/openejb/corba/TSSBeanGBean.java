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
import org.openejb.corba.security.config.tss.TSSConfig;
import org.openejb.RpcEjbDeployment;

/**
 * @version $Revision$ $Date$
 */
public final class TSSBeanGBean {

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(TSSBeanGBean.class, TSSBean.class, NameFactory.CORBA_TSS);

        infoFactory.addAttribute("classLoader", ClassLoader.class, false);
        infoFactory.addAttribute("POAName", String.class, true);
        infoFactory.addReference("Server", CORBABean.class, NameFactory.CORBA_SERVICE);
        infoFactory.addAttribute("tssConfig", TSSConfig.class, true);
        infoFactory.addOperation("registerContainer", new Class[] {RpcEjbDeployment.class});
        infoFactory.addOperation("unregisterContainer", new Class[] {RpcEjbDeployment.class});
        infoFactory.setConstructor(new String[]{"classLoader", "POAName", "Server"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
