/**
 *
 * Copyright 2006 The Apache Software Foundation
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

package org.openejb.deployment;

import java.net.UnknownHostException;
import java.util.Collections;

import javax.naming.Reference;

import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.openejb.client.ServerMetaData;
import org.openejb.client.naming.RemoteEJBReference;

/**
 * @version $Rev:$ $Date:$
 */
public class OpenEjbClientRemoteRefBuilder extends OpenEjbRemoteRefBuilder {

    private final ServerMetaData server;

    public OpenEjbClientRemoteRefBuilder(Environment defaultEnvironment, String host, int port) throws UnknownHostException{
        super(defaultEnvironment);
        server = new ServerMetaData("BOOT", host, port);
    }

    protected Reference buildRemoteReference(Artifact configurationId, AbstractNameQuery abstractNameQuery, boolean session, String home, String remote) {
        Reference reference = new RemoteEJBReference(abstractNameQuery.toString(), Collections.singletonList(server));
        return reference;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(OpenEjbClientRemoteRefBuilder.class, OpenEjbRemoteRefBuilder.GBEAN_INFO, NameFactory.MODULE_BUILDER); //TODO decide what type this should be

        infoBuilder.addAttribute("host", String.class, true);
        infoBuilder.addAttribute("port", int.class, true);

        infoBuilder.setConstructor(new String[]{"defaultEnvironment", "host", "port"});

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
