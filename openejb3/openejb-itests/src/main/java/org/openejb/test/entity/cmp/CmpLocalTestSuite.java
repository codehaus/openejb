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
package org.openejb.test.entity.cmp;

import org.openejb.test.TestManager;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Properties;

/**
 *
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class CmpLocalTestSuite extends org.openejb.test.TestSuite{

    public CmpLocalTestSuite(){
        super();
        this.addTest(new CmpJndiTests());
        this.addTest(new CmpHomeIntfcTests());
        this.addTest(new CmpEjbHomeTests());
        this.addTest(new CmpEjbObjectTests());
        this.addTest(new CmpRemoteIntfcTests());
        this.addTest(new CmpHomeHandleTests());
        this.addTest(new CmpHandleTests());
        this.addTest(new CmpEjbMetaDataTests());
        //TODO:0:this.addTest(new CmpAllowedOperationsTests());
        this.addTest(new CmpJndiEncTests());
        this.addTest(new CmpRmiIiopTests());

    }

    public static junit.framework.Test suite() {
        return new CmpLocalTestSuite();
    }

    /**
     * Sets up the fixture, for example, open a network connection.
     * This method is called before a test is executed.
     */
    protected void setUp() throws Exception {
        Properties props = TestManager.getServer().getContextEnvironment();
        props.put(Context.SECURITY_PRINCIPAL, "ENTITY_TEST_CLIENT");
        props.put(Context.SECURITY_CREDENTIALS, "ENTITY_TEST_CLIENT");
        InitialContext initialContext = new InitialContext(props);

        /*[2] Create database table */
        TestManager.getDatabase().createEntityTable();
    }

    /**
     * Tears down the fixture, for example, close a network connection.
     * This method is called after a test is executed.
     */
    protected void tearDown() throws Exception {
        /*[1] Drop database table */
        TestManager.getDatabase().dropEntityTable();
    }
}
