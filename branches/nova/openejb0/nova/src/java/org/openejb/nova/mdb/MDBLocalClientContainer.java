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

import java.lang.reflect.Method;
import javax.resource.ResourceException;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.transaction.xa.XAResource;

import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.core.service.InvocationResult;

import net.sf.cglib.reflect.FastClass;
import org.openejb.nova.ClientContainer;
import org.openejb.nova.EJBInvocation;
import org.openejb.nova.EJBInvocationType;
import org.openejb.nova.proxy.EJBProxyFactory;
import org.openejb.nova.dispatch.MethodSignature;
import org.openejb.nova.proxy.EJBProxy;
import org.openejb.nova.proxy.EJBProxyHandler;
import org.openejb.nova.proxy.EBJProxyHelper;

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
    public static final int BEFORE_DELIVERY = 0;
    public static final int AFTER_DELIVERY = 1;

    private final Interceptor firstInterceptor;
    private final int[] operationMap;
    private final EJBProxyFactory objectFactory;
    private final FastClass fastClass;

    /**
     * Constructor used to initialize the ClientContainer.
     * @param signatures the signatures of the virtual methods
     * @param mdbInterface the class of the MDB's messaging interface (e.g. javax.jmx.MessageListner)
     */
    public MDBLocalClientContainer(Interceptor firstInterceptor, MethodSignature[] signatures, Class mdbInterface) {
        this.firstInterceptor = firstInterceptor;

        objectFactory = new EJBProxyFactory(MDBMessageEndpointProxy.class, new Class[]{mdbInterface, MessageEndpoint.class});
        operationMap = EBJProxyHelper.getOperationMap(EJBInvocationType.LOCAL, objectFactory.getType(), signatures);

        fastClass = FastClass.create(objectFactory.getType());
    }

    public MessageEndpoint getMessageEndpoint(XAResource resource) {
        // @todo should this be EJBInvocationType.MESSAGE_ENDPOINT?
        EJBProxyHandler objectHandler = new EJBProxyHandler(this, EJBInvocationType.LOCAL, operationMap, resource);
        return (MessageEndpoint) objectFactory.create(objectHandler, new Class[]{EJBProxyHandler.class}, new Object[]{objectHandler});
    }


    public InvocationResult invoke(EJBInvocation ejbInvocation) throws Throwable {
        return firstInterceptor.invoke(ejbInvocation);
    }

    /**
     * Base class for MessageEndpoint invocations. Handles operations which can
     * be performed directly by the proxy.
     */
    public static class MDBMessageEndpointProxy extends EJBProxy implements MessageEndpoint {
        public MDBMessageEndpointProxy(EJBProxyHandler handler) {
            super(handler);
        }

        public XAResource getAdapterXAResource() {
            return (XAResource)handler.getId();
        }


        /**
         * @see javax.resource.spi.endpoint.MessageEndpoint#beforeDelivery(java.lang.reflect.Method)
         */
        public void beforeDelivery(Method method) throws NoSuchMethodException, ResourceException {
            //translate the method into an index.
            //construct invocation and call.
            MDBLocalClientContainer container = (MDBLocalClientContainer)handler.getContainer();
            int methodIndex = container.fastClass.getIndex(method.getName(), method.getParameterTypes());
            int vopIndex = container.operationMap[methodIndex];
            Object[] args = new Object[]{new Integer(vopIndex)};
            try {
                MDBInvocation invocation = new MDBInvocationImpl(EJBInvocationType.MESSAGE_ENDPOINT, BEFORE_DELIVERY, args, (XAResource)handler.getId());
                container.invoke(invocation);
            } catch (Throwable throwable) {
                if (throwable instanceof ResourceException) {
                    throw (ResourceException) throwable;
                }
                throw new ResourceException(throwable);
            }

        }

        /**
         * @see javax.resource.spi.endpoint.MessageEndpoint#afterDelivery()
         */
        public void afterDelivery() throws ResourceException {
            Object[] args = new Object[]{};
            try {
                MDBLocalClientContainer container = (MDBLocalClientContainer)handler.getContainer();
                MDBInvocation invocation = new MDBInvocationImpl(EJBInvocationType.MESSAGE_ENDPOINT, AFTER_DELIVERY, args, (XAResource)handler.getId());
                container.invoke(invocation);
            } catch (Throwable throwable) {
                if (throwable instanceof ResourceException) {
                    throw (ResourceException) throwable;
                }
                throw new ResourceException(throwable);
            }

        }

        /**
         * @see javax.resource.spi.endpoint.MessageEndpoint#release()
         */
        public void release() {
            // TODO Auto-generated method stub

        }
    }
}
