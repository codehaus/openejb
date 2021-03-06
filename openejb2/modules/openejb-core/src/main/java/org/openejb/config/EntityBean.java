/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact openejb-group@openejb.sf.net.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.sf.net/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE OPENEJB GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE OPENEJB GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2001 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.config;

import org.openejb.config.ejb11.*;

/**
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 */
public class EntityBean implements Bean {

    Entity bean;
    String type;

    EntityBean(Entity bean) {
        this.bean = bean;
        if (bean.getPersistenceType().equals("Container")) {
            type = CMP_ENTITY;
        } else {
            type = BMP_ENTITY;
        }
    }

    public EjbLocalRef[] getEjbLocalRef() {
        return bean.getEjbLocalRef();
    }

    public String getLocal() {
        return bean.getLocal();
    }

    public String getLocalHome() {
        return bean.getLocalHome();
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

    public String getPrimaryKey() {
        return bean.getPrimKeyClass();
    }

    public EjbRef[] getEjbRef() {
        return bean.getEjbRef();
    }

    public EnvEntry[] getEnvEntry() {
        return bean.getEnvEntry();
    }

    public ResourceRef[] getResourceRef() {
        return bean.getResourceRef();
    }

    public SecurityRoleRef[] getSecurityRoleRef() {
        return bean.getSecurityRoleRef();
    }

}

