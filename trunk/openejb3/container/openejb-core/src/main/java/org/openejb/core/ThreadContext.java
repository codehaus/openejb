package org.openejb.core;

import org.openejb.util.FastThreadLocal;
import org.openejb.ClassLoaderUtil;

public class ThreadContext implements Cloneable {

    protected static final FastThreadLocal threadStorage = new FastThreadLocal();
    protected static Class implClass = ThreadContext.class;

    protected boolean valid = false;
    protected DeploymentInfo deploymentInfo;
    protected Object primaryKey;
    protected byte currentOperation;
    protected Object securityIdentity;
    protected Object unspecified;

    static {
        String className = System.getProperty(EnvProps.THREAD_CONTEXT_IMPL);

        if (className == null) {
            className = System.getProperty(EnvProps.THREAD_CONTEXT_IMPL);
        }

        if (className != null) {
            try {
                ClassLoader cl = ClassLoaderUtil.getContextClassLoader();
                implClass = Class.forName(className, true, cl);
            } catch (Exception e) {
                System.out.println("Can not load ThreadContext class. org.openejb.core.threadcontext_class = " + className);
                e.printStackTrace();
                implClass = null;
            }
        }
    }

    protected static ThreadContext newThreadContext() {
        try {
            return (ThreadContext) implClass.newInstance();
        } catch (Exception e) {

            e.printStackTrace();
            throw new RuntimeException("ThreadContext implemenation class could not be instantiated. Class type = " + implClass + " exception message = " + e.getMessage());
        }
    }

    public static boolean isValid() {
        ThreadContext tc = (ThreadContext) threadStorage.get();
        if (tc != null)
            return tc.valid;
        else
            return false;
    }

    protected void makeInvalid() {
        valid = false;
        deploymentInfo = null;
        primaryKey = null;
        currentOperation = (byte) 0;
        securityIdentity = null;
        unspecified = null;
    }

    public static void invalidate() {
        ThreadContext tc = (ThreadContext) threadStorage.get();
        if (tc != null)
            tc.makeInvalid();
    }

    public static void setThreadContext(ThreadContext tc) {
        if (tc == null) {
            tc = (ThreadContext) threadStorage.get();
            if (tc != null) tc.makeInvalid();
        } else {
            threadStorage.set(tc);
        }
    }

    public static ThreadContext getThreadContext() {
        ThreadContext tc = (ThreadContext) threadStorage.get();
        if (tc == null) {
            tc = ThreadContext.newThreadContext();
            threadStorage.set(tc);
        }
        return tc;
    }

    public byte getCurrentOperation() {
        return currentOperation;
    }

    public Object getPrimaryKey() {
        return primaryKey;
    }

    public DeploymentInfo getDeploymentInfo() {
        return deploymentInfo;
    }

    public Object getSecurityIdentity() {
        return securityIdentity;
    }

    public Object getUnspecified() {
        return unspecified;
    }

    public void set(DeploymentInfo di, Object primKey, Object securityIdentity) {
        setDeploymentInfo(di);
        setPrimaryKey(primKey);
        setSecurityIdentity(securityIdentity);
        valid = true;
    }

    public void setCurrentOperation(byte op) {
        currentOperation = op;
        valid = true;
    }

    public void setPrimaryKey(Object primKey) {
        primaryKey = primKey;
        valid = true;
    }

    public void setSecurityIdentity(Object identity) {
        securityIdentity = identity;
        valid = true;
    }

    public void setDeploymentInfo(DeploymentInfo info) {
        deploymentInfo = info;
    }

    public void setUnspecified(Object obj) {
        unspecified = obj;
    }

    public boolean valid() {
        return valid;
    }

    public java.lang.Object clone() throws java.lang.CloneNotSupportedException {
        return super.clone();
    }
}
