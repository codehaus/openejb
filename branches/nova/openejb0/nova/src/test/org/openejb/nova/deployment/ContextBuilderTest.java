/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */
package org.openejb.nova.deployment;

import java.net.URL;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.transaction.UserTransaction;

import junit.framework.TestCase;
import org.apache.geronimo.kernel.jmx.JMXKernel;
import org.apache.geronimo.naming.java.ComponentContextBuilder;
import org.apache.geronimo.naming.java.ProxyFactory;
import org.apache.geronimo.naming.jmx.JMXReferenceFactory;
import org.apache.geronimo.transaction.manager.UserTransactionImpl;
import org.apache.geronimo.xbeans.j2ee.EjbLocalRefType;
import org.apache.geronimo.xbeans.j2ee.EjbRefType;
import org.apache.geronimo.xbeans.j2ee.EnvEntryType;
import org.apache.geronimo.xbeans.j2ee.ResourceRefType;

/**
 * THIS IS A COPY OF org.apache.geronimo.naming.java.ContextBuilderTest.
 * Copied because maven doesn't share test classes.
 *
 * @version $Revision$ $Date$
 */
public class ContextBuilderTest extends TestCase {
    protected static final String objectName1 = "geronimo.test:name=test1";
    protected static final String objectName2 = "geronimo.test:name=test2";
    protected static final String objectName3 = "geronimo.test:name=test3";


    protected EjbRefType[] ejbRefs;
    protected EjbLocalRefType[] ejbLocalRefs;
    protected EnvEntryType[] envEntries;
    protected ResourceRefType[] resRefs;
    protected Context compCtx;
    protected JMXKernel kernel;
    protected ProxyFactory proxyFactory;
    protected TestObject testObject1 = new TestObject();
    protected TestObject testObject2 = new TestObject();
    protected TestObject testObject3 = new TestObject();

    protected void setUp() throws Exception {
        setUpKernel();
    }

    protected void setUpContext() throws InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException, MalformedObjectNameException {

        proxyFactory = new ProxyFactory() {
            public Object getProxy(Class homeInterface, Class remoteInterface, Object targetId) throws NamingException {
                return homeInterface;
            }

            public Object getProxy(Class interfaced, Object targetId) throws NamingException {
                return interfaced;
            }

        };
        EnvEntryType stringEntry = EnvEntryType.Factory.newInstance();
        stringEntry.addNewEnvEntryName().setStringValue("string");
        stringEntry.addNewEnvEntryType().setStringValue("java.lang.String");
        stringEntry.addNewEnvEntryValue().setStringValue("Hello World");
        EnvEntryType intEntry = EnvEntryType.Factory.newInstance();
        intEntry.addNewEnvEntryName().setStringValue("int");
        intEntry.addNewEnvEntryType().setStringValue("java.lang.Integer");
        intEntry.addNewEnvEntryValue().setStringValue("12345");
        envEntries = new EnvEntryType[] {stringEntry, intEntry};

        EjbRefType ejbRef = EjbRefType.Factory.newInstance();
        ejbRef.addNewEjbRefName().setStringValue("here/there/EJB1");
        ejbRef.addNewEjbRefType().setStringValue("Session");
        ejbRef.addNewHome().setStringValue(Object.class.getName());
        ejbRef.addNewRemote().setStringValue(Object.class.getName());
        //ejbRef.addNewJndiName().setStringValue(objectName1);

        EjbRefType ejbLinkRef = EjbRefType.Factory.newInstance();
        ejbLinkRef.addNewEjbRefName().setStringValue("here/LinkEjb");
        ejbLinkRef.addNewEjbRefType().setStringValue("Session");
        ejbLinkRef.addNewHome().setStringValue(Object.class.getName());
        ejbLinkRef.addNewRemote().setStringValue(Object.class.getName());
        ejbLinkRef.addNewEjbLink().setStringValue(objectName3);
        ejbRefs = new EjbRefType[] {ejbRef, ejbLinkRef};

        EjbLocalRefType ejbLocalRef = EjbLocalRefType.Factory.newInstance();
        ejbLocalRef.addNewEjbRefName().setStringValue("local/here/LocalEJB2");
        ejbLocalRef.addNewEjbRefType().setStringValue("Entity");
        ejbLocalRef.addNewLocalHome().setStringValue(Object.class.getName());
        ejbLocalRef.addNewLocal().setStringValue(Object.class.getName());
        //ejbLocalRef.addNewJndiNamev(objectName2);

        EjbLocalRefType ejbLocalLinkRef = EjbLocalRefType.Factory.newInstance();
        ejbLocalLinkRef.addNewEjbRefName().setStringValue("local/here/LinkLocalEjb");
        ejbLocalLinkRef.addNewEjbRefType().setStringValue("Entity");
        ejbLocalLinkRef.addNewLocalHome().setStringValue(Object.class.getName());
        ejbLocalLinkRef.addNewLocal().setStringValue(Object.class.getName());
        ejbLocalLinkRef.addNewEjbLink().setStringValue(objectName3);
        ejbLocalRefs = new EjbLocalRefType[] {ejbLocalRef, ejbLocalLinkRef};

        ResourceRefType urlRef = ResourceRefType.Factory.newInstance();
        urlRef.addNewResRefName().setStringValue("url/testURL");
        urlRef.addNewResType().setStringValue(URL.class.getName());
        //urlRef.addNewJndiName().setStringValue("http://localhost/path");

        ResourceRefType cfRef = ResourceRefType.Factory.newInstance();
        cfRef.addNewResRefName().setStringValue("DefaultCF");
        cfRef.addNewResType().setStringValue("javax.sql.DataSource");
        //cfRef.addNewJndiName().setStringValue(objectName1);
        resRefs = new ResourceRefType[] {urlRef, cfRef};
    }

