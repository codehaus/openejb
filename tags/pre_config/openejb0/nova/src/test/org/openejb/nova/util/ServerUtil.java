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
package org.openejb.nova.util;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import javax.management.Attribute;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import org.apache.geronimo.kernel.deployment.service.MBeanRelationshipMetadata;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.kernel.service.GeronimoMBeanInfo;
import org.apache.geronimo.kernel.service.GeronimoMBean;
import org.openejb.nova.deployment.EJBInfo;

/**
 *
 *
 * @version $Revision$ $Date$
 */
public class ServerUtil {
    private static final ObjectName LOADER = JMXUtil.getObjectName("geronimo.remoting:transport=async");
    private static final ObjectName SUBSYSTEM = JMXUtil.getObjectName("geronimo.remoting:router=SubsystemRouter");
    private static final ObjectName JMX_ROUTER = JMXUtil.getObjectName("geronimo.remoting:router=JMXRouter");
    private static final ObjectName RELATIONSHIP = JMXUtil.getObjectName("geronimo.remoting:role=Relationship,name=Route");
    private static final Object[] REL_ARGS = {"name=Route\nleft.name=Source\nright.name=Target\nright.class=org.apache.geronimo.remoting.router.RouterTargetMBean"};
    private static final ObjectName RELATION_SERVICE = JMXUtil.getObjectName("geronimo.boot:role=RelationService");
    private static final ObjectName DEPENDS_SERVICE1 = JMXUtil.getObjectName("geronimo.boot:role=DependencyService");
    private static final ObjectName DEPENDS_SERVICE2 = JMXUtil.getObjectName("geronimo.boot:role=DependencyService2");

    public static MBeanServer newLocalServer() throws Exception {
        MBeanServer mbServer = MBeanServerFactory.createMBeanServer("LocalTestServer");
        mbServer.createMBean("javax.management.relation.RelationService", RELATION_SERVICE, new Object[]{Boolean.TRUE}, new String[]{"boolean"});
        mbServer.createMBean("org.apache.geronimo.kernel.deployment.DependencyService", DEPENDS_SERVICE1);
        mbServer.createMBean("org.apache.geronimo.kernel.service.DependencyService2", DEPENDS_SERVICE2);

        return mbServer;
    }

    public static MBeanServer newRemoteServer() throws Exception {
        MBeanServer mbServer = newLocalServer();
        mbServer.createMBean("org.apache.geronimo.remoting.router.SubsystemRouter", SUBSYSTEM);

        mbServer.createMBean("org.apache.geronimo.common.jmx.Relationship", RELATIONSHIP, REL_ARGS, new String[]{"java.lang.String"});

        mbServer.createMBean("org.apache.geronimo.remoting.transport.TransportLoader", LOADER);
        mbServer.setAttribute(LOADER, new Attribute("BindURI", new URI("async://0.0.0.0:3434")));
        mbServer.setAttribute(LOADER, new Attribute("RouterTarget", SUBSYSTEM.toString()));

        mbServer.createMBean("org.apache.geronimo.remoting.router.JMXRouter", JMX_ROUTER);
        MBeanRelationshipMetadata relMetadata = new MBeanRelationshipMetadata("/JMX", "Route", "Target", SUBSYSTEM, "Source");
        HashSet relations = new HashSet();
        relations.add(relMetadata);
        mbServer.invoke(DEPENDS_SERVICE1, "addRelationships", new Object[]{JMX_ROUTER, relations}, new String[]{ObjectName.class.getName(), Set.class.getName()});

        mbServer.invoke(SUBSYSTEM, "start", null, null);
        mbServer.invoke(LOADER, "start", null, null);
        mbServer.invoke(JMX_ROUTER, "start", null, null);
        return mbServer;
    }

    public static void stopRemoteServer(MBeanServer mbServer) throws Exception {
        mbServer.invoke(JMX_ROUTER, "stop", null, null);
        mbServer.invoke(LOADER, "stop", null, null);
        mbServer.invoke(SUBSYSTEM, "stop", null, null);
        stopLocalServer(mbServer);
    }

    public static void stopLocalServer(MBeanServer mbServer) throws Exception {
        MBeanServerFactory.releaseMBeanServer(mbServer);
    }

    public static Object registerGeronimoMbean(MBeanServer server, ObjectName objectName, String className, Object[] args, String[] types) throws Exception {
        GeronimoMBeanInfo mbeanInfo = EJBInfo.getGeronimoMBeanInfo(className, null);
        Object target = server.instantiate(className, args, types);
        mbeanInfo.setTarget(target); //how does deployGeronimoMBean do this?
        GeronimoMBean mbean = new GeronimoMBean();
        mbean.setMBeanInfo(mbeanInfo);
        server.registerMBean(mbean, objectName);
        return target;
    }
}