package org.openejb.alt.config;

import org.openejb.jee.SessionType;
import org.openejb.jee.ResourceRef;

public class SessionBean implements Bean {

    org.openejb.jee.SessionBean bean;
    String type;

    SessionBean(org.openejb.jee.SessionBean bean) {
        this.bean = bean;
        if (bean.getSessionType() == SessionType.STATEFUL) {
            type = STATEFUL;
        } else {
            type = STATELESS;
        }
    }

    public String getType() {
        return type;
    }

    public Object getBean() {
        return bean;
    }

    public String getEjbName() {
        return bean.getEjbName();
    }

    public String getEjbClass() {
        return bean.getEjbClass();
    }

    public String getHome() {
        return bean.getHome();
    }

    public String getRemote() {
        return bean.getRemote();
    }

    public String getLocal() {
        return bean.getLocal();
    }

    public String getLocalHome() {
        return bean.getLocalHome();
    }

    public ResourceRef[] getResourceRef() {
        return bean.getResourceRef().toArray(new ResourceRef[]{});
    }
}

