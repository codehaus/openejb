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
package org.openejb.nova.mdb;

import javax.resource.spi.endpoint.MessageEndpoint;
import javax.transaction.xa.XAResource;

import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.core.service.InvocationResult;

import org.openejb.nova.ClientContainer;
import org.openejb.nova.EJBInvocation;
import org.openejb.nova.EJBInvocationType;
import org.openejb.nova.dispatch.MethodSignature;
import org.openejb.nova.proxy.EJBProxyFactory;
import org.openejb.nova.proxy.EJBProxyHandler;
import org.openejb.nova.proxy.EJBProxyHelper;

/**
 * Container for the local interface of a Message Driven Bean.
 * This container owns implementations of EJBLocalHome and EJBLocalObject
 * that can be used by a client in the same classloader as the server.
 *
 * The implementation of the interfaces is generated using cglib FastClass
 * proxies to avoid the overhead of native Java reflection.
 *
 * @version $Revision$ $Date$
 */
public class MDBLocalClientContainer implements ClientContainer {
    private final MDBContainer container;
    private final int[] operationMap;
    private final EJBProxyFactory objectFactory;

    /**
     * Constructor used to initialize the ClientContainer.
     * @param signatures the signatures of the virtual methods
     * @param mdbInterface the class of the MDB's messaging interface (e.g. javax.jmx.MessageListner)
     */
    public MDBLocalClientContainer(MDBContainer container, MethodSignature[] signatures, Class mdbInterface) {
        this.container = container;

        objectFactory = new EJBProxyFactory(MessageEndpointProxy.class, new Class[]{mdbInterface, MessageEndpoint.class});
        operationMap = EJBProxyHelper.getOperationMap(EJBInvocationType.LOCAL, objectFactory.getType(), signatures);
    }

    public MessageEndpoint getMessageEndpoint(XAResource resource) {
        EJBProxyHandler objectHandler = new EJBProxyHandler(this, EJBInvocationType.LOCAL, operationMap, resource);
        return (MessageEndpoint) objectFactory.create(objectHandler, new Class[]{EJBProxyHandler.class, MDBContainer.class}, new Object[]{objectHandler, container});
    }


    public InvocationResult invoke(EJBInvocation ejbInvocation) throws Throwable {
        return container.invoke(ejbInvocation);
    }
}
