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
package org.openejb;

import java.util.Collection;
import java.util.Iterator;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GBean;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.gbean.GBeanContext;
import org.apache.geronimo.kernel.management.ManagedObject;

import org.openejb.entity.cmp.ConnectionProxyFactory;

import org.tranql.ejb.EJBSchema;
import org.tranql.schema.Schema;
import org.tranql.query.ConnectionFactoryDelegate;

/**
 *
 *
 * @version $Revision$ $Date$
 */
public class EJBModule implements GBean {
    private final Collection ejbs;
    private final EJBSchema ejbSchema;
    private final Schema sqlSchema;
    private final ConnectionFactoryDelegate delegate;
    private final ConnectionProxyFactory connectionFactory;

    public EJBModule(Collection ejbs, EJBSchema ejbSchema, Schema sqlSchema, ConnectionFactoryDelegate delegate, ConnectionProxyFactory connectionFactory) {
        this.ejbs = ejbs;
        this.ejbSchema = ejbSchema;
        this.sqlSchema = sqlSchema;
        this.delegate = delegate;
        this.connectionFactory = connectionFactory;
    }

    public String[] getEjbs() {
        String[] ejbsArray = new String[ejbs.size()];
        int i = 0;
        for (Iterator iterator = ejbs.iterator(); iterator.hasNext();) {
            ejbsArray[i++] = ((ManagedObject) iterator.next()).getObjectName();
        }
        return ejbsArray;
    }

    public void setGBeanContext(GBeanContext gBeanContext) {
    }

    public void doStart() throws WaitingException, Exception {
        if (delegate != null) {
            delegate.setConnectionFactory(connectionFactory.getProxy());
        }
    }

    public void doStop() throws WaitingException, Exception {
        if (delegate != null) {
            delegate.setConnectionFactory(null);
        }
    }

    public void doFail() {
        if (delegate != null) {
            delegate.setConnectionFactory(null);
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(EJBModule.class);
        infoFactory.addAttribute("EJBSchema", true);
        infoFactory.addAttribute("SQLSchema", true);
        infoFactory.addReference("ejbs", ManagedObject.class);
        infoFactory.addReference("ConnectionFactory", ConnectionProxyFactory.class);
        infoFactory.addAttribute("Delegate", true);
        infoFactory.setConstructor(
                new String[]{"ejbs", "EJBSchema", "SQLSchema", "Delegate", "ConnectionFactory"},
                new Class[]{Collection.class, EJBSchema.class, Schema.class, ConnectionFactoryDelegate.class, ConnectionProxyFactory.class}
        );
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
