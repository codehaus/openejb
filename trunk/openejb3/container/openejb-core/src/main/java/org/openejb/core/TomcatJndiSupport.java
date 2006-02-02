package org.openejb.core;

import org.openejb.*;

import javax.naming.Context;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Properties;
import java.util.Map;

public class TomcatJndiSupport extends RpcContainerWrapper {
    private final Class contextBindings;
    private final Method bindContext;
    private final Method bindThread;
    private final Method unbindThread;

    public TomcatJndiSupport(RpcContainer container) throws OpenEJBException {
        super(container);
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            contextBindings = classLoader.loadClass("org.apache.naming.ContextBindings");
            bindContext = contextBindings.getMethod("bindContext", new Class[]{Object.class, Context.class, Object.class});
            bindThread = contextBindings.getMethod("bindThread", new Class[]{Object.class, Object.class});
            unbindThread = contextBindings.getMethod("unbindThread", new Class[]{Object.class, Object.class});
        } catch (ClassNotFoundException e) {
            throw new OpenEJBException("Unable to setup Tomcat JNDI support.  Support requires the org.apache.naming.ContextBindings class to be available.");
        } catch (NoSuchMethodException e) {
            throw new OpenEJBException("Unable to setup Tomcat JNDI support.  Method of org.apache.naming.ContextBindings was not found:" + e.getMessage());
        }
        org.openejb.DeploymentInfo[] deploymentInfos = container.deployments();
        for (int i = 0; i < deploymentInfos.length; i++) {
            DeploymentInfo deployment = (DeploymentInfo) deploymentInfos[i];
            setupDeployment(deployment);
        }
    }

    public void init(Object containerId, HashMap deployments, Properties properties) throws OpenEJBException {
    }

    public void deploy(Object deploymentID, org.openejb.DeploymentInfo info) throws OpenEJBException {
        super.deploy(deploymentID, info);
        setupDeployment((DeploymentInfo) info);
    }

    public static Map contexts = new HashMap();

    private void setupDeployment(DeploymentInfo deployment) {

        deployment.setContainer(this);

        Object deploymentID = deployment.getDeploymentID();
        Context jndiEnc = deployment.getJndiEnc();
        bindContext(deploymentID, jndiEnc);
        contexts.put(deploymentID, jndiEnc);
    }

    public Object invoke(Object deployID, Method callMethod, Object[] args, Object primKey, Object securityIdentity) throws OpenEJBException {
        try {

            bindThread(deployID);
            return super.invoke(deployID, callMethod, args, primKey, securityIdentity);
        } finally {
            unbindThread(deployID);
        }
    }

    public void bindContext(Object name, Context context) {
        try {
            bindContext.invoke(null, new Object[]{name, context, name});
        } catch (Throwable e) {
            throw convertToRuntimeException(e, "bindContext");
        }
    }

    public void bindThread(Object name) {
        try {
            bindThread.invoke(null, new Object[]{name, name});
        } catch (Throwable e) {
            throw convertToRuntimeException(e, "bindThread");
        }
    }

    public void unbindThread(Object name) {
        try {
            unbindThread.invoke(null, new Object[]{name, name});
        } catch (Throwable e) {
            throw convertToRuntimeException(e, "unbindThread");
        }
    }

    private RuntimeException convertToRuntimeException(Throwable e, String methodName) {
        if (e instanceof InvocationTargetException) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                return (RuntimeException) cause;
            } else {
                e = cause;
            }
        }
        return new RuntimeException("ContextBindings." + methodName, e);
    }
}
