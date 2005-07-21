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
package org.openejb.proxy;

import org.apache.geronimo.naming.reference.SimpleAwareReference;

/**
 * @version $Revision$ $Date$
 */
public class EJBProxyReference extends SimpleAwareReference {
    public static EJBProxyReference createRemote(String containerId, boolean sessionBean, String remoteInterfaceName, String homeInterfaceName) {
        return new EJBProxyReference(containerId, sessionBean, remoteInterfaceName, homeInterfaceName, null, null, false);
    }

    public static EJBProxyReference createLocal(String containerId, boolean sessionBean, String localInterfaceName, String localHomeInterfaceName) {
        return new EJBProxyReference(containerId, sessionBean, null, null, localInterfaceName, localHomeInterfaceName, true);
    }

    private final String containerId;
    private final boolean isSessionBean;
    private final String remoteInterfaceName;
    private final String homeInterfaceName;
    private final String localInterfaceName;
    private final String localHomeInterfaceName;
    private final boolean isLocal;

    private transient EJBProxyFactory proxyFactory;

    private EJBProxyReference(String containerId, boolean sessionBean, String remoteInterfaceName, String homeInterfaceName, String localInterfaceName, String localHomeInterfaceName, boolean local) {
        this.containerId = containerId;
        isSessionBean = sessionBean;
        this.remoteInterfaceName = remoteInterfaceName;
        this.homeInterfaceName = homeInterfaceName;
        this.localInterfaceName = localInterfaceName;
        this.localHomeInterfaceName = localHomeInterfaceName;
        isLocal = local;
    }

    public Object getContent() {
        EJBProxyFactory proxyFactory = getEJBProxyFactory();
        if (isLocal) {
            return proxyFactory.getEJBLocalHome();
        } else {
            return proxyFactory.getEJBHome();
        }
    }

    private EJBProxyFactory getEJBProxyFactory() {
        if (proxyFactory == null) {
            ClassLoader cl = getClassLoader();
            Class remoteInterface = loadClass(cl, remoteInterfaceName);
            Class homeInterface = loadClass(cl, homeInterfaceName);
            Class localInterface = loadClass(cl, localInterfaceName);
            Class localHomeInterface = loadClass(cl, localHomeInterfaceName);

            proxyFactory = new EJBProxyFactory(containerId,
                    isSessionBean,
                    remoteInterface,
                    homeInterface,
                    localInterface,
                    localHomeInterface);
        }
        return proxyFactory;
    }

    private Class loadClass(ClassLoader cl, String name) {
        if (name == null) {
            return null;
        }
        try {
            return cl.loadClass(name);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("ejb" + (isLocal ? "-local" : "") + "-ref class not found: " + name);
        }
    }

    public String getContainerId() {
        return containerId;
    }
}