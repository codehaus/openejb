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
package org.openejb.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.lang.reflect.Constructor;

import org.apache.geronimo.kernel.ClassLoading;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GBean;
import org.apache.geronimo.gbean.GBeanContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.openejb.ContainerIndex;
import org.openejb.OpenEJB;

/**
 * @version $Revision$ $Date$
 */
public class SimpleSocketService implements SocketService, GBean {
    private static final Log log = LogFactory.getLog(SimpleSocketService.class);
    private final ServerService server;

    public SimpleSocketService(String serviceClassName, InetAddress[] onlyFrom, ContainerIndex containerIndex) throws Exception {
        ServerService service;

        Class serviceClass = ClassLoading.loadClass(serviceClassName);
        if (!serviceClass.isAssignableFrom(serviceClass)) {
            throw new ServiceException("Server service class does not implement " + ServerService.class.getName() + ": " + serviceClassName);
        }
        try {
            Constructor constructor = serviceClass.getConstructor(new Class[] { ContainerIndex.class });
            service = (ServerService) constructor.newInstance(new Object[] {containerIndex});
        } catch (Exception e) {
            throw new ServiceException("Error constructing server service class", e);
        }

        service = new ServiceLogger(service);
        service = new ServiceAccessController(service, onlyFrom);
        service = new ServicePool(service);
        server = service;

        // TODO Horrid hack, the concept needs to survive somewhere
        if (OpenEJB.getApplicationServer() == null){
            OpenEJB.setApplicationServer(new ServerFederation());
        }
    }

    public void setGBeanContext(GBeanContext context) {
    }

    public synchronized void doStart() throws ServiceException {
        server.start();
    }

    public synchronized void doStop() throws ServiceException {
        server.stop();
    }

    public void doFail() {
        try {
            server.stop();
        } catch (ServiceException e) {
            log.error("Could not clean up simple socket service");
        }
    }

    public void service(Socket socket) throws ServiceException, IOException {
        server.service(socket);
    }

    public String getName() {
        return server.getName();
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(SimpleSocketService.class);

        infoFactory.addAttribute("ServiceClassName", String.class, true);
        infoFactory.addAttribute("OnlyFrom", InetAddress[].class, true);
        infoFactory.addAttribute("Name", String.class, false);

        infoFactory.addReference("ContainerIndex", ContainerIndex.class);

        infoFactory.addInterface(SocketService.class);

        infoFactory.setConstructor(new String[]{"ServiceClassName", "OnlyFrom", "ContainerIndex"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
