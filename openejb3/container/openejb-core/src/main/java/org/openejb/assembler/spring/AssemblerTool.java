package org.openejb.assembler.spring;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Vector;
import javax.naming.InitialContext;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ManagedConnectionFactory;

import org.openejb.OpenEJBException;
import org.openejb.assembler.classic.ConnectionManagerInfo;
import org.openejb.assembler.classic.EnterpriseBeanInfo;
import org.openejb.assembler.classic.IntraVmServerInfo;
import org.openejb.assembler.classic.JndiContextInfo;
import org.openejb.assembler.classic.ManagedConnectionFactoryInfo;
import org.openejb.assembler.classic.MethodInfo;
import org.openejb.assembler.classic.MethodPermissionInfo;
import org.openejb.assembler.classic.MethodTransactionInfo;
import org.openejb.assembler.classic.RoleMappingInfo;
import org.openejb.assembler.classic.SecurityRoleReferenceInfo;
import org.openejb.core.DeploymentInfo;
import org.openejb.util.Messages;
import org.openejb.util.SafeToolkit;
import org.openejb.util.proxy.ProxyFactory;
import org.openejb.util.proxy.ProxyManager;

public class AssemblerTool {

    public static final Class PROXY_FACTORY = org.openejb.util.proxy.ProxyFactory.class;
    public static final Class SECURITY_SERVICE = org.openejb.spi.SecurityService.class;
    public static final Class TRANSACTION_SERVICE = org.openejb.spi.TransactionService.class;
    public static final Class CONNECTION_MANAGER = javax.resource.spi.ConnectionManager.class;
    public static final Class CONNECTOR = javax.resource.spi.ManagedConnectionFactory.class;

    protected static final Messages messages = new Messages("org.openejb.util.resources");
    protected static final SafeToolkit toolkit = SafeToolkit.getToolkit("AssemblerTool");
    protected static final HashMap codebases = new HashMap();

    protected Properties props;

    static {
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        codebases.put("CLASSPATH", cl);

        System.setProperty("noBanner", "true");
    }

    /*
    TODO: The Exception Handling here isn't up-to-date and doesn't
    use a message number. Message numbers allow the message text to
    be internationalized.
    */
    public InitialContext assembleRemoteJndiContext(JndiContextInfo context)
            throws org.openejb.OpenEJBException {
        try {
            InitialContext ic = new InitialContext(context.properties);
            return ic;
        } catch (javax.naming.NamingException ne) {

            throw new org.openejb.OpenEJBException("The remote JNDI EJB references for remote-jndi-contexts = " + context.jndiContextId + "+ could not be resolved.", ne);
        }
    }

    public ConnectionManager assembleConnectionManager(ConnectionManagerInfo cmInfo)
            throws OpenEJBException, java.lang.Exception {
        /*TODO: Add better exception handling, this method throws java.lang.Exception,
         which is not very specific. Only a very specific OpenEJBException should be
         thrown.
         */
        Class managerClass = SafeToolkit.loadClass(cmInfo.className, cmInfo.codebase);

        checkImplementation(CONNECTION_MANAGER, managerClass, "ConnectionManager", cmInfo.connectionManagerId);

        ConnectionManager connectionManager = (ConnectionManager) toolkit.newInstance(managerClass);

        if (cmInfo.properties != null) {
            Properties clonedProps = (Properties) (this.props.clone());
            clonedProps.putAll(cmInfo.properties);
            applyProperties(connectionManager, clonedProps);
        }

        return connectionManager;
    }

    public ManagedConnectionFactory assembleManagedConnectionFactory(ManagedConnectionFactoryInfo mngedConFactInfo)
            throws org.openejb.OpenEJBException, java.lang.Exception {

        ManagedConnectionFactory managedConnectionFactory = null;
        try {
            Class factoryClass = SafeToolkit.loadClass(mngedConFactInfo.className, mngedConFactInfo.codebase);
            checkImplementation(CONNECTOR, factoryClass, "Connector", mngedConFactInfo.id);

            managedConnectionFactory = (ManagedConnectionFactory) toolkit.newInstance(factoryClass);
        } catch (Exception e) {
            throw new OpenEJBException("Could not instantiate Connector '" + mngedConFactInfo.id + "'.", e);
        }

        try {

            if (mngedConFactInfo.properties != null) {
                Properties clonedProps = (Properties) (this.props.clone());
                clonedProps.putAll(mngedConFactInfo.properties);
                applyProperties(managedConnectionFactory, clonedProps);
            }
        } catch (java.lang.reflect.InvocationTargetException ite) {
            throw new OpenEJBException("Could not initialize Connector '" + mngedConFactInfo.id + "'.", ite.getTargetException());
        } catch (Exception e) {

            throw new OpenEJBException("Could not initialize Connector '" + mngedConFactInfo.id + "'.", e);
        }

        return managedConnectionFactory;
    }

