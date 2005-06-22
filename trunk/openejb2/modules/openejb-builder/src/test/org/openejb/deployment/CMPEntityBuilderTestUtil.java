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

package org.openejb.deployment;

import junit.framework.TestCase;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContext;
import org.apache.geronimo.xbeans.j2ee.EjbJarType;
import org.openejb.xbeans.ejbjar.OpenejbOpenejbJarType;
import org.tranql.cache.GlobalSchema;
import org.tranql.ejb.EJBSchema;
import org.tranql.sql.SQLSchema;

/**
 *
 *
 * @version $Revision$ $Date$
 *
 * */
public class CMPEntityBuilderTestUtil extends TestCase {

    private final CMPEntityBuilder delegate;

    public void testDummy() { }

    public CMPEntityBuilderTestUtil() {
        delegate = null;
    }
    
    public CMPEntityBuilderTestUtil(OpenEJBModuleBuilder builder) {
        this.delegate = new CMPEntityBuilder(builder);
    }
    
    public void buildCMPSchema(EARContext earContext, J2eeContext moduleJ2eeContext, EjbJarType ejbJar, OpenejbOpenejbJarType openejbEjbJar, ClassLoader cl, EJBSchema ejbSchema, SQLSchema sqlSchema, GlobalSchema globalSchema) throws DeploymentException {
        delegate.buildCMPSchema(earContext, moduleJ2eeContext, ejbJar, openejbEjbJar, cl, ejbSchema, sqlSchema, globalSchema);
    }
    
}