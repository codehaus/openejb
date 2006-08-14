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
package org.openejb.persistence;

import javax.persistence.EntityManagerFactory;
import javax.naming.Name;
import javax.naming.Context;
import java.util.Map;
import java.util.HashMap;
import java.util.Hashtable;

/**
 * @version $Revision$ $Date$
 */
public class EntityManagerFactoryCache implements javax.naming.spi.ObjectFactory {
    private final Map<ClassLoader, Map<String, EntityManagerFactory>> cache = new HashMap();
    private final PersistenceDeployer deployer;

    public EntityManagerFactoryCache(PersistenceDeployer deployer) {
        this.deployer = deployer;
    }

    public Map<String,EntityManagerFactory> getEntityManagerFactories() throws PersistenceDeployerException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Map<String, EntityManagerFactory> factories = cache.get(classLoader);
        if (factories == null){
            factories = deployer.deploy(classLoader);
            cache.put(classLoader, factories);
        }

        return factories;
    }

    public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) throws Exception {
        return getEntityManagerFactories().get(name.toString());
    }
}
