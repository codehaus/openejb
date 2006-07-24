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
package org.openejb.entity.cmp;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.ejb.DuplicateKeyException;
import javax.ejb.EntityBean;

import net.sf.cglib.reflect.FastClass;
import org.apache.geronimo.interceptor.InvocationResult;
import org.openejb.transaction.EjbTransactionContext;
import org.openejb.EJBOperation;
import org.openejb.EjbInvocation;
import org.openejb.dispatch.MethodSignature;
import org.openejb.dispatch.VirtualOperation;
import org.openejb.proxy.EJBProxyFactory;
import org.openejb.timer.TimerState;

/**
 * @version $Revision$ $Date$
 */
public class CmpCreateMethod implements VirtualOperation, Serializable {
    private static final long serialVersionUID = -1846351514946502346L;
    private final Class beanClass;
    private final Cmp1Bridge cmp1Bridge;
    private final MethodSignature createSignature;
    private final MethodSignature postCreateSignature;

    private final EjbCmpEngine ejbCmpEngine;

    private final transient FastClass fastBeanClass;
    private final transient int createIndex;
    private final transient int postCreateIndex;

    public CmpCreateMethod(Class beanClass,
            Cmp1Bridge cmp1Bridge,
            MethodSignature createSignature,
            MethodSignature postCreateSignature,
            EjbCmpEngine ejbCmpEngine) {

        this.beanClass = beanClass;
        this.cmp1Bridge = cmp1Bridge;
        this.createSignature = createSignature;
        this.postCreateSignature = postCreateSignature;
        this.ejbCmpEngine = ejbCmpEngine;

        fastBeanClass = FastClass.create(beanClass);
        Method createMethod = createSignature.getMethod(beanClass);
        if (createMethod == null) {
            throw new IllegalArgumentException("Bean class does not implement create method:" +
                    " beanClass=" + beanClass.getName() + " method=" + createSignature);
        }
        createIndex = fastBeanClass.getIndex(createMethod.getName(), createMethod.getParameterTypes());

        Method postCreateMethod = postCreateSignature.getMethod(beanClass);
        if (postCreateMethod == null) {
            throw new IllegalArgumentException("Bean class does not implement post create method:" +
                    " beanClass=" + beanClass.getName() + " method=" + postCreateSignature);
        }
        postCreateIndex = fastBeanClass.getIndex(postCreateMethod.getName(), postCreateMethod.getParameterTypes());
    }

    public InvocationResult execute(EjbInvocation invocation) throws Throwable {
        CmpInstanceContext ctx = (CmpInstanceContext) invocation.getEJBInstanceContext();

        ejbCmpEngine.beforeCreate(ctx);

        // call the create method
        EntityBean instance = (EntityBean) ctx.getInstance();
        Object[] args = invocation.getArguments();
        boolean oldTimerMethodAvailable = ctx.setTimerState(EJBOperation.EJBCREATE);
        try {
            ctx.setOperation(EJBOperation.EJBCREATE);
            fastBeanClass.invoke(createIndex, instance, args);
        } catch (InvocationTargetException ite) {
            Throwable t = ite.getTargetException();
            if (t instanceof Exception && !(t instanceof RuntimeException)) {
                // checked exception - which we simply include in the result
                return invocation.createExceptionResult((Exception) t);
            } else {
                // unchecked Exception - just throw it to indicate an abnormal completion
                throw t;
            }
        } finally {
            ctx.setOperation(EJBOperation.INACTIVE);
            TimerState.setTimerState(oldTimerMethodAvailable);
        }

        if (cmp1Bridge != null) {
            // load all of the cmp1 instance fields into the cmp engine
            cmp1Bridge.copyFromObjectToCmp(ctx);
        }

        // create the new instance using the data set during the ejbCreate callback
        EjbTransactionContext ejbTransactionContext = invocation.getEjbTransactionData();
        try {
            ejbCmpEngine.afterCreate(ctx, ejbTransactionContext);
        } catch (DuplicateKeyException e) {
            return invocation.createExceptionResult(e);
        }

        // associate the new cmp instance with the tx context
        ctx.setLoaded(true);
        ejbTransactionContext.associate(ctx);

        // call the post create method
        try {
            ctx.setOperation(EJBOperation.EJBPOSTCREATE);
            ctx.setTimerState(EJBOperation.EJBPOSTCREATE);
            fastBeanClass.invoke(postCreateIndex, instance, args);
        } catch (InvocationTargetException ite) {
            Throwable t = ite.getTargetException();
            if (t instanceof Exception && !(t instanceof RuntimeException)) {
                // checked exception - which we simply include in the result
                // we do not force rollback, that is up to the application
                return invocation.createExceptionResult((Exception) t);
            } else {
                // unchecked Exception - just throw it to indicate an abnormal completion
                throw t;
            }
        } finally {
            ctx.setOperation(EJBOperation.INACTIVE);
            TimerState.setTimerState(oldTimerMethodAvailable);
        }

        // return a new proxy
        Object id = ctx.getId();
        Object ejbProxy = getEjbProxy(invocation.getType().isLocal(), ctx.getProxyFactory(), id);
        return invocation.createResult(ejbProxy);
    }

    private Object getEjbProxy(boolean local, EJBProxyFactory proxyFactory, Object id) {
        if (local) {
            return proxyFactory.getEJBLocalObject(id);
        } else {
            return proxyFactory.getEJBObject(id);
        }
    }

    protected Object readResolve() {
        return new CmpCreateMethod(beanClass, cmp1Bridge, createSignature, postCreateSignature, ejbCmpEngine);
    }
}
