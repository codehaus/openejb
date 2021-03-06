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
package org.openejb.mdb;

import java.lang.reflect.Method;
import java.util.Map;
import javax.ejb.EJBException;
import javax.resource.ResourceException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.Status;
import javax.transaction.xa.XAResource;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.interceptor.InvocationResult;
import org.apache.geronimo.transaction.manager.NamedXAResource;
import org.openejb.EJBInterfaceType;
import org.openejb.EjbInvocation;
import org.openejb.EjbInvocationImpl;

/**
 * Container for the local interface of a Message Driven Bean.
 * This container owns implementations of EJBLocalHome and EJBLocalObject
 * that can be used by a client in the same classloader as the server.
 * <p/>
 * The implementation of the interfaces is generated using cglib FastClass
 * proxies to avoid the overhead of native Java reflection.
 * <p/>
 * <p/>
 * <p/>
 * <p/>
 * The J2EE connector and EJB specifications are not clear on what happens when beforeDelivery or
 * afterDelivery throw an exception, so here is what we have decided:
 * <p/>
 * Exception from beforeDelivery:
 * if container started TX, roll it back
 * reset class loader to adapter classloader
 * reset state to STATE_NONE
 * <p/>
 * Exception from delivery method:
 * if container started TX, roll it back
 * reset class loader to adapter classloader
 * if state was STATE_BEFORE_CALLED, set state to STATE_ERROR so after can still be called
 * <p/>
 * Exception from afterDelivery:
 * if container started TX, roll it back
 * reset class loader to adapter classloader
 * reset state to STATE_NONE
 * <p/>
 * One subtle side effect of this is if the adapter ignores an exception from beforeDelivery and
 * continues with delivery and afterDelivery, the delivery will be treated as a single standalone
 * delivery and the afterDelivery will throw an IllegalStateException.
 *
 * @version $Revision$ $Date$
 */
public class EndpointHandler implements MethodInterceptor {
    private static final Log log = LogFactory.getLog(EndpointProxy.class);

    private static final int STATE_NONE = 0;
    private static final int STATE_BEFORE_CALLED = 1;
    private static final int STATE_METHOD_CALLED = 2;
    private static final int STATE_ERROR = 3;

    private final MdbDeployment mdbDeployment;
    private final NamedXAResource xaResource;
    private final int[] operationMap;
    private final Map methodIndexMap;
    private final ClassLoader classLoader;
    private final TransactionManager transactionManager;

    private ClassLoader adapterClassLoader;
    private Transaction adapterTransaction;
    private Transaction beanTransaction;
    private boolean isReleased = false;
    private int state = STATE_NONE;

    public EndpointHandler(MdbDeployment mdbDeployment, NamedXAResource xaResource, int[] operationMap, TransactionManager transactionManager) {
        this.mdbDeployment = mdbDeployment;
        this.xaResource = xaResource;
        this.operationMap = operationMap;
        this.methodIndexMap = mdbDeployment.getMethodIndexMap();
        classLoader = mdbDeployment.getClassLoader();
        this.transactionManager = transactionManager;
    }

    public void beforeDelivery(Method method) throws NoSuchMethodException, ResourceException {
        if (isReleased) {
            throw new IllegalStateException("Proxy has been released");
        }

        if (state != STATE_NONE && state != STATE_ERROR) {
            if (state == STATE_BEFORE_CALLED) {
                throw new IllegalStateException("beforeDelivery can not be called again until message is delivered and afterDelivery is called");
            } else if (state == STATE_METHOD_CALLED) {
                throw new IllegalStateException("The last message delivery must be completed with an afterDeliver before beforeDeliver can be called again");
            }
        }

        Integer methodIndex = (Integer) methodIndexMap.get(method);
        if (methodIndex == null) {
            throw new NoSuchMethodError("Unknown method: " + method);
        }

        try {
            setupDelivery(methodIndex.intValue());
        } catch (NoSuchMethodException e) {
            throw e;
        } catch (Throwable throwable) {
            if (throwable instanceof Exception) {
                throw new ResourceException(throwable);
            } else if (throwable instanceof Error) {
                throw new ResourceException(throwable);
            }
            throw new ResourceException("Unknown throwable", throwable);
        }

        state = STATE_BEFORE_CALLED;
    }


    private Object invoke(int methodIndex, Object[] args) throws Throwable {
        InvocationResult result;
        try {
            EjbInvocation invocation = new EjbInvocationImpl(EJBInterfaceType.LOCAL, null, methodIndex, args);
            result = mdbDeployment.invoke(invocation);
        } catch (Throwable t) {
            if (!(t instanceof EJBException)) {
                t = new EJBException().initCause(t);
            }
            throw t;
        }
        if (result.isNormal()) {
            return result.getResult();
        } else {
            throw result.getException();
        }
    }

    public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        if (isReleased) {
            throw new IllegalStateException("Proxy has been released");
        }

        if (state != STATE_NONE) {
            if (state == STATE_BEFORE_CALLED) {
                state = STATE_METHOD_CALLED;
            } else if (state == STATE_METHOD_CALLED) {
                throw new IllegalStateException("The last message delivery must be completed with an afterDeliver before another message can be delivered");
            }
        }

