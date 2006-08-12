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

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.util.Properties;

/**
 * @version $Revision$ $Date$
 */
public class GlobalJndiDataSourceResolver implements DataSourceResolver {
    private final Properties jndiProperties;
    private final InitialContext initialContext;

    public GlobalJndiDataSourceResolver(Properties jndiProperties) {
        this.jndiProperties = jndiProperties;

        try {
            initialContext = new InitialContext(this.jndiProperties);
        } catch (NamingException ne) {
            throw new RuntimeException(ne);
        }
    }

    public DataSource getDataSource(String name) throws Exception {
        return (DataSource) initialContext.lookup(name);
    }
}
