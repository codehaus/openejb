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
package org.openejb.test;

import org.openejb.client.RemoteInitialContextFactory;

import java.util.Properties;

/**
 * @version $Revision$ $Date$
 */
public class TomcatRemoteTestServer implements TestServer {
    private Properties properties;

    public void init(Properties props) {
        properties = props;
        props.put("test.server.class", TomcatRemoteTestServer.class.getName());
        props.put("java.naming.factory.initial", RemoteInitialContextFactory.class.getName());
        props.put("java.naming.provider.url","http://127.0.0.1:8080/openejb/remote");
    }

    public void start() {
        System.out.println("Note: Tomcat should be started before running these tests");
    }

    public void stop() {
    }


    public Properties getContextEnvironment(){
        return (Properties)properties.clone();
    }
}
