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
package org.openejb.dispatch;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.apache.geronimo.kernel.ClassLoading;

/**
 *
 *
 * @version $Revision$ $Date$
 */
public final class InterfaceMethodSignature implements Serializable {
    private static final String[] NOARGS = {};
    private final String methodName;
    private final String[] parameterTypes;
    private final boolean isHomeMethod;
    private final int hashCode;

    public InterfaceMethodSignature(Method method, boolean isHomeMethod) {
        this(method.getName(), convertParameterTypes(method.getParameterTypes()), isHomeMethod);
    }

    public InterfaceMethodSignature(String methodName, boolean isHomeMethod) {
        this(methodName, NOARGS, isHomeMethod);
    }

    public InterfaceMethodSignature(String methodName, Class[] params, boolean isHomeMethod) {
        this(methodName, convertParameterTypes(params), isHomeMethod);
    }

    public InterfaceMethodSignature(MethodSignature signature, boolean isHomeMethod) {
        this(signature.getMethodName(), signature.getParameterTypes(), isHomeMethod);
    }

    public InterfaceMethodSignature(String methodName, String[] parameterTypes, boolean isHomeMethod) {
        this.methodName = methodName;
        this.parameterTypes = parameterTypes != null ? parameterTypes : NOARGS;
        this.isHomeMethod = isHomeMethod;

        int result = 17;
        result = 37 * result + methodName.hashCode();
        for (int i = 0; i < parameterTypes.length; i++) {
            result = 37 * result + parameterTypes[i].hashCode();
        }
        hashCode = result;
    }

    public String getMethodName() {
        return methodName;
    }

    public String[] getParameterTypes() {
        return parameterTypes;
    }

    public boolean isHomeMethod() {
        return isHomeMethod;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(methodName).append('(');
        if (parameterTypes != null) {
            for (int i = 0; i < parameterTypes.length; i++) {
                String parameterType = parameterTypes[i];
                if (i > 0) {
                    buffer.append(',');
                }
                buffer.append(parameterType);
            }
        }
        buffer.append(')');
        return buffer.toString();
    }

    public boolean match(Method method) {
//        if (!isCorrectType(method.getDeclaringClass())) {
//            return false;
//        }

        if(!methodName.equals(method.getName())) {
            return false;
        }
        Class[] types = method.getParameterTypes();
        if (types.length != parameterTypes.length) {
            return false;
        }
        for (int i = 0; i < parameterTypes.length; i++) {
            if(!types[i].getName().equals(parameterTypes[i])) {
                return false;
            }
        }
        return true;
    }

    public Method getMethod(Class clazz) {
        if (clazz == null) { // || !isCorrectType(clazz)) {
            return null;
        }

        try {
            ClassLoader classLoader = clazz.getClassLoader();
            Class[] args = new Class[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i++) {
                args[i] = ClassLoading.loadClass(parameterTypes[i], classLoader);
            }
            return clazz.getMethod(methodName, args);
        } catch (Exception e) {
            return null;
        }
    }

    public int hashCode() {
        return hashCode;
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof InterfaceMethodSignature == false) {
            return false;
        }
        InterfaceMethodSignature other = (InterfaceMethodSignature) obj;
        return  hashCode == other.hashCode &&
                isHomeMethod == other.isHomeMethod &&
                methodName.equals(other.methodName) &&
                Arrays.equals(parameterTypes, other.parameterTypes);
    }

    private static String[] convertParameterTypes(Class[] params) {
        if(params == null || params.length == 0) {
            return NOARGS;
        }

        String[] types = new String[params.length];
        for (int i = 0; i < params.length; i++) {
            types[i] = params[i].getName();
        }
        return types;
    }

//    private boolean isCorrectType(Class clazz) {
//        if (isHomeMethod) {
//            if(!EJBHome.class.isAssignableFrom(clazz) && !EJBLocalHome.class.isAssignableFrom(clazz)) {
//                return false;
//            }
//        } else {
//            if(!EJBObject.class.isAssignableFrom(clazz) && !EJBLocalObject.class.isAssignableFrom(clazz)) {
//                return false;
//            }
//        }
//        return true;
//    }
}
