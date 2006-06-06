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
package org.openejb.jee.javaee;

import org.openejb.jee.javaee.JndiEnvironmentRef;
import org.openejb.jee.javaee.ResAuth;

/**
 * @version $Revision$ $Date$
 */
public class ResourceRef extends JndiEnvironmentRef {
    private String resRefName;
    private String resType;
    private ResAuth resAuth;
    private ResourceSharingScope resourceSharingScope;

    public ResourceRef() {
    }

    public ResourceRef(String resRefName) {
        this.resRefName = resRefName;
    }

    public String getResRefName() {
        return resRefName;
    }

    public void setResRefName(String resRefName) {
        this.resRefName = resRefName;
    }

    public String getResType() {
        return resType;
    }

    public void setResType(String resType) {
        this.resType = resType;
    }

    public ResAuth getResAuth() {
        return resAuth;
    }

    public void setResAuth(ResAuth resAuth) {
        this.resAuth = resAuth;
    }

    public ResourceSharingScope getResourceSharingScope() {
        return resourceSharingScope;
    }

    public void setResourceSharingScope(ResourceSharingScope resourceSharingScope) {
        this.resourceSharingScope = resourceSharingScope;
    }
}