    public void applyProxyFactory(IntraVmServerInfo ivmInfo) throws OpenEJBException {
        Class factoryClass = SafeToolkit.loadClass(ivmInfo.proxyFactoryClassName, ivmInfo.codebase);

        checkImplementation(PROXY_FACTORY, factoryClass, "ProxyFactory", ivmInfo.factoryName);

        ProxyFactory factory = (ProxyFactory) toolkit.newInstance(factoryClass);

        factory.init(ivmInfo.properties);
        ProxyManager.registerFactory("ivm_server", factory);
        ProxyManager.setDefaultFactory("ivm_server");

    }

    public void applyProperties(Object target, Properties props) throws java.lang.reflect.InvocationTargetException, java.lang.IllegalAccessException, java.lang.NoSuchMethodException {
        if (props != null /*&& props.size()>0*/) {
            Method method = target.getClass().getMethod("init", new Class[]{Properties.class});
            method.invoke(target, new Object[]{props});
        }
    }

    public void applyTransactionAttributes(DeploymentInfo deploymentInfo, MethodTransactionInfo[] mtis) {
        /*TODO: Add better exception handling.  This method doesn't throws any exceptions!!
         there is a lot of complex code here, I'm sure something could go wrong the user
         might want to know about.
         */
        for (int i = 0; i < mtis.length; i++) {
            MethodTransactionInfo transInfo = mtis[i];
            MethodInfo[] mis = transInfo.methods;

            for (int z = 0; z < mis.length; z++) {
                MethodInfo methodInfo = mis[z];

                if (mis[z].ejbDeploymentId == null || mis[z].ejbDeploymentId.equals(deploymentInfo.getDeploymentID())) {
                    if (!deploymentInfo.isBeanManagedTransaction()) {

                        Vector methodVect = new Vector();

                        if (methodInfo.methodIntf == null) {

                            resolveMethods(methodVect, deploymentInfo.getRemoteInterface(), methodInfo);
                            resolveMethods(methodVect, deploymentInfo.getHomeInterface(), methodInfo);
                        } else if (methodInfo.methodIntf.equals("Home")) {
                            resolveMethods(methodVect, deploymentInfo.getHomeInterface(), methodInfo);
                        } else if (methodInfo.methodIntf.equals("Remote")) {
                            resolveMethods(methodVect, deploymentInfo.getRemoteInterface(), methodInfo);
                        } else {

                        }

                        for (int x = 0; x < methodVect.size(); x++) {
                            Method method = (Method) methodVect.elementAt(x);

                            if ((method.getDeclaringClass() == javax.ejb.EJBObject.class ||
                                    method.getDeclaringClass() == javax.ejb.EJBHome.class) &&
                                    method.getName().equals("remove") == false) {
                                continue;
                            }
                            deploymentInfo.setMethodTransactionAttribute(method, transInfo.transAttribute);
                        }
                    }
                }
            }
        }

    }

    public void applySecurityRoleReference(DeploymentInfo deployment, EnterpriseBeanInfo beanInfo, AssemblerTool.RoleMapping roleMapping) {
        if (beanInfo.securityRoleReferences != null) {
            for (int l = 0; l < beanInfo.securityRoleReferences.length; l++) {
                SecurityRoleReferenceInfo roleRef = beanInfo.securityRoleReferences[l];
                String[] physicalRoles = roleMapping.getPhysicalRoles(roleRef.roleLink);
                deployment.addSecurityRoleReference(roleRef.roleName, physicalRoles);
            }
        }
    }

    public void applyMethodPermissions(DeploymentInfo deployment, MethodPermissionInfo[] permissions) {
        /*TODO: Add better exception handling.  This method doesn't throws any exceptions!!
         there is a lot of complex code here, I'm sure something could go wrong the user
         might want to know about.
         At the very least, log a warning or two.
         */
        for (int a = 0; a < permissions.length; a++) {
            MethodPermissionInfo methodPermission = permissions[a];
            for (int b = 0; b < methodPermission.methods.length; b++) {
                MethodInfo methodInfo = methodPermission.methods[b];

                if (methodInfo.ejbDeploymentId == null || methodInfo.ejbDeploymentId.equals(deployment.getDeploymentID())) {

                    java.lang.reflect.Method[] methods = resolveMethodInfo(methodInfo, deployment);

                    for (int c = 0; c < methods.length; c++) {
                        deployment.appendMethodPermissions(methods[c], methodPermission.roleNames);
                    }
                }

            }
        }
    }

