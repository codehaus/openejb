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

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.HashSet;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import junit.framework.TestCase;

import org.openejb.nova.dispatch.MethodSignature;
import org.openejb.nova.dispatch.VirtualOperation;

/**
 *
 *
 * @version $Revision$ $Date$
 */
public class OperationFactoryTest extends TestCase {
    private StatelessOperationFactory factory;

    public void testSignatures() throws Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        MethodSignature[] signatures = factory.getSignatures();
        assertEquals(5, signatures.length);
        for (int i = 0; i < signatures.length; i++) {
            MethodSignature signature = signatures[i];
        }
        assertEquals(new HashSet(Arrays.asList(EJB1.sigs)), new HashSet(Arrays.asList(signatures)));
    }

    public void testOperations() throws Exception {
        VirtualOperation[] vtable = factory.getVTable();
        assertEquals(5, vtable.length);
        for (int i = 0; i < vtable.length; i++) {
            VirtualOperation virtualOperation = vtable[i];
            assertTrue(virtualOperation instanceof BusinessMethod);
        }
    }

    protected void setUp() throws Exception {
        super.setUp();
        factory = StatelessOperationFactory.newInstance(EJB1.class);
    }

    private static class EJB1 implements SessionBean {
        public static final MethodSignature[] sigs = {
            new MethodSignature("method1", new String[]{}),
            new MethodSignature("method2", new String[]{"int", "java.lang.Integer"}),
            new MethodSignature("method3", new String[]{"java.lang.Integer"}),
            new MethodSignature("method4", new String[]{"[I"}),
            new MethodSignature("method5", new String[]{"[[Ljava.lang.Integer;"}),
        };

        public void method1() {
        }

        public void method2(int i, Integer j) {
        }

        public void method3(Integer i) {
        }

        public void method4(int[] i) {
        }

        public void method5(Integer[][] i) {
        }

        public void ejbActivate() throws EJBException, RemoteException {
        }

        public void ejbPassivate() throws EJBException, RemoteException {
        }

        public void ejbRemove() throws EJBException, RemoteException {
        }

        public void setSessionContext(SessionContext ctx) throws EJBException, RemoteException {
        }
    }
}
