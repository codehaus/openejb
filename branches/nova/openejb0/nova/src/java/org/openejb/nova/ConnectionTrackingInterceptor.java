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

import java.util.Set;

import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.core.service.InvocationResult;
import org.apache.geronimo.core.service.Invocation;
import org.apache.geronimo.connector.outbound.connectiontracking.TrackedConnectionAssociator;
import org.apache.geronimo.transaction.TransactionContext;
import org.apache.geronimo.transaction.InstanceContext;

/**
 *
 *
 * @version $Revision$ $Date$
 *
 * */
public class ConnectionTrackingInterceptor implements Interceptor {

    private final Interceptor next;
    private final TrackedConnectionAssociator trackedConnectionAssociator;
    private final Set unshareableResources;

    public ConnectionTrackingInterceptor(final Interceptor next,
                                         final TrackedConnectionAssociator trackedConnectionAssociator,
                                         final Set unshareableResources) {
        this.next = next;
        this.trackedConnectionAssociator = trackedConnectionAssociator;
        this.unshareableResources = unshareableResources;
    }

    public InvocationResult invoke(Invocation invocation) throws Throwable {
        EJBInvocation ejbInvocation = (EJBInvocation)invocation;
        InstanceContext enteringInstanceContext = ejbInvocation.getEJBInstanceContext();
        TransactionContext enteringTransactionContext = ejbInvocation.getTransactionContext();
        Set oldUnshareableResources = trackedConnectionAssociator.setUnshareableResources(unshareableResources);
        InstanceContext leavingInstanceContext = trackedConnectionAssociator.enter(enteringInstanceContext);
        TransactionContext leavingTransactionContext = trackedConnectionAssociator.setTransactionContext(enteringTransactionContext);
        try {
            return next.invoke(invocation);
        } finally {
            trackedConnectionAssociator.exit(leavingInstanceContext, unshareableResources);
            trackedConnectionAssociator.resetTransactionContext(leavingTransactionContext);
            trackedConnectionAssociator.setUnshareableResources(oldUnshareableResources);
        }
    }
}