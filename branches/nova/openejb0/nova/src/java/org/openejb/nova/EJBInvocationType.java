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

import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * Type-safe enum describing
 *
 *
 * @version $Revision$ $Date$
 */
public final class EJBInvocationType implements Serializable {
    private final transient String name;
    private final transient boolean local;
    private final transient int transactionPolicyKey;

    private EJBInvocationType(String name, boolean local, int transactionPolicyKey) {
        this.name = name;
        this.local = local;
        this.transactionPolicyKey = transactionPolicyKey;
    }

    /**
     * Keep these in the same order since MethodHelper relies in the ordinal number of the enum.
     */
    public static final EJBInvocationType REMOTE = new EJBInvocationType("Remote", false, 0);
    public static final EJBInvocationType HOME = new EJBInvocationType("Home", false, 0);
    public static final EJBInvocationType LOCAL = new EJBInvocationType("Local", true, 1);
    public static final EJBInvocationType LOCALHOME = new EJBInvocationType("LocalHome", true, 1);
    public static final EJBInvocationType WEB_SERVICE = new EJBInvocationType("Web-Service", false, 2);
    public static final EJBInvocationType TIMEOUT = new EJBInvocationType("ejbTimeout", true, 3);
    //MESSAGE_ENDPOINT is only for beforeDelivery/afterDelivery.  The actual bean methods use LOCAL
    public static final EJBInvocationType MESSAGE_ENDPOINT = new EJBInvocationType("Message-Endpoint-Delivery", true, 4);

    private static final EJBInvocationType[] VALUES = {
        REMOTE, HOME, LOCAL, LOCALHOME, WEB_SERVICE, TIMEOUT, MESSAGE_ENDPOINT
    };


    public boolean isLocal() {
        return local;
    }

    public String toString() {
        return name;
    }

    public int getTransactionPolicyKey() {
        return transactionPolicyKey;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public static int getMaxTransactionPolicyKey() {
        return maxTransactionPolicyKey;
    }

    private static int nextOrdinal;
    private final int ordinal = nextOrdinal++;

    Object readResolve() throws ObjectStreamException {
        return VALUES[ordinal];
    }

    private static int maxTransactionPolicyKey = 0;

    // verify that all are defined and the ids match up
    static {
        assert (VALUES.length == nextOrdinal) : "VALUES is missing a value";
        for (int i = 0; i < VALUES.length; i++) {
            EJBInvocationType value = VALUES[i];
            assert (value.ordinal == i) : "Ordinal mismatch for " + value;
            if (maxTransactionPolicyKey < value.transactionPolicyKey) {
                maxTransactionPolicyKey = value.transactionPolicyKey;
            }
        }
    }
}
