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
package org.openejb.nova.entity.bmp;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;
import javax.ejb.EntityContext;

import net.sf.cglib.reflect.FastClass;

import org.openejb.nova.dispatch.AbstractOperationFactory;
import org.openejb.nova.dispatch.MethodSignature;
import org.openejb.nova.dispatch.VirtualOperation;
import org.openejb.nova.entity.BusinessMethod;
import org.openejb.nova.entity.HomeMethod;

/**
 *
 *
 * @version $Revision$ $Date$
 */
public class BMPOperationFactory extends AbstractOperationFactory {
    public static BMPOperationFactory newInstance(Class beanClass) {
        FastClass fastClass = FastClass.create(beanClass);
        String beanClassName = beanClass.getName();
        Method[] methods = beanClass.getMethods();
        Method setEntityContext;
        Method unsetEntityContext;
        try {
            setEntityContext = beanClass.getMethod("setEntityContext", new Class[]{EntityContext.class});
            unsetEntityContext = beanClass.getMethod("unsetEntityContext", null);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Bean does not implement javax.ejb.EntityBean");
        }

        ArrayList sigList = new ArrayList(methods.length);
        ArrayList vopList = new ArrayList(methods.length);
        Integer remove = null;
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if (Object.class == method.getDeclaringClass()) {
                continue;
            }
            if (setEntityContext.equals(method)) {
                continue;
            }
            if (unsetEntityContext.equals(method)) {
                continue;
            }
            String name = method.getName();
            int index = fastClass.getIndex(name, method.getParameterTypes());
            MethodSignature sig = new MethodSignature(beanClassName, method);
            VirtualOperation vop;
            if (!name.startsWith("ejb")) {
                vop = new BusinessMethod(fastClass, index);
            } else if (name.startsWith("ejbCreate")) {
                try {
                    Method postCreate = beanClass.getMethod("ejbPostCreate" + name.substring(9), method.getParameterTypes());
                    vop = new BMPCreateMethod(method, postCreate);
                } catch (NoSuchMethodException e) {
                    throw new IllegalStateException("No ejbPostCreate method found matching " + method);
                }
            } else if (name.startsWith("ejbFind")) {
                vop = new BMPFinderMethod(method);
            } else if (name.startsWith("ejbHome")) {
                vop = new HomeMethod(fastClass, index);
            } else if (name.equals("ejbRemove")) {
                vop = new BMPRemoveMethod(fastClass, index);
                remove = new Integer(sigList.size());
            } else {
                continue;
            }
            sigList.add(sig);
            vopList.add(vop);
        }
        MethodSignature[] signatures = (MethodSignature[]) sigList.toArray(new MethodSignature[0]);
        VirtualOperation[] vtable = (VirtualOperation[]) vopList.toArray(new VirtualOperation[0]);

        return new BMPOperationFactory(beanClass, vtable, signatures, remove);
    }

    private final Integer remove;

    private BMPOperationFactory(Class beanClass, VirtualOperation[] vtable, MethodSignature[] signatures, Integer remove) {
        super(beanClass, vtable, signatures);
        this.remove = remove;
    }

    public Map getObjectMap(Class interfaceClass) {
        Map map = super.getObjectMap(interfaceClass);
        try {
            map.put(interfaceClass.getMethod("remove", null), remove);
        } catch (Exception e) {
            throw new IllegalArgumentException("Bean does not define ejbRemove");
        }
        return map;
    }

    public Map getLocalObjectMap(Class interfaceClass) {
        Map map = super.getLocalObjectMap(interfaceClass);
        try {
            map.put(interfaceClass.getMethod("remove", null), remove);
        } catch (Exception e) {
            throw new IllegalArgumentException("Bean does not define ejbRemove");
        }
        return map;
    }
}