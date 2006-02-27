/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

package org.openejb;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.ejb.EnterpriseBean;
import javax.ejb.TimerService;

import org.apache.geronimo.core.service.Interceptor;
import org.openejb.proxy.EJBProxyFactory;
import org.openejb.timer.TimerServiceImpl;
import org.openejb.timer.BasicTimerService;
import org.openejb.timer.UnavailableTimerService;


/**
 * Simple implementation of ComponentContext satisfying invariant.
 *
 * @version $Revision$ $Date$
 *
 * */
public abstract class AbstractInstanceContext implements EJBInstanceContext {
    private final Object containerId;
    private final EnterpriseBean instance;
    protected final Interceptor systemChain;
    private final EJBProxyFactory proxyFactory;
    private final BasicTimerService activeTimer;
    private final TimerService timerService;
    private final Set unshareableResources;
    private final Set applicationManagedSecurityResources;

    private final Map connectionManagerMap = new HashMap();

    private BasicTimerService timerState = UnavailableTimerService.INSTANCE;
    private boolean dead = false;
    private int callDepth;

    public AbstractInstanceContext(Object containerId, EnterpriseBean instance, Interceptor systemChain, EJBProxyFactory proxyFactory, BasicTimerService basicTimerService, Set unshareableResources, Set applicationManagedSecurityResources) {
        this.containerId = containerId;
        this.instance = instance;
        this.systemChain = systemChain;
        this.proxyFactory = proxyFactory;
        this.activeTimer = basicTimerService;
        this.timerService = basicTimerService == null? null: new TimerServiceImpl(this);
        this.unshareableResources = unshareableResources;
        this.applicationManagedSecurityResources = applicationManagedSecurityResources;
    }

    public Object getId() {
        return null;
    }

    public Object getContainerId() {
        return containerId;
    }

    public void associate() throws Throwable {
        if (dead) {
            throw new IllegalStateException("Context is dead: container=" + getContainerId() + ", id=" + getId());
        }
    }

    public void flush() throws Throwable {
        if (dead) {
            throw new IllegalStateException("Context is dead: container=" + getContainerId() + ", id=" + getId());
        }
    }

    public void beforeCommit() throws Throwable {
        if (dead) {
            throw new IllegalStateException("Context is dead: container=" + getContainerId() + ", id=" + getId());
        }
    }

    public void afterCommit(boolean status) throws Throwable {
        if (dead) {
            throw new IllegalStateException("Context is dead: container=" + getContainerId() + ", id=" + getId());
        }
    }

    public void unassociate() throws Throwable {
    }

    public Map getConnectionManagerMap() {
        return connectionManagerMap;
    }

    public Set getUnshareableResources() {
        return unshareableResources;
    }

    public Set getApplicationManagedSecurityResources() {
        return applicationManagedSecurityResources;
    }

    public EnterpriseBean getInstance() {
        return instance;
    }

    public EJBProxyFactory getProxyFactory() {
        return proxyFactory;
    }

    public TimerService getTimerService() {
        return timerService;
    }

    public BasicTimerService getBasicTimerService() {
        return timerState;
    }

    public void setTimerServiceAvailable(boolean available) {
        if (available) {
            timerState = activeTimer;
        } else {
            timerState = UnavailableTimerService.INSTANCE;
        }
    }

    public void die() {
        this.dead = true;
    }

    public final boolean isDead() {
        return dead;
    }

    public boolean isInCall() {
        return callDepth > 0;
    }

    public void enter() {
        callDepth++;
    }

    public void exit() {
        assert isInCall();
        callDepth--;
    }

    public String toString() {
        return "[InstanceContext: container=" + getContainerId() + ", id=" + getId() + "]";
    }

}
