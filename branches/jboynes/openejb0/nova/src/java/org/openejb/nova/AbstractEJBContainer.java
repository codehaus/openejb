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
package org.openejb.nova;

import java.net.URI;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.transaction.TransactionManager;

import org.apache.geronimo.core.service.AbstractRPCContainer;
import org.apache.geronimo.ejb.metadata.TransactionDemarcation;
import org.apache.geronimo.naming.java.ReadOnlyContext;

import org.openejb.nova.dispatch.VirtualOperation;
import org.openejb.nova.transaction.EJBUserTransaction;

/**
 *
 *
 * @version $Revision$ $Date$
 */
public abstract class AbstractEJBContainer extends AbstractRPCContainer implements EJBContainer {
    protected final URI uri;
    protected final String ejbClassName;
    protected final String homeClassName;
    protected final String localHomeClassName;
    protected final String remoteClassName;
    protected final String localClassName;
    protected final TransactionDemarcation txnDemarcation;
    protected final TransactionManager txnManager;
    protected final ReadOnlyContext componentContext;
    protected final EJBUserTransaction userTransaction;

    protected ClassLoader classLoader;
    protected Class beanClass;
    protected VirtualOperation[] vtable;

    protected EJBRemoteClientContainer remoteClientContainer;
    protected Class homeInterface;
    protected Class remoteInterface;

    protected EJBLocalClientContainer localClientContainer;
    protected Class localHomeInterface;
    protected Class localInterface;

    public AbstractEJBContainer(EJBContainerConfiguration config) {
        uri = config.uri;
        ejbClassName = config.beanClassName;
        homeClassName = config.homeInterfaceName;
        remoteClassName = config.remoteInterfaceName;
        localHomeClassName = config.localHomeInterfaceName;
        localClassName = config.localInterfaceName;
        txnDemarcation = config.txnDemarcation;
        txnManager = config.txnManager;
        userTransaction = config.userTransaction;
        componentContext = config.componentContext;
    }

    /* Start the Component
     * @see org.apache.geronimo.core.service.AbstractManagedObject#doStart()
     */
    protected void doStart() throws Exception {
        super.doStart();
        classLoader = Thread.currentThread().getContextClassLoader();
        beanClass = classLoader.loadClass(ejbClassName);

        if (homeClassName != null) {
            homeInterface = classLoader.loadClass(homeClassName);
            remoteInterface = classLoader.loadClass(remoteClassName);
        } else {
            homeInterface = null;
            remoteInterface = null;
        }
        if (localHomeClassName != null) {
            localHomeInterface = classLoader.loadClass(localHomeClassName);
            localInterface = classLoader.loadClass(localClassName);
        } else {
            localHomeInterface = null;
            localInterface = null;
        }
    }

    /* Stop the Component
     * @see org.apache.geronimo.core.service.AbstractManagedObject#doStop()
     */
    protected void doStop() throws Exception {
        homeInterface = null;
        remoteInterface = null;
        localHomeInterface = null;
        localInterface = null;
        beanClass = null;
        super.doStop();
    }

    public Class getBeanClass() {
        return beanClass;
    }

    public EJBHome getEJBHome() {
        return remoteClientContainer.getEJBHome();
    }

    public EJBObject getEJBObject(Object primaryKey) {
        return remoteClientContainer.getEJBObject(primaryKey);
    }

    public EJBLocalHome getEJBLocalHome() {
        return localClientContainer.getEJBLocalHome();
    }

    public EJBLocalObject getEJBLocalObject(Object primaryKey) {
        return localClientContainer.getEJBLocalObject(primaryKey);
    }

    public TransactionDemarcation getDemarcation() {
        return txnDemarcation;
    }

    public EJBUserTransaction getUserTransaction() {
        return userTransaction;
    }

    public ReadOnlyContext getComponentContext() {
        return componentContext;
    }

    /**
     * Return the name of this EJB's implementation class
     * @return the name of this EJB's implementation class
     * @jmx.managed-attribute
     */
    public String getBeanClassName() {
        return ejbClassName;
    }

    /**
     * Return the name of this EJB's home interface class
     * @return the name of this EJB's home interface class
     * @jmx.managed-attribute
     */
    public String getHomeClassName() {
        return homeClassName;
    }

    /**
     * Return the name of this EJB's remote component interface class
     * @return the name of this EJB's remote component interface class
     * @jmx.managed-attribute
     */
    public String getRemoteClassName() {
        return remoteClassName;
    }

    /**
     * Return the name of this EJB's local home class
     * @return the name of this EJB's local home class
     * @jmx.managed-attribute
     */
    public String getLocalHomeClassName() {
        return localHomeClassName;
    }

    /**
     * Return the name of this EJB's local component interface class
     * @return the name of this EJB's local component interface class
     * @jmx.managed-attribute
     */
    public String getLocalClassName() {
        return localClassName;
    }
}