    protected void setUpKernel() throws InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException, MalformedObjectNameException {
        kernel = new JMXKernel("geronimo.test");
        kernel.getMBeanServer().registerMBean(testObject1, ObjectName.getInstance(objectName1));
        kernel.getMBeanServer().registerMBean(testObject2, ObjectName.getInstance(objectName2));
        kernel.getMBeanServer().registerMBean(testObject3, ObjectName.getInstance(objectName3));
    }

    public void testEnvEntries() throws Exception {
        setUpContext();
        compCtx = new ComponentContextBuilder(proxyFactory, null, this.getClass().getClassLoader())
                .buildContext(ejbRefs, ejbLocalRefs, envEntries, resRefs);
        assertEquals("Hello World", compCtx.lookup("env/string"));
        assertEquals(new Integer(12345), compCtx.lookup("env/int"));
        //assertEquals(new URL("http://localhost/path"), compCtx.lookup("env/url/testURL"));
        assertNotNull(compCtx.lookup("env/url/testURL"));
    }

    public void testUserTransaction() throws Exception {
        setUpContext();
        compCtx = new ComponentContextBuilder(proxyFactory, null, this.getClass().getClassLoader()).buildContext(ejbRefs, ejbLocalRefs, envEntries, resRefs);
        try {
            compCtx.lookup("UserTransaction");
            fail("Expected NameNotFoundException");
        } catch (NameNotFoundException e) {
            // OK
        }

        UserTransaction userTransaction = new UserTransactionImpl();
        compCtx = new ComponentContextBuilder(proxyFactory, userTransaction, this.getClass().getClassLoader()).buildContext(ejbRefs, ejbLocalRefs, envEntries, resRefs);
        assertEquals(userTransaction, compCtx.lookup("UserTransaction"));
    }

//
//    Bad test... test object needs to be converted to a GeronimoMBean
//
//    public void testClientEJBRefs() throws Exception {
//        ReadOnlyContext compContext = new ComponentContextBuilder(proxyFactory, null).buildContext(client);
//        RootContext.setComponentContext(compContext);
//        InitialContext initialContext = new InitialContext();
//        assertEquals("Expected object from testObject1", testObject1.getEJBHome(),
//                initialContext.lookup("java:comp/env/here/there/EJB1"));
//        assertEquals("Expected object from testObject3", testObject3.getEJBHome(),
//                initialContext.lookup("java:comp/env/here/LinkEjb"));
//        assertEquals("Expected object from testObject1", testObject1.getConnectionFactory(),
//                initialContext.lookup("java:comp/env/DefaultCF"));
//    }
//
//    public void testLocalEJBRefs() throws Exception {
//        ReadOnlyContext compContext = new ComponentContextBuilder(proxyFactory, null).buildContext(session);
//        RootContext.setComponentContext(compContext);
//        InitialContext initialContext = new InitialContext();
//        assertEquals("Expected object from testObject1", testObject1.getEJBHome(),
//                initialContext.lookup("java:comp/env/here/there/EJB1"));
//
//        assertEquals("Expected object from testObject3", testObject3.getEJBHome(),
//                initialContext.lookup("java:comp/env/here/LinkEjb"));
//
//        assertEquals("Expected object from testObject1", testObject2.getEJBLocalHome(),
//                initialContext.lookup("java:comp/env/local/here/LocalEJB2"));
//
//        assertEquals("Expected object from testObject3", testObject3.getEJBLocalHome(),
//                initialContext.lookup("java:comp/env/local/here/LinkLocalEjb"));
//
//        assertEquals("Expected object from testObject1", testObject1.getConnectionFactory(),
//                initialContext.lookup("java:comp/env/DefaultCF"));
//    }

}
