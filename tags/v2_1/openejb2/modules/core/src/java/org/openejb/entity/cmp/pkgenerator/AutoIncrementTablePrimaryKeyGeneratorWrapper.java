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
package org.openejb.entity.cmp.pkgenerator;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.connector.outbound.ConnectionFactorySource;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.tranql.cache.CacheRow;
import org.tranql.cache.DuplicateIdentityException;
import org.tranql.cache.InTxCache;
import org.tranql.identity.GlobalIdentity;
import org.tranql.pkgenerator.AutoIncrementTablePrimaryKeyGenerator;
import org.tranql.pkgenerator.PrimaryKeyGenerator;
import org.tranql.pkgenerator.PrimaryKeyGeneratorException;
import org.tranql.ql.QueryBindingImpl;
import org.tranql.sql.jdbc.binding.BindingFactory;

/**
 * @version $Revision$ $Date$
 */
public class AutoIncrementTablePrimaryKeyGeneratorWrapper implements PrimaryKeyGenerator, GBeanLifecycle {
    private static final Log log = LogFactory.getLog(AutoIncrementTablePrimaryKeyGeneratorWrapper.class);

    private final ConnectionFactorySource connectionFactoryWrapper;
    private final String sql;
    private final Class returnType;
    private PrimaryKeyGenerator delegate;

    public AutoIncrementTablePrimaryKeyGeneratorWrapper(ConnectionFactorySource connectionFactoryWrapper, String sql, Class returnType) {
        this.connectionFactoryWrapper = connectionFactoryWrapper;
        this.sql = sql;
        this.returnType = returnType;
    }

    public void doStart() throws Exception {
        DataSource dataSource = (DataSource) connectionFactoryWrapper.$getResource();
        delegate = new AutoIncrementTablePrimaryKeyGenerator(dataSource, sql, BindingFactory.getResultBinding(1, new QueryBindingImpl(0, returnType)));
    }

    public void doStop() throws Exception {
        delegate = null;
    }

    public void doFail() {
        delegate = null;
    }

    public Object getNextPrimaryKey(CacheRow cacheRow) throws PrimaryKeyGeneratorException {
        return delegate.getNextPrimaryKey(cacheRow);
    }

    public CacheRow updateCache(InTxCache cache, GlobalIdentity id, CacheRow cacheRow) throws DuplicateIdentityException {
        return delegate.updateCache(cache, id, cacheRow);
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(AutoIncrementTablePrimaryKeyGeneratorWrapper.class);
        infoFactory.addInterface(PrimaryKeyGenerator.class);

        infoFactory.addReference("ManagedConnectionFactoryWrapper", ConnectionFactorySource.class, NameFactory.JCA_MANAGED_CONNECTION_FACTORY);
        infoFactory.addAttribute("sql", String.class, true);
        infoFactory.addAttribute("returnType", Class.class, true);

        infoFactory.setConstructor(new String[]{"ManagedConnectionFactoryWrapper", "sql", "returnType"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}