    public void applyMethodPermissions(DeploymentInfo deployment, MethodPermissionInfo[] permissions, AssemblerTool.RoleMapping roleMapping) {
        /*TODO: Add better exception handling.  This method doesn't throws any exceptions!!
         there is a lot of complex code here, I'm sure something could go wrong the user
         might want to know about.
         At the very least, log a warning or two.
         */
        for (int i = 0; i < permissions.length; i++) {
            permissions[i] = applyRoleMappings(permissions[i], roleMapping);
        }
        applyMethodPermissions(deployment, permissions);
    }

    /*
    * Makes a copy of the MethodPermissionObject and then replaces the logical roles of the MethodPermissionInfo copy
    * with the physical roles in the roleMapping object.
    * If the RoleMapping object doesn't have a set of physical roles for a particular logical role in the
    * MethodPermissionInfo, then the logical role is used.
    *
    * @param methodPermission the permission object to be copies and updated.
    * @param roleMapping encapsulates the mapping of many logical roles to their equivalent physical roles.
    * @see org.openejb.assembler.classic.MethodPermissionInfo
    * @see org.openejb.assembler.classic.AssemblerTool.RoleMapping
    */
    public MethodPermissionInfo applyRoleMappings(MethodPermissionInfo methodPermission,
                                                  AssemblerTool.RoleMapping roleMapping) {
        /*TODO: Add better exception handling.  This method doesn't throws any exceptions!!
         there is a lot of complex code here, I'm sure something could go wrong the user
         might want to know about.
         At the very least, log a warning or two.
         */

        HashSet physicalRoles = new HashSet();

        for (int z = 0; z < methodPermission.roleNames.length; z++) {
            String[] physicals = roleMapping.getPhysicalRoles(methodPermission.roleNames[z]);
            if (physicals != null) {
                for (int x = 0; x < physicals.length; x++) {
                    physicalRoles.add(physicals[x]);
                }
            } else {// if no physical roles are mapped use logical role

                physicalRoles.add(methodPermission.roleNames[z]);
            }
        }
        methodPermission.roleNames = new String[physicalRoles.size()];
        physicalRoles.toArray(methodPermission.roleNames);
        return methodPermission;
    }

    public static class RoleMapping {
        private HashMap map = new HashMap();

        public RoleMapping(RoleMappingInfo[] roleMappingInfos) {
            for (int i = 0; i < roleMappingInfos.length; i++) {
                RoleMappingInfo mapping = roleMappingInfos[i];
                for (int z = 0; z < mapping.logicalRoleNames.length; z++) {
                    map.put(mapping.logicalRoleNames[z], mapping.physicalRoleNames);
                }
            }
        }

        public String[] logicalRoles() {
            return (String[]) map.keySet().toArray();
        }

        public String[] getPhysicalRoles(String logicalRole) {
            String[] roles = (String[]) map.get(logicalRole);
            return roles != null ? (String[]) roles.clone() : null;
        }

    }

    protected java.lang.reflect.Method[] resolveMethodInfo(MethodInfo methodInfo, org.openejb.core.DeploymentInfo di) {
        /*TODO: Add better exception handling.  This method doesn't throws any exceptions!!
         there is a lot of complex code here, I'm sure something could go wrong the user
         might want to know about.
         At the very least, log a warning or two.
         */

        Vector methodVect = new Vector();

        Class remote = di.getRemoteInterface();
        Class home = di.getHomeInterface();
        if (methodInfo.methodIntf == null) {
            resolveMethods(methodVect, remote, methodInfo);
            resolveMethods(methodVect, home, methodInfo);
        } else if (methodInfo.methodIntf.equals("Remote")) {
            resolveMethods(methodVect, remote, methodInfo);
        } else {
            resolveMethods(methodVect, home, methodInfo);
        }
        return (java.lang.reflect.Method[]) methodVect.toArray(new java.lang.reflect.Method[methodVect.size()]);
    }

