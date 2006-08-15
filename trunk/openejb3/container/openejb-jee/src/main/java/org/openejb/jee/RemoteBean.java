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
package org.openejb.jee;

import java.util.List;

/**
 * @version $Revision$ $Date$
 */
public interface RemoteBean {

    public String getHome();

    public void setHome(String value);

    public String getRemote();

    public void setRemote(String value);

    public String getLocalHome();

    public void setLocalHome(String value);

    public String getLocal();

    public void setLocal(String value);

    public List<SecurityRoleRef> getSecurityRoleRef();
}