/* ====================================================================
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce this list of
 *    conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
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
 *    (http://openejb.org/).
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
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the OpenEJB Project.  For more information
 * please see <http://openejb.org/>.
 *
 * ====================================================================
 */
package org.openejb.nova.entity.cmp;

import java.lang.reflect.Method;
import javax.ejb.EnterpriseBean;
import javax.ejb.EntityBean;

import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.openejb.nova.EJBContainer;
import org.openejb.nova.entity.EntityInstanceContext;

/**
 *
 *
 * @version $Revision$ $Date$
 */
public final class CMPInstanceContext extends EntityInstanceContext implements MethodInterceptor {
    private final EntityBean instance;
    private InstanceData instanceData;

    public CMPInstanceContext(EJBContainer container, Factory factory) throws Exception {
        super(container);
        instance = (EntityBean) factory.newInstance(this);
    }

    public EnterpriseBean getInstance() {
        return instance;
    }

    public InstanceData getInstanceData() {
        return instanceData;
    }

    public void setInstanceData(InstanceData instanceData) {
        this.instanceData = instanceData;
    }

    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        int index = methodProxy.getSuperIndex();

        InstanceOperation iop = ((CMPEntityContainer) container).getITable()[index];
        return iop.invokeInstance(this, objects);
    }

    public void associate() throws Exception {
        if (id != null && !isStateValid()) {
            instanceData = ((CMPEntityContainer) container).getInstanceData(id);
        }
        super.associate();
    }

    public void flush() throws Exception {
        super.flush();
        if (id != null) {
            assert (isStateValid()) : "Attempting to flush instance without valid state";
            ((CMPEntityContainer) container).setInstanceData(id, instanceData);
        }
    }

    public void addRelation(int slot, Object primaryKey) {
    }

    public void removeRelation(int slot, Object primaryKey) {
    }
}
