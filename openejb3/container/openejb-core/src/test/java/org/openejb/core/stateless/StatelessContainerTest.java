/**
 *
 * Copyright 2006 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.openejb.core.stateless;

import junit.framework.TestCase;
import org.openejb.DeploymentInfo;
import org.openejb.OpenEJBException;
import org.openejb.alt.config.DeployedJar;
import org.openejb.alt.config.EjbJarInfoBuilder;
import org.openejb.alt.config.ejb.EjbDeployment;
import org.openejb.alt.config.ejb.OpenejbJar;
import org.openejb.assembler.classic.EjbJarBuilder;
import org.openejb.assembler.classic.EjbJarInfo;
import org.openejb.jee.EjbJar;
import org.openejb.jee.StatelessBean;
import org.openejb.ri.sp.PseudoSecurityService;
import org.openejb.ri.sp.PseudoTransactionService;

import javax.ejb.SessionContext;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

/**
 * @version $Revision$ $Date$
 */
public class StatelessContainerTest extends TestCase {

    public void testPojoStyleBean() throws Exception {

        StatelessBean bean = new StatelessBean("widget", WidgetBean.class.getName());
        bean.setBusinessLocal(Widget.class.getName());
        bean.addPostConstruct("init");
        bean.addPreDestroy("destroy");

        EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(bean);

        OpenejbJar openejbJar = new OpenejbJar();
        openejbJar.addEjbDeployment(new EjbDeployment("Stateless Container", "widget", "widget"));

        DeployedJar jar = new DeployedJar("", ejbJar, openejbJar);

        HashMap<String, DeploymentInfo> ejbs = build(jar);

        StatelessContainer container = new StatelessContainer("Stateless Container", new PseudoTransactionService(), new PseudoSecurityService(), ejbs, 10, 0, false);

        Object result = container.invoke("widget", Widget.class.getMethod("getLifecycle"), new Object[]{}, null, "");
        assertTrue("instance of Stack", result instanceof Stack);

        Stack<Lifecycle> actual = (Stack<Lifecycle>) result;

        List expected = Arrays.asList(Lifecycle.values());

        assertEquals(join("\n", expected), join("\n", actual));
    }

    private static String join(String delimeter, List items) {
        StringBuffer sb = new StringBuffer();
        for (Object item : items) {
            sb.append(item.toString()).append(delimeter);
        }
        return sb.toString();
    }

    private HashMap<String, DeploymentInfo> build(DeployedJar jar) throws OpenEJBException {
        EjbJarInfoBuilder infoBuilder = new EjbJarInfoBuilder();
        EjbJarBuilder builder = new EjbJarBuilder(this.getClass().getClassLoader());
        EjbJarInfo jarInfo = infoBuilder.buildInfo(jar);
        HashMap<String, DeploymentInfo> ejbs = builder.build(jarInfo);
        return ejbs;
    }

    public static interface Widget {
        Stack<Lifecycle> getLifecycle();
    }

    public static enum Lifecycle {
        CONSTRUCTOR, INJECTION, POST_CONSTRUCT, BUSINESS_METHOD, PRE_DESTROY
    }

    public static class WidgetBean implements Widget {

        private Stack<Lifecycle> lifecycle = new Stack();

        private SessionContext sessionContext;

        public WidgetBean() {
            lifecycle.push(Lifecycle.CONSTRUCTOR);
        }

        public void setSessionContext(SessionContext sessionContext) {
            lifecycle.push(Lifecycle.INJECTION);
            this.sessionContext = sessionContext;
        }

        public Stack<Lifecycle> getLifecycle() {
            lifecycle.push(Lifecycle.BUSINESS_METHOD);
            return lifecycle;
        }

        public void init() {
            lifecycle.push(Lifecycle.POST_CONSTRUCT);
        }

        public void destroy() {
            lifecycle.push(Lifecycle.PRE_DESTROY);
        }
    }
}
