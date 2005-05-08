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

package org.openejb.mejb;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.io.Serializable;
import javax.ejb.EJBException;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.management.j2ee.Management;
import javax.management.j2ee.ManagementHome;
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;
import javax.security.auth.Subject;

import net.sf.cglib.reflect.FastClass;
import org.apache.geronimo.core.service.Invocation;
import org.apache.geronimo.core.service.InvocationResult;
import org.apache.geronimo.core.service.SimpleInvocationResult;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Kernel;
import org.openejb.EJBComponentType;
import org.openejb.EJBContainer;
import org.openejb.EJBInvocation;
import org.openejb.dispatch.InterfaceMethodSignature;
import org.openejb.dispatch.MethodSignature;
import org.openejb.proxy.EJBProxyFactory;
import org.openejb.proxy.ProxyInfo;

/**
 * @version $Rev:  $ $Date$
 */
public class MEJB extends org.apache.geronimo.j2ee.mejb.MEJB implements EJBContainer {

    private static final int CREATE_INDEX = -1;
    private static final String DEFAULT_EJB_NAME = "ejb/mgmt/MEJB";

    private final String objectName;
    private final InterfaceMethodSignature[] signatures;
    private final int[] methodMap;
    private final EJBProxyFactory proxyFactory;
    private final ProxyInfo proxyInfo;
    private final FastClass fastClass;
    private final String ejbName;

    public MEJB(String objectName, Kernel kernel) {
        super(kernel);
        this.objectName = objectName;
        String ejbName;
        try {
            ObjectName oname = ObjectName.getInstance(objectName);
            ejbName = oname.getKeyProperty("name");
            if (ejbName == null) {
                ejbName = DEFAULT_EJB_NAME;
            }
        } catch (MalformedObjectNameException e) {
            ejbName = DEFAULT_EJB_NAME;
        }
        this.ejbName = ejbName;
        fastClass = FastClass.create(MEJB.class);
        LinkedHashMap vopMap = buildSignatures();
        signatures = (InterfaceMethodSignature[])vopMap.keySet().toArray(new InterfaceMethodSignature[vopMap.size()]);
        methodMap = new int[signatures.length];
        int i = 0;
        for (Iterator it = vopMap.values().iterator(); it.hasNext(); ) {
            methodMap[i++] = ((Integer)it.next()).intValue();
        }
        proxyInfo = new ProxyInfo(EJBComponentType.STATELESS, objectName, ManagementHome.class, Management.class, null, null, null, null);
        proxyFactory = new EJBProxyFactory(this);
    }

    private LinkedHashMap buildSignatures() {
        LinkedHashMap vopMap = new LinkedHashMap();
        vopMap.put(new InterfaceMethodSignature("create", true), new Integer(CREATE_INDEX));
        // add the business methods
        Method[] beanMethods = Management.class.getMethods();
        for (int i = 0; i < beanMethods.length; i++) {
            Method beanMethod = beanMethods[i];

            MethodSignature signature = new MethodSignature(beanMethod);
            int methodIndex = fastClass.getIndex(beanMethod.getName(), beanMethod.getParameterTypes());

            vopMap.put(new InterfaceMethodSignature(signature, false),
                    new Integer(methodIndex));
        }

        return vopMap;
    }

    public Object getContainerID() {
        return objectName;
    }

    public String getEjbName() {
        return ejbName;
    }

    public EJBHome getEjbHome() {
        return proxyFactory.getEJBHome();
    }

    public EJBObject getEjbObject(Object primaryKey) {
        return proxyFactory.getEJBObject(primaryKey);
    }

    public EJBLocalHome getEjbLocalHome() {
        return proxyFactory.getEJBLocalHome();
    }

    public EJBLocalObject getEjbLocalObject(Object primaryKey) {
        return proxyFactory.getEJBLocalObject(primaryKey);
    }

    // EJBObject implementation
    //TODO who implements this?
    public Handle getHandle() {
        return null;
    }

    public Object invoke(Method callMethod, Object[] args, Object primKey) throws Throwable {
        throw new EJBException("This invoke style not implemented in MEJB");
    }

    public String[] getJndiNames() {
        return new String[0];
    }

    public String[] getLocalJndiNames() {
        return new String[0];
    }

    public ClassLoader getClassLoader() {
        return this.getClass().getClassLoader();
    }

    public EJBContainer getUnmanagedReference() {
        return this;
    }

    public int getMethodIndex(Method method) {
        return proxyFactory.getMethodIndex(method);
    }

    public InterfaceMethodSignature[] getSignatures() {
        return signatures;
    }

    public ProxyInfo getProxyInfo() {
        return proxyInfo;
    }

    public Subject getDefaultSubject() {
        //TODO this is wrong
        return null;
    }

    //todo implement to make MEJB accessible through CORBA
    public Serializable getHomeTxPolicyConfig() {
        return null;
    }

    //todo implement to make MEJB accessible through CORBA
    public Serializable getRemoteTxPolicyConfig() {
        return null;
    }

    public InvocationResult invoke(Invocation invocation) throws Throwable {
        EJBInvocation ejbInvocation = (EJBInvocation) invocation;
        int methodIndex = methodMap[ejbInvocation.getMethodIndex()];
        if (methodIndex == CREATE_INDEX) {
            return new SimpleInvocationResult(true, getEjbObject(null));
        }
        try {
            return new SimpleInvocationResult(true, fastClass.invoke(methodIndex, this, ejbInvocation.getArguments()));
        } catch (InvocationTargetException ite) {
            Throwable t = ite.getTargetException();
            if (t instanceof Exception && t instanceof RuntimeException == false) {
                // checked exception - which we simply include in the result
                return new SimpleInvocationResult(false, t);
            } else {
                // unchecked Exception - just throw it to indicate an abnormal completion
                throw t;
            }
        }
    }
    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = new GBeanInfoBuilder(MEJB.class, NameFactory.STATELESS_SESSION_BEAN);
        infoBuilder.addAttribute("objectName", String.class, false);
        infoBuilder.addAttribute("kernel", Kernel.class, false);
        infoBuilder.addInterface(EJBContainer.class);

        infoBuilder.setConstructor(new String[]{"objectName", "kernel"});

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
