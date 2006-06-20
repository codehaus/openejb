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

import org.apache.geronimo.naming.reference.ConfigurationAwareReference;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.GBeanNotFoundException;

import javax.naming.NameNotFoundException;

/**
 * @version $Revision$ $Date$
 */
public class EJBProxyReference extends ConfigurationAwareReference {
    public static EJBProxyReference createRemote(Artifact configurationId, AbstractNameQuery abstractNameQuery, boolean sessionBean, String homeInterfaceName, String remoteInterfaceName) {
        return new EJBProxyReference(configurationId, abstractNameQuery, sessionBean, homeInterfaceName, remoteInterfaceName, null, null, false);
    }

    public static EJBProxyReference createLocal(Artifact configurationId, AbstractNameQuery abstractNameQuery, boolean sessionBean, String localHomeInterfaceName, String localInterfaceName) {
        return new EJBProxyReference(configurationId, abstractNameQuery, sessionBean, null, null, localHomeInterfaceName, localInterfaceName, true);
    }

    private final boolean isSessionBean;
    private final String remoteInterfaceName;
    private final String homeInterfaceName;
    private final String localInterfaceName;
    private final String localHomeInterfaceName;
    private final boolean isLocal;

    private transient EJBProxyFactory proxyFactory;

    private EJBProxyReference(Artifact configurationId, AbstractNameQuery containerQuery, boolean sessionBean, String homeInterfaceName, String remoteInterfaceName, String localHomeInterfaceName, String localInterfaceName, boolean local) {
        super(configurationId, containerQuery);
        isSessionBean = sessionBean;
        this.remoteInterfaceName = remoteInterfaceName;
        this.homeInterfaceName = homeInterfaceName;
        this.localInterfaceName = localInterfaceName;
        this.localHomeInterfaceName = localHomeInterfaceName;
        isLocal = local;
    }

    public Object getContent() throws NameNotFoundException {
        EJBProxyFactory proxyFactory = getEJBProxyFactory();
        if (isLocal) {
            return proxyFactory.getEJBLocalHome();
        } else {
            return proxyFactory.getEJBHome();
        }
    }

    private EJBProxyFactory getEJBProxyFactory() throws NameNotFoundException {
        if (proxyFactory == null) {
            ClassLoader cl = getClassLoader();
            Class remoteInterface = loadClass(cl, remoteInterfaceName);
            Class homeInterface = loadClass(cl, homeInterfaceName);
            Class localInterface = loadClass(cl, localInterfaceName);
            Class localHomeInterface = loadClass(cl, localHomeInterfaceName);


            AbstractName configurationName;
            try {
                configurationName = resolveTargetName();
            } catch (GBeanNotFoundException e) {
                throw new NameNotFoundException("Could not resolve abstract name query " + abstractNameQueries + " in configuration " + getConfiguration().getId());
            }
            String containerId = configurationName.toURI().toString();
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
}