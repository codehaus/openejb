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
package org.openejb.nova.slsb;

import java.net.URI;

import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.remoting.InterVMRoutingInterceptor;
import org.apache.geronimo.remoting.InterceptorRegistry;
import org.apache.geronimo.remoting.IntraVMRoutingInterceptor;
import org.apache.geronimo.remoting.MarshalingInterceptor;
import org.apache.geronimo.remoting.transport.NullTransportInterceptor;
import org.apache.geronimo.remoting.transport.RemoteTransportInterceptor;

import org.openejb.nova.ClientContainerFactory;
import org.openejb.nova.EJBLocalClientContainer;
import org.openejb.nova.EJBRemoteClientContainer;
import org.openejb.nova.dispatch.MethodSignature;

/**
 *
 *
 * @version $Revision$ $Date$
 */
public class StatelessClientContainerFactory implements ClientContainerFactory {
    private final StatelessLocalClientContainer localContainer;
    private final StatelessRemoteClientContainer remoteContainer;

    public StatelessClientContainerFactory(MethodSignature[] signatures, URI uri, Class home, Class remote, Interceptor localEndpoint, Class localHome, Class local) {
        if (localHome != null) {
            localContainer = new StatelessLocalClientContainer(localEndpoint, signatures, localHome, local);
        } else {
            localContainer = null;
        }

        if (home != null) {
            Long remoteId = Long.valueOf(uri.getFragment());
            RemoteTransportInterceptor transport = new RemoteTransportInterceptor(uri);
            Interceptor clientStack = InterceptorRegistry.instance.lookup(remoteId);
            clientStack = new NullTransportInterceptor(clientStack);
            clientStack = new MarshalingInterceptor(clientStack);
            clientStack = new IntraVMRoutingInterceptor(clientStack, remoteId, true);
            clientStack = new InterVMRoutingInterceptor(transport, clientStack);
            remoteContainer = new StatelessRemoteClientContainer(clientStack, signatures, home, remote);
        } else {
            remoteContainer = null;
        }
    }

    public EJBLocalClientContainer getLocalClient() {
        return localContainer;
    }


    public EJBRemoteClientContainer getRemoteClient() {
        return remoteContainer;
    }
}