    protected static void resolveMethods(Vector methods, Class intrface, MethodInfo mi)
            throws SecurityException {
        /*TODO: Add better exception handling. There is a lot of complex code here, I'm sure something could go wrong the user
         might want to know about.
         At the very least, log a warning or two.
         */

        if (mi.methodName.equals("*")) {
            Method[] mthds = intrface.getMethods();
            for (int i = 0; i < mthds.length; i++)
                methods.add(mthds[i]);
        } else if (mi.methodParams != null) {// there are paramters specified
            try {
                Class[] params = new Class[mi.methodParams.length];
                ClassLoader cl = intrface.getClassLoader();
                for (int i = 0; i < params.length; i++) {
                    try {
                        params[i] = getClassForParam(mi.methodParams[i], cl);
                    } catch (ClassNotFoundException cnfe) {

                    }
                }
                Method m = intrface.getMethod(mi.methodName, params);
                methods.add(m);
            } catch (NoSuchMethodException nsme) {
                /*
                Do nothing.  Exceptions are not only possible they are expected to be a part of normall processing.
                Normally exception handling should not be a part of business logic, but server start up doesn't need to be
                as peformant as server runtime, so its allowed.
                */
            }
        } else {// no paramters specified so may be several methods
            Method[] ms = intrface.getMethods();
            for (int i = 0; i < ms.length; i++) {
                if (ms[i].getName().equals(mi.methodName))
                    methods.add(ms[i]);
            }
        }

    }

    protected void checkImplementation(Class intrfce, Class factory, String serviceType, String serviceName) throws OpenEJBException {
        if (!intrfce.isAssignableFrom(factory)) {
            handleException("init.0100", serviceType, serviceName, factory.getName(), intrfce.getName());
        }
    }

    private static java.lang.Class getClassForParam(java.lang.String className, ClassLoader cl) throws ClassNotFoundException {
        if (cl == null) {
            cl = ClassLoader.getSystemClassLoader();
        }

        if (className.equals("int")) {
            return java.lang.Integer.TYPE;
        } else if (className.equals("double")) {
            return java.lang.Double.TYPE;
        } else if (className.equals("long")) {
            return java.lang.Long.TYPE;
        } else if (className.equals("boolean")) {
            return java.lang.Boolean.TYPE;
        } else if (className.equals("float")) {
            return java.lang.Float.TYPE;
        } else if (className.equals("char")) {
            return java.lang.Character.TYPE;
        } else if (className.equals("short")) {
            return java.lang.Short.TYPE;
        } else if (className.equals("byte")) {
            return java.lang.Byte.TYPE;
        } else
            return cl.loadClass(className);

    }

    /*------------------------------------------------------*/
    /*    Methods for easy exception handling               */
    /*------------------------------------------------------*/
    public void handleException(String errorCode, Object arg0, Object arg1, Object arg2, Object arg3) throws OpenEJBException {
        throw new OpenEJBException(messages.format(errorCode, arg0, arg1, arg2, arg3));
    }

    public void handleException(String errorCode, Object arg0, Object arg1, Object arg2) throws OpenEJBException {
        throw new OpenEJBException(messages.format(errorCode, arg0, arg1, arg2));
    }

    public void handleException(String errorCode, Object arg0, Object arg1) throws OpenEJBException {
        throw new OpenEJBException(messages.format(errorCode, arg0, arg1));
    }

    public void handleException(String errorCode, Object arg0) throws OpenEJBException {
        throw new OpenEJBException(messages.format(errorCode, arg0));
    }

    public void handleException(String errorCode) throws OpenEJBException {
        throw new OpenEJBException(messages.format(errorCode));
    }

    /*------------------------------------------------------*/
    /*  Methods for logging exceptions that are noteworthy  */
    /*  but not bad enough to stop the container system.    */
    /*------------------------------------------------------*/
    public void logWarning(String errorCode, Object arg0, Object arg1, Object arg2, Object arg3) {
        System.out.println("Warning: " + messages.format(errorCode, arg0, arg1, arg2, arg3));
    }

    public void logWarning(String errorCode, Object arg0, Object arg1, Object arg2) {
        System.out.println("Warning: " + messages.format(errorCode, arg0, arg1, arg2));
    }

    public void logWarning(String errorCode, Object arg0, Object arg1) {
        System.out.println("Warning: " + messages.format(errorCode, arg0, arg1));
    }

    public void logWarning(String errorCode, Object arg0) {
        System.out.println("Warning: " + messages.format(errorCode, arg0));
    }

    public void logWarning(String errorCode) {
        System.out.println("Warning: " + messages.format(errorCode));
    }
}