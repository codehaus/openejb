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
package org.openejb.security;

import java.rmi.AccessException;
import java.security.AccessControlContext;
import java.security.AccessControlException;
import java.security.Permission;
import javax.ejb.AccessLocalException;
import javax.security.auth.Subject;
import javax.security.jacc.PolicyContext;

import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.core.service.Invocation;
import org.apache.geronimo.core.service.InvocationResult;
import org.apache.geronimo.security.ContextManager;

import org.openejb.EJBInvocation;


/**
 * An interceptor that performs the JACC EJB security check before continuing
 * on w/ the interceptor stack call.
 * @version $Revision$ $Date$
 */
public final class EJBSecurityInterceptor implements Interceptor {
    private final Interceptor next;
    private final String contextId;
    private final PermissionManager permissionManager;

    public EJBSecurityInterceptor(Interceptor next, Object contextId, PermissionManager permissionManager) {
        this.next = next;
        //TODO go back to the commented version when possible
//        this.contextId = contextId.toString();
        this.contextId = contextId.toString().replaceAll("[, ]", "_");
        this.permissionManager = permissionManager;
    }

    public InvocationResult invoke(Invocation invocation) throws Throwable {
        EJBInvocation ejbInvocation = ((EJBInvocation) invocation);

        Subject subject = ContextManager.getCurrentCaller();
        String oldPolicyContextID = PolicyContext.getContextID();
        try {
            PolicyContext.setContextID(contextId);
            AccessControlContext accessContext = ContextManager.getCurrentContext();
            if (accessContext != null) {
                Permission permission = permissionManager.getPermission(ejbInvocation.getType(), ejbInvocation.getMethodIndex());
                if (permission != null) accessContext.checkPermission(permission);
            }

            ContextManager.setCurrentCaller(ContextManager.getNextCaller());

            return next.invoke(invocation);
        } catch (AccessControlException e) {
            if (ejbInvocation.getType().isLocal()) {
                throw new AccessLocalException(e.getMessage());
            } else {
                throw new AccessException(e.getMessage());
            }
        } finally {
            PolicyContext.setContextID(oldPolicyContextID);
            ContextManager.setCurrentCaller(subject);
        }
    }
}
