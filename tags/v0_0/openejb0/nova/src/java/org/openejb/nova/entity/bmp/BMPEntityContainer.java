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
package org.openejb.nova.entity.bmp;

import java.net.URI;

import org.apache.geronimo.cache.InstancePool;
import org.apache.geronimo.naming.java.ComponentContextInterceptor;
import org.apache.geronimo.remoting.DeMarshalingInterceptor;
import org.apache.geronimo.remoting.InterceptorRegistry;

import org.openejb.nova.AbstractEJBContainer;
import org.openejb.nova.entity.EntityContainerConfiguration;
import org.openejb.nova.util.SoftLimitedInstancePool;
import org.openejb.nova.dispatch.DispatchInterceptor;
import org.openejb.nova.entity.EntityClientContainerFactory;
import org.openejb.nova.entity.EntityInstanceFactory;
import org.openejb.nova.entity.EntityInstanceInterceptor;
import org.openejb.nova.transaction.TransactionContextInterceptor;

/**
 * @jmx.mbean
 *      extends="org.apache.geronimo.core.service.Container,org.apache.geronimo.kernel.management.StateManageable"
 *
 * @version $Revision$ $Date$
 */
public class BMPEntityContainer extends AbstractEJBContainer implements BMPEntityContainerMBean {
    private final String pkClassName;
    private Long remoteId;
    private InstancePool pool;

    public BMPEntityContainer(EntityContainerConfiguration config) {
        super(config);
        pkClassName = config.pkClassName;
    }

    protected void doStart() throws Exception {
        super.doStart();

        Class pkClass = classLoader.loadClass(pkClassName);

        BMPOperationFactory vopFactory = BMPOperationFactory.newInstance(beanClass);
        vtable = vopFactory.getVTable();

        pool = new SoftLimitedInstancePool(new EntityInstanceFactory(this), 1);

        TransactionContextInterceptor firstInterceptor = new TransactionContextInterceptor(txnManager);
        addInterceptor(firstInterceptor);
        addInterceptor(new ComponentContextInterceptor(componentContext));
        addInterceptor(new EntityInstanceInterceptor(pool));
        addInterceptor(new DispatchInterceptor(vtable));

        // set up server side remoting endpoint
        DeMarshalingInterceptor demarshaller = new DeMarshalingInterceptor();
        demarshaller.setClassloader(classLoader);
        demarshaller.setNext(firstInterceptor);
        remoteId = InterceptorRegistry.instance.register(demarshaller);

        // set up client containers
        URI target = uri.resolve("#" + remoteId);
        EntityClientContainerFactory clientFactory = new EntityClientContainerFactory(pkClass, vopFactory, target, homeInterface, remoteInterface, firstInterceptor, localHomeInterface, localInterface);
        if (homeClassName != null) {
            remoteClientContainer = clientFactory.getRemoteClient();
        }

        if (localHomeClassName != null) {
            localClientContainer = clientFactory.getLocalClient();
        }
    }

    protected void doStop() throws Exception {
        InterceptorRegistry.instance.unregister(remoteId);
        clearInterceptors();
        remoteClientContainer = null;
        localClientContainer = null;
        pool = null;
        super.doStop();
    }
}