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

import java.net.URI;
import java.util.ArrayList;
import java.util.Properties;

import EDU.oswego.cs.dl.util.concurrent.Executor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Policy;
import org.omg.CORBA.SetOverrideType;
import org.omg.CORBA.UserException;
import org.omg.CORBA.portable.ObjectImpl;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableInterceptor.ClientRequestInfo;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.transaction.context.TransactionContextManager;

import org.openejb.corba.security.ClientPolicyFactory;
import org.openejb.corba.security.config.ConfigAdapter;
import org.openejb.corba.security.config.css.CSSConfig;
import org.openejb.corba.transaction.ClientTransactionPolicyConfig;
import org.openejb.corba.transaction.ClientTransactionPolicyFactory;
import org.openejb.corba.transaction.nodistributedtransactions.NoDTxClientTransactionPolicyConfig;


/**
 * @version $Revision$ $Date$
 */
public class CSSBean implements GBeanLifecycle {

    private final static Log log = LogFactory.getLog(CSSBean.class);

    private final ClassLoader classLoader;
    private final Executor threadPool;
    private final ConfigAdapter configAdapter;
    private final TransactionContextManager transactionContextManager;
    private String description;
    private CSSConfig nssConfig;
    private CSSConfig cssConfig;
    private ORB nssORB;
    private ORB cssORB;
    private ArrayList nssArgs;
    private ArrayList cssArgs;
    private Properties nssProps;
    private Properties cssProps;
    private ClientContext context;

    public CSSBean() {
        this.classLoader = null;
        this.threadPool = null;
        this.configAdapter = null;
        this.transactionContextManager = null;
    }

    public CSSBean(String configAdapter, Executor threadPool, TransactionContextManager transactionContextManager, ClassLoader classLoader) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        this.classLoader = classLoader;
        this.threadPool = threadPool;
        this.transactionContextManager = transactionContextManager;
        this.configAdapter = (ConfigAdapter) classLoader.loadClass(configAdapter).newInstance();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public CSSConfig getNssConfig() {
        return nssConfig;
    }

    public void setNssConfig(CSSConfig nssConfig) {
        this.nssConfig = nssConfig;
    }

    public CSSConfig getCssConfig() {
        return cssConfig;
    }

    public void setCssConfig(CSSConfig config) {
        if (config == null) config = new CSSConfig();
        this.cssConfig = config;
    }

    public ORB getORB() {
        return cssORB;
    }

    public ArrayList getNssArgs() {
        return nssArgs;
    }

    public void setNssArgs(ArrayList nssArgs) {
        this.nssArgs = nssArgs;
    }

    public ArrayList getCssArgs() {
        return cssArgs;
    }

    public void setCssArgs(ArrayList cssArgs) {
        if (cssArgs == null) cssArgs = new ArrayList();
        this.cssArgs = cssArgs;
    }

    public Properties getNssProps() {
        return nssProps;
    }

    public void setNssProps(Properties nssProps) {
        this.nssProps = nssProps;
    }

    public Properties getCssProps() {
        return cssProps;
    }

    public void setCssProps(Properties cssProps) {
        if (cssProps == null) cssProps = new Properties();
        this.cssProps = cssProps;
    }

    public org.omg.CORBA.Object getHome(URI nsURI, String name) {

        if (log.isDebugEnabled()) log.debug(description + " - Looking up home from " + nsURI.toString() + " at " + name);

        try {
            org.omg.CORBA.Object ref = nssORB.string_to_object(nsURI.toString());
            NamingContextExt ic = NamingContextExtHelper.narrow(ref);

            NameComponent[] nameComponent = ic.to_name(name);
            org.omg.CORBA.Object bean = ic.resolve(nameComponent);
            String beanIOR = nssORB.object_to_string(bean);

            bean =  cssORB.string_to_object(beanIOR);

            if (bean instanceof ClientContextHolder) {
                ((ClientContextHolder)bean).setClientContext(context);
            }

            return bean;
        } catch (UserException ue) {
            log.error(description + " - Looking up home", ue);
            throw new RuntimeException(ue);
        }
    }

    public void doStart() throws Exception {

        if (nssConfig == null) {
            if (log.isDebugEnabled()) log.debug("Defaulting NSS config to be CSS config");
            nssConfig = cssConfig;
        }
        if (nssArgs == null) {
            if (log.isDebugEnabled()) log.debug("Defaulting NSS args to be CSS args");
            nssArgs = cssArgs;
        }
        if (nssProps == null) {
            if (log.isDebugEnabled()) log.debug("Defaulting NSS props to be CSS props");
            nssProps = cssProps;
        }

        ClassLoader savedLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(classLoader);

            Properties properties = configAdapter.translateToProps(nssConfig);
            properties.putAll(nssProps);

            if (log.isDebugEnabled()) log.debug("Starting NameService ORB");

            nssORB = ORB.init((String[]) nssArgs.toArray(new String[nssArgs.size()]), properties);

            threadPool.execute(new Runnable() {
                public void run() {
                    nssORB.run();
                }
            });

            properties = configAdapter.translateToProps(cssConfig);
            properties.putAll(cssProps);

            if (log.isDebugEnabled()) log.debug("Starting CSS ORB");

            cssORB = ORB.init((String[]) cssArgs.toArray(new String[cssArgs.size()]), properties);

            threadPool.execute(new Runnable() {
                public void run() {
                    cssORB.run();
                }
            });

            context = new ClientContext();
            context.setSecurityConfig(cssConfig);
            context.setTransactionConfig(buildClientTransactionPolicyConfig());

        } finally {
            Thread.currentThread().setContextClassLoader(savedLoader);
        }

        log.info("Started CORBA Client Security Server - " + description);
    }

    private ClientTransactionPolicyConfig buildClientTransactionPolicyConfig() {
        return new NoDTxClientTransactionPolicyConfig(transactionContextManager);
    }

    public void doStop() throws Exception {
        nssORB.destroy();
        cssORB.destroy();
        log.info("Stopped CORBA Client Security Server - " + description);
    }

    public void doFail() {
        log.info("Failed CORBA Client Security Server " + description);
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(CSSBean.class, "CORBACSS");

        infoFactory.addAttribute("configAdapter", String.class, true);
        infoFactory.addAttribute("description", String.class, true);
        infoFactory.addAttribute("nssConfig", CSSConfig.class, true);
        infoFactory.addAttribute("cssConfig", CSSConfig.class, true);
        infoFactory.addAttribute("ORB", ORB.class, false);
        infoFactory.addAttribute("nssArgs", ArrayList.class, true);
        infoFactory.addAttribute("cssArgs", ArrayList.class, true);
        infoFactory.addAttribute("nssProps", Properties.class, true);
        infoFactory.addAttribute("cssProps", Properties.class, true);
        infoFactory.addOperation("getHome", new Class[]{URI.class, String.class});

        infoFactory.addReference("ThreadPool", Executor.class, NameFactory.GERONIMO_SERVICE);
        infoFactory.addReference("TransactionContextManager", TransactionContextManager.class, NameFactory.JTA_RESOURCE);
        infoFactory.addAttribute("classLoader", ClassLoader.class, false);

        infoFactory.setConstructor(new String[]{"configAdapter", "ThreadPool", "TransactionContextManager", "classLoader"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
