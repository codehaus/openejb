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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import net.sf.cglib.core.Signature;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.reflect.FastClass;
import org.objectweb.asm.Type;
import org.openejb.nova.dispatch.MethodSignature;

/**
 *
 *
 * @version $Revision$ $Date$
 */
public class EJBProxyInterceptor implements MethodInterceptor {
    /**
     * The client container that is invoked from intercepted methods.
     */
    private final ClientContainer container;

    /**
     * The type of the ejb invocation.  This is used during construction of the EJBInvocation object.
     */
    private final EJBInvocationType ejbInvocationType;

    /**
     * Id of the object being invoked.  This is used during construction of the EJBInvocation object.
     * May be null for a home proxy.
     */
    private final Object id;

    /**
     * Map from interface method ids to vop ids.
     */
    private final int[] operationMap;

    public EJBProxyInterceptor(ClientContainer container, EJBInvocationType ejbInvocationType, Class proxyType, MethodSignature[] signatures) {
        this(container, ejbInvocationType, getOperationMap(ejbInvocationType, proxyType, signatures), null);
    }

    public EJBProxyInterceptor(ClientContainer container, EJBInvocationType ejbInvocationType, int[] operationMap, Object id) {
        assert container != null;
        assert operationMap != null;

        this.container = container;
        this.ejbInvocationType = ejbInvocationType;
        this.operationMap = operationMap;
        this.id = id;
    }

    /**
     * Handles an invocation on a proxy
     * @param object the proxy instance
     * @param method java method that was invoked
     * @param args arguments to the mentod
     * @param methodProxy a CGLib method proxy of the method invoked
     * @return the result of the invocation
     * @throws java.lang.Throwable if any exceptions are thrown by the implementation method
     */
    public Object intercept(Object object, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        int vopIndex = operationMap[methodProxy.getSuperIndex()];
        return container.invoke(ejbInvocationType, id, vopIndex, args);
    }

    public static int[] getOperationMap(EJBInvocationType ejbInvocationType, Class proxyType, MethodSignature[] signatures) {
        // translate the method names
        MethodSignature[] translated = translate(ejbInvocationType, signatures);

        // get the map from method keys to the intercepted shadow index
        Map proxyToShadowIndex = buildProxyToShadowIndex(proxyType);

        // create the method lookup table and fill it with -1
        int[] shadowIndexToProxy = new int[FastClass.create(proxyType).getMaxIndex() + 1];
        Arrays.fill(shadowIndexToProxy, -1);

        // for each translated method (the method signature on the proxy),
        // fill in it's id into the shadowIndex table
        for (int i = 0; i < translated.length; i++) {
            if (translated[i] != null) {
                Integer shadowIndex = (Integer) proxyToShadowIndex.get(translated[i]);
                if (shadowIndex != null) {
                    shadowIndexToProxy[shadowIndex.intValue()] = i;
                }
            }
        }
        return shadowIndexToProxy;
    }

    /**
     * Translates the implementation method signatures to interface method signatures.
     * @param signatures the implementation method signatures
     * @return the matching interface method signatures
     */
    private static MethodSignature[] translate(EJBInvocationType ejbInvocationType, MethodSignature[] signatures) {
        MethodSignature[] translated = new MethodSignature[signatures.length];
        if (ejbInvocationType == EJBInvocationType.HOME || ejbInvocationType == EJBInvocationType.LOCALHOME) {
            for (int i = 0; i < signatures.length; i++) {
                MethodSignature signature = signatures[i];
                String name = signature.getMethodName();
                if (name.startsWith("ejbCreate")) {
                    translated[i] = new MethodSignature("c" + name.substring(4), signature.getParameterTypes());
                } else if (name.startsWith("ejbFind")) {
                    translated[i] = new MethodSignature("f" + name.substring(4), signature.getParameterTypes());
                } else if (name.startsWith("ejbHome")) {
                    String translatedName = Character.toLowerCase(name.charAt(7)) + name.substring(8);
                    translated[i] = new MethodSignature(translatedName, signature.getParameterTypes());
                } else if (name.startsWith("ejbRemove")) {
                    translated[i] = new MethodSignature("remove", signature.getParameterTypes());
                }
            }
        } else if (ejbInvocationType == EJBInvocationType.REMOTE || ejbInvocationType == EJBInvocationType.LOCAL) {
            for (int i = 0; i < signatures.length; i++) {
                MethodSignature signature = signatures[i];
                String name = signature.getMethodName();
                if (name.startsWith("ejbRemove")) {
                    translated[i] = new MethodSignature("remove", signature.getParameterTypes());
                } else {
                    translated[i] = new MethodSignature(signature.getMethodName(), signature.getParameterTypes());
                }
            }
        }
        return translated;
    }

    /**
     * Builds a map from the MethodKeys for the real method to the index of
     * the shadow method, which is the same number returned from MethodProxy.getSuperIndex().
     * The map contains only the MethodKeys of methods that have shadow methods (i.e., only
     * the enhanced methods).
     * @param proxyType the generated proxy implementation class
     * @return a map from MethodKeys to the Integer for the shadow method
     */
    private static Map buildProxyToShadowIndex(Class proxyType) {
        Map shadowMap = new HashMap();
        Method[] methods = proxyType.getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            int shadowIndex = getSuperIndex(proxyType, methods[i]);
            if (shadowIndex >= 0) {
                shadowMap.put(new MethodSignature(methods[i]), new Integer(shadowIndex));
            }
        }
        return shadowMap;
    }

    public static int getSuperIndex(Class proxyType, Method method) {
        Signature signature = new Signature(method.getName(), Type.getReturnType(method), Type.getArgumentTypes(method));
        MethodProxy methodProxy = MethodProxy.find(proxyType, signature);
        if (methodProxy != null) {
            return methodProxy.getSuperIndex();
        }
        return -1;
    }
}
