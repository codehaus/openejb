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
package org.openejb.nova.security;

import java.security.Permission;
import javax.security.jacc.EJBMethodPermission;

import org.openejb.nova.EJBInvocationType;
import org.openejb.nova.dispatch.MethodSignature;

/**
 * @version $Revision$ $Date$
 */
public final class PermissionManager {
    private final Permission[][] permissions = new Permission[EJBInvocationType.MAX_ORDINAL][];

    public PermissionManager(String ejbName, MethodSignature[] signatures) {
        permissions[EJBInvocationType.HOME.getOrdinal()] = mapPermissions(ejbName, "Home", signatures);
        permissions[EJBInvocationType.REMOTE.getOrdinal()] = mapPermissions(ejbName, "Remote", signatures);
        permissions[EJBInvocationType.LOCALHOME.getOrdinal()] = mapPermissions(ejbName, "LocalHome", signatures);
        permissions[EJBInvocationType.LOCAL.getOrdinal()] = mapPermissions(ejbName, "Local", signatures);
        permissions[EJBInvocationType.WEB_SERVICE.getOrdinal()] = mapPermissions(ejbName, "ServiceEndpoint", signatures);
    }

    public Permission getPermission(EJBInvocationType invocationType, int operationIndex) {
        return permissions[invocationType.getOrdinal()][operationIndex];
    }

    private static Permission[] mapPermissions(String ejbName, String intfName, MethodSignature[] signatures) {
        Permission[] permissions = new Permission[signatures.length];
        for (int index = 0; index < signatures.length; index++) {
            MethodSignature signature = signatures[index];
            permissions[index] = new EJBMethodPermission(ejbName, signature.getMethodName(), intfName, signature.getParameterTypes());
        }
        return permissions;
    }
}
