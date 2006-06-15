/**
 * 
 * Copyright 2006 The Apache Software Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.openejb.persistence;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

public class FakeDataSource implements DataSource {
    
    public Connection getConnection() throws SQLException {
        return null;
    }

    public Connection getConnection(String arg0, String arg1)
            throws SQLException {
        return null;
    }

    public PrintWriter getLogWriter() throws SQLException {
        return null;
    }

    public void setLogWriter(PrintWriter arg0) throws SQLException {
    }

    public void setLoginTimeout(int arg0) throws SQLException {
    }

    public int getLoginTimeout() throws SQLException {
        return 0;
    }

}