        int methodIndex = operationMap[methodProxy.getSuperIndex()];
        if (methodIndex < 0) throw new AssertionError("Unknown method: method=" + method);
        try {
            if (state == STATE_NONE) {
                try {
                    setupDelivery(methodIndex);
                } catch (Throwable throwable) {
                    state = STATE_ERROR;
                    if (throwable instanceof Exception) {
                        throw new EJBException((Exception) throwable);
                    } else if (throwable instanceof Error) {
                        throw new EJBException((Exception) throwable);
                    }
                    throw (EJBException) new EJBException("Unknown throwable").initCause(throwable);
                }
            }
            return invoke(methodIndex, args);
        } finally {
            if (state == STATE_NONE) {
                try {
                    teardownDelivery();
                } catch (Throwable throwable) {
                    state = STATE_ERROR;
                    if (throwable instanceof Exception) {
                        throw new EJBException((Exception) throwable);
                    } else if (throwable instanceof Error) {
                        throw new EJBException((Exception) throwable);
                    }
                    throw (EJBException) new EJBException("Unknown throwable").initCause(throwable);
                }
            }
        }
    }

    public void afterDelivery() throws ResourceException {
        if (isReleased) {
            throw new IllegalStateException("Proxy has been released");
        }

        if (state != STATE_METHOD_CALLED && state != STATE_ERROR) {
            if (state == STATE_BEFORE_CALLED) {
                throw new IllegalStateException("Exactally one message must be delivered between beforeDelivery and afterDelivery");
            } else if (state == STATE_NONE) {
                throw new IllegalStateException("afterDelivery may only be called if message delivery began with a beforeDelivery call");
            }
        }
        state = STATE_NONE;

        try {
            teardownDelivery();
        } catch (Throwable throwable) {
            if (throwable instanceof Exception) {
                throw new ResourceException(throwable);
            } else if (throwable instanceof Error) {
                throw new ResourceException(throwable);
            }
            throw new ResourceException("Unknown throwable", throwable);
        }
    }

    public void release() {
        isReleased = true;

        if (state == STATE_BEFORE_CALLED || state == STATE_METHOD_CALLED) {
            // restore the adapter classloader if necessary
            if (adapterClassLoader != classLoader) {
                Thread.currentThread().setContextClassLoader(adapterClassLoader);
            }

            // rollback bean transaction
            if (beanTransaction != null) {
                try {
                    transactionManager.rollback();
                } catch (Exception e) {
                    log.warn("Unable to roll back", e);
                }
            }
        }
        adapterClassLoader = null;
        beanTransaction = null;
        adapterTransaction = null;
    }

    private void setupDelivery(int methodIndex) throws Throwable {
        boolean adapterTransactionSuspended = false;
        Thread currentThread = Thread.currentThread();
        try {
            // setup the classloader
            adapterClassLoader = currentThread.getContextClassLoader();
            if (adapterClassLoader != classLoader) {
                currentThread.setContextClassLoader(classLoader);
            }

            // setup the transaction
            adapterTransaction = transactionManager.getTransaction();
            boolean transactionRequired = mdbDeployment.isDeliveryTransacted(methodIndex);

            // if the adapter gave us a transaction and we are required, just move on
            if (transactionRequired && adapterTransaction != null) {
                return;
            }

            // suspend what ever we got from the adapter
            if (adapterTransaction != null) {
                transactionManager.suspend();
                adapterTransactionSuspended = true;
            }

            if (transactionRequired) {
                // start a new container transaction
                transactionManager.begin();
                beanTransaction = transactionManager.getTransaction();
                if (xaResource != null) {
                    beanTransaction.enlistResource(xaResource);
                }
            }
        } catch (Throwable e) {
            // restore the adapter classloader if necessary
            if (adapterClassLoader != classLoader) {
                currentThread.setContextClassLoader(adapterClassLoader);
            }
            adapterClassLoader = null;

            // restore the adapter transaction is possible
            if (adapterTransactionSuspended) {
                try {
                    transactionManager.resume(adapterTransaction);
                } catch (Exception resumeException) {
                    log.error("Could not resume adapter transaction", resumeException);
                }
            }
            beanTransaction = null;
            adapterTransaction = null;

            // throw the root cause of this exception
            throw e;
        }
    }

    private void teardownDelivery() throws Throwable {
        Throwable throwable = null;
        try {
            if (beanTransaction != null) {
                try {
                    //TODO is this delist necessary???????
                    //check we are really in a transaction.
                    if (xaResource != null) {
                        beanTransaction.delistResource(xaResource, XAResource.TMSUSPEND);
                    }
                } catch (Throwable t) {
                    beanTransaction.setRollbackOnly();
                    throw t;
                } finally {
                    if (transactionManager.getStatus() == Status.STATUS_ACTIVE) {
                        transactionManager.commit();
                    } else {
                        transactionManager.rollback();                        
                    }
                }
            }
        } catch (Throwable t) {
            throwable = t;
        } finally {
            // restore the adapter classloader if necessary
            if (adapterClassLoader != classLoader) {
                Thread.currentThread().setContextClassLoader(adapterClassLoader);
            }
            adapterClassLoader = null;

            // only resume adapter transaction if it exists and if it was suspended:
            // this can be detected by testing beanTransaction != null
            if (adapterTransaction != null && beanTransaction != null) {
                try {
                    transactionManager.resume(adapterTransaction);
                } catch (Throwable resumeException) {
                    // if we already have encountered a problem, just log this one
                    if (throwable != null) {
                        log.warn("Could not resume adapter transaction", resumeException);
                    } else {
                        throwable = resumeException;
                    }
                }
            }
            beanTransaction = null;
            adapterTransaction = null;
        }

        // If we encountered an exception rethrow it
        if (throwable != null) {
            throw throwable;
        }
    }
}
