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
package org.openejb.assembler.spring;

import org.openejb.DeploymentInfo;
import org.openejb.Container;
import org.openejb.OpenEJBException;

/**
 * @version $Revision$ $Date$
 */
public class ContainerPointer implements Container {
    private final String containerId;

    public ContainerPointer(String containerId) {
        this.containerId = containerId;
    }

    public String getContainerId() {
        return containerId;
    }

    public int getContainerType() {
        return -1;
    }

    public Object getContainerID() {
        return containerId;
    }

    public DeploymentInfo getDeploymentInfo(Object deploymentID) {
        return null;
    }

    public DeploymentInfo [] deployments() {
        return new DeploymentInfo[0];
    }

    public void deploy(Object deploymentID, DeploymentInfo info) throws OpenEJBException {
    }
}
