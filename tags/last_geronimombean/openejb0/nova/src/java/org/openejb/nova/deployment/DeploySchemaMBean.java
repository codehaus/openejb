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

package org.openejb.nova.deployment;

import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.RuntimeOperationsException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.NotCompliantMBeanException;
import javax.sql.DataSource;

import org.apache.geronimo.kernel.deployment.task.DeployGeronimoMBean;
import org.apache.geronimo.kernel.deployment.service.MBeanMetadata;
import org.apache.geronimo.kernel.deployment.DeploymentException;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.kernel.service.GeronimoMBeanInfo;
import org.apache.geronimo.kernel.service.GeronimoMBean;
import org.openejb.nova.entity.cmp.SimpleCommandFactory;

/**
 *
 *
 * @version $Revision$ $Date$
 *
 * */
public class DeploySchemaMBean extends DeployGeronimoMBean{

    private final ObjectName datasourceName;
    private SimpleCommandFactory schema;

    public DeploySchemaMBean(MBeanServer server, ObjectName datasourceName, MBeanMetadata metadata) {
        super(server, metadata);
        this.datasourceName = datasourceName;
    }

    public SimpleCommandFactory getSchema() {
        assert schema != null;
        return schema;
    }

    public  boolean canRun() throws DeploymentException {
        if (!super.canRun()) {
            return false;
        }
        try {
            return new Integer(State.RUNNING_INDEX).equals(server.getAttribute(datasourceName, "state"));
        } catch (Exception e) {
            return false;
        }
    }

    public void perform() throws DeploymentException {
        DataSource ds;
        try {
            ds = (DataSource)server.invoke(datasourceName, "getConnectionFactory", null, null);
        } catch (InstanceNotFoundException e) {
            throw new DeploymentException("Did not find datasource factory at " + datasourceName, e);
        } catch (MBeanException e) {
            throw new DeploymentException("Problem accessing datasource factory at " + datasourceName, e);
        } catch (ReflectionException e) {
            throw new DeploymentException("Problem accessing datasource factory at " + datasourceName, e);
        }
        schema = new SimpleCommandFactory(ds);
        GeronimoMBeanInfo info = new GeronimoMBeanInfo();
        info.setTargetClass(SimpleCommandFactory.class.getName());
        info.setTarget(schema);
        GeronimoMBean mbean = new GeronimoMBean();
        try {
            mbean.setMBeanInfo(info);
            server.registerMBean(mbean, metadata.getName());
        } catch (MBeanException e) {
            throw new DeploymentException("Problem setting MBeanInfo on GeronimoMBean " + metadata.getName(), e);
        } catch (RuntimeOperationsException e) {
            throw new DeploymentException("Problem setting MBeanInfo on GeronimoMBean " + metadata.getName(), e);
        } catch (InstanceAlreadyExistsException e) {
            throw new DeploymentException("Problem registering schema mbean " + metadata.getName(), e);
        } catch (NotCompliantMBeanException e) {
            throw new DeploymentException("Problem registering schema mbean " + metadata.getName(), e);
        }
    }
}
