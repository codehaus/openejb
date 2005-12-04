/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
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
 *    (http://openejb.sf.net/).
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
 * Copyright 2004-2005 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.corba;

import java.util.ArrayList;
import java.util.Properties;

import EDU.oswego.cs.dl.util.concurrent.Executor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.security.SecurityService;

import org.openejb.corba.security.config.ConfigAdapter;
import org.openejb.corba.security.config.tss.TSSConfig;
import org.openejb.corba.util.Util;


/**
 * @version $Revision$ $Date$
 */
public class CORBABean implements GBeanLifecycle, ORBRef {

    private final Log log = LogFactory.getLog(CORBABean.class);

    private final ClassLoader classLoader;
    private final Executor threadPool;
    private final ConfigAdapter configAdapter;
    private TSSConfig tssConfig;
    private ORB orb;
    private POA rootPOA;
    private ArrayList args = new ArrayList();
    private Properties props = new Properties();

    public CORBABean() {
        this.classLoader = null;
        this.threadPool = null;
        this.configAdapter = null;
    }

    public CORBABean(String configAdapter, ClassLoader classLoader, Executor threadPool, SecurityService securityService, SunNameService nameService) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        this.classLoader = classLoader;
        this.threadPool = threadPool;
        if (configAdapter != null) {
            this.configAdapter = (ConfigAdapter) classLoader.loadClass(configAdapter).newInstance();
        } else {
            this.configAdapter = null;
        }
        //security service included to force start order.
        //name service included to force start order.
    }

    public TSSConfig getTssConfig() {
        return tssConfig;
    }

    public void setTssConfig(TSSConfig config) {
        if (config == null) config = new TSSConfig();
        this.tssConfig = config;
    }

    public ORB getORB() {
        return orb;
    }

    public POA getRootPOA() {
        return rootPOA;
    }

    public ArrayList getArgs() {
        return args;
    }

    public void setArgs(ArrayList args) {
        if (args == null) args = new ArrayList();
        this.args = args;
    }

    public Properties getProps() {
        return props;
    }

    public void setProps(Properties props) {
        if (props == null) props = new Properties();
        this.props = props;
    }

    public void doStart() throws Exception {

        ClassLoader savedLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(classLoader);

            if (configAdapter != null) {
                orb = ORB.init(configAdapter.translateToArgs(tssConfig, args), configAdapter.translateToProps(tssConfig, props));
                configAdapter.postProcess(tssConfig, orb);
            } else {
                orb = ORB.init((String[]) args.toArray(new String[args.size()]), props);
            }

            Util.setORB(orb);

            org.omg.CORBA.Object obj = orb.resolve_initial_references("RootPOA");

            rootPOA = POAHelper.narrow(obj);

            threadPool.execute(new Runnable() {
                public void run() {
                    orb.run();
                }
            });
        } finally {
            Thread.currentThread().setContextClassLoader(savedLoader);
        }

        log.debug("Started CORBABean");
    }

    public void doStop() throws Exception {
        orb.destroy();
        log.debug("Stopped CORBABean");
    }

    public void doFail() {
        log.warn("Failed CORBABean");
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(CORBABean.class, NameFactory.CORBA_SERVICE);

        infoFactory.addAttribute("configAdapter", String.class, true);
        infoFactory.addAttribute("tssConfig", TSSConfig.class, true);
        infoFactory.addAttribute("args", ArrayList.class, true);
        infoFactory.addAttribute("props", Properties.class, true);

        infoFactory.addAttribute("ORB", ORB.class, false);
        infoFactory.addAttribute("rootPOA", POA.class, false);

        infoFactory.addAttribute("classLoader", ClassLoader.class, false);
        infoFactory.addReference("ThreadPool", Executor.class, NameFactory.GERONIMO_SERVICE);
        infoFactory.addReference("SecurityService", SecurityService.class, NameFactory.GERONIMO_SERVICE);
        infoFactory.addReference("NameService", SunNameService.class, NameFactory.CORBA_SERVICE);

        infoFactory.setConstructor(new String[]{"configAdapter", "classLoader", "ThreadPool", "SecurityService", "NameService"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
