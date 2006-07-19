package org.openejb.assembler.spring;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import javax.transaction.TransactionManager;

import org.apache.xbean.recipe.ConstructionException;
import org.apache.xbean.recipe.ObjectRecipe;
import org.apache.xbean.recipe.StaticRecipe;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.apache.xbean.spring.context.SpringApplicationContext;
import org.openejb.Container;
import org.openejb.OpenEJBException;
import org.openejb.RpcContainer;
import org.openejb.core.DeploymentInfo;
import org.openejb.core.ContainerSystem;
import org.openejb.spi.SecurityService;
import org.openejb.util.Logger;
import org.openejb.util.OpenEJBErrorHandler;
import org.openejb.util.proxy.ProxyFactory;
import org.openejb.util.proxy.ProxyManager;

public class Assembler implements org.openejb.spi.Assembler {
    private static final Logger logger = Logger.getInstance("OpenEJB", "org.openejb.util.resources");

    public static final String CONTAINER_DECORATORS = "openejb.container.decorators";

    private ProxyFactory proxyFactory;
    private ContainerSystem containerSystem;
    private TransactionManager transactionManager;
    private SecurityService securityService;

    private String[] decorators;

    public String[] getDecorators() {
        return decorators;
    }

    public void setDecorators(String[] decorators) {
        this.decorators = decorators;
    }

    public ProxyFactory getProxyFactory() {
        return proxyFactory;
    }

    private void setProxyFactory(ProxyFactory proxyFactory) {
        this.proxyFactory = proxyFactory;
        ProxyManager.registerFactory("ivm_server", this.proxyFactory);
        ProxyManager.setDefaultFactory("ivm_server");
    }

    public ContainerSystem getContainerSystem() {
        return containerSystem;
    }

    private void setContainerSystem(ContainerSystem containerSystem) {
        this.containerSystem = containerSystem;
    }

    public SecurityService getSecurityService() {
        return securityService;
    }

    private void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    private void setTransactionManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public void init(Properties props) throws OpenEJBException {
        if (props.contains(CONTAINER_DECORATORS)) {
            decorators = props.getProperty(CONTAINER_DECORATORS).split(" *, *");
        }

        AssemblerUtil.addSystemJndiProperties();
    }

    public void build() throws OpenEJBException {
        try {
            String springXml = System.getProperty("openejb.spring.conf", "META-INF/org.openejb/spring.xml");
            containerSystem = buildContainerSystem(springXml);
        } catch (OpenEJBException e) {
            //
            // OpenEJBExceptions contain useful information and are debbugable.
            // Let the exception pass through to the top and be logged.
            //
            throw e;
        } catch (Exception e) {
            //
            // General Exceptions at this level are too generic and difficult to debug.
            // These exceptions are considered unknown bugs and are fatal.
            // If you get an error at this level, please trap and handle the error
            // where it is most relevant.
            //
            OpenEJBErrorHandler.handleUnknownError(e, "Assembler");
            throw new OpenEJBException(e);
        }
    }

    /**
     * When given a complete OpenEjbConfiguration graph this method,
     * will construct an entire container system and return a reference to that
     * container system, as ContainerSystem instance.
     *
     * @return ContainerSystem the container system
     * @throws Exception if there was a problem constructing the ContainerSystem.
     * @param springConfigLocation the location of the spring configuration file
     */
    public ContainerSystem buildContainerSystem(String springConfigLocation) throws Exception {
        //
        // Load the spring configuration file
        //
        SpringApplicationContext factory = new ClassPathXmlApplicationContext(springConfigLocation);

        //
        // Proxy factory
        //
        //  This operation must take place first because of interdependencies.
        //  As DeploymentInfo objects are registered with the ContainerSystem using the
        //  ContainerSystem.addDeploymentInfo() method, they are also added to the JNDI
        //  Naming Service for OpenEJB.  This requires that a proxy for the deployed bean's
        //  EJBHome be created. The proxy requires that the default proxy factory is set.
        //
        ProxyFactory proxyFactory = AssemblerUtil.getBean(factory, ProxyFactory.class);
        setProxyFactory(proxyFactory);

        //
        // Container system
        //
        ContainerSystem containerSystem = AssemblerUtil.getBean(factory, org.openejb.core.ContainerSystem.class);
        setContainerSystem(containerSystem);

        //
        // Transaction manager
        //
        TransactionManager transactionManager = AssemblerUtil.getBean(factory, TransactionManager.class);
        setTransactionManager(transactionManager);

        //
        // Security system
        //
        SecurityService securityService = AssemblerUtil.getBean(factory, SecurityService.class);
        setSecurityService(securityService);

        //
        // Create the containers
        //
        List<Container> containers = wrapContainers(AssemblerUtil.getBeans(factory, Container.class));
        List<DeploymentInfo> deployments = new ArrayList<DeploymentInfo>();
        for (Container container : containers) {
            containerSystem.addContainer(container.getContainerID(), container);
            for (org.openejb.DeploymentInfo d : container.deployments()) {
                DeploymentInfo deployment = (DeploymentInfo) d;
                deployment.setContainer(container);
                deployments.add(deployment);
                containerSystem.addDeployment(deployment);
            }
        }

        //
        // Transaction attributes
        //
        AssemblyInfo assemblyInfo = AssemblerUtil.getBean(factory, AssemblyInfo.class);
        for (DeploymentInfo deployment : deployments) {
            applyTransactionAttributes(deployment, assemblyInfo.methodTransactions);
        }

        //
        // Method permisions
        //
        for (DeploymentInfo deployment : deployments) {
            applyMethodPermissions(deployment, assemblyInfo);
        }

        return containerSystem;
    }

    protected List<Container> wrapContainers(List<Container> containers) {
        List<Container> wrappedContainers = new ArrayList<Container>(containers.size());
        for (Container container : containers) {
            if (container instanceof RpcContainer) {
                for (String decorator : AssemblerUtil.asList(decorators)) {
                    try {
                        ObjectRecipe decoratorRecipe = new ObjectRecipe(decorator,new String[]{"container"}, null);
                        decoratorRecipe.setProperty("container", new StaticRecipe(container));
                        container = (Container) decoratorRecipe.create();
                    } catch (ConstructionException e) {
                        logger.error("Container wrapper class " + decorator + " could not be constructed and will be skipped.", e);
                    }
                }
            }
            wrappedContainers.add(container);
        }
        return wrappedContainers;
    }

    protected void applyTransactionAttributes(DeploymentInfo deploymentInfo, MethodTransactionInfo[] transactionInfos) {
        if (deploymentInfo.isBeanManagedTransaction()) {
            // deployments with bean managed transactions don't have transaction attributes
            return;
        }
        for (org.openejb.assembler.spring.MethodTransactionInfo transactionInfo : AssemblerUtil.asList(transactionInfos)) {
            for (MethodInfo methodInfo : AssemblerUtil.asList(transactionInfo.methods)) {
                if (methodInfo.deploymentId == null || methodInfo.deploymentId.equals(deploymentInfo.getDeploymentID())) {
                    // find all the methods matching the method spec
                    List<Method> methods = resolveMethods(deploymentInfo, methodInfo);
                    for (Method method : methods) {
                        // not all methods can have a transaction attribute
                        if (isValidTxMethod(method)) {
                            deploymentInfo.setMethodTransactionAttribute(method, transactionInfo.transAttribute);
                        }
                    }
                }
            }
        }
    }

    protected boolean isValidTxMethod(Method method) {
        // bean methods are alwas valid
        if (method.getDeclaringClass() != javax.ejb.EJBObject.class &&
                method.getDeclaringClass() != javax.ejb.EJBHome.class &&
                method.getDeclaringClass() != javax.ejb.EJBLocalObject.class &&
                method.getDeclaringClass() != javax.ejb.EJBLocalHome.class) {
            return true;
        }

        // the only valid EJBObject and EJBHome method is remove
        return method.getName().equals("remove");
    }

    protected void applyMethodPermissions(DeploymentInfo deploymentInfo, AssemblyInfo assemblyInfo) {
        Map<String, String[]> roleMappings = new TreeMap<String, String[]>();
        for (org.openejb.assembler.spring.RoleMapping roleMapping : AssemblerUtil.asList(assemblyInfo.roleMappings)) {
            roleMappings.put(roleMapping.logical, roleMapping.physical);
        }

        for (MethodPermissionInfo methodPermission : assemblyInfo.methodPermissions) {
            for (MethodInfo methodInfo : methodPermission.methods) {
                if (methodInfo.deploymentId == null || methodInfo.deploymentId.equals(deploymentInfo.getDeploymentID())) {
                    List<Method> methods = resolveMethods(deploymentInfo, methodInfo);
                    for (Method method : methods) {
                        deploymentInfo.appendMethodPermissions(method, mapRoleNames(methodPermission.roleNames, roleMappings));
                    }
                }
            }
        }
    }

    protected String[] mapRoleNames(String[] names, Map<String, String[]> roleMappings) {
        List<String> physicalRoles = new ArrayList<String>(names.length);
        for (String logical : names) {
            String[] physical = roleMappings.get(logical);
            if (physical == null) {
                physicalRoles.add(logical);
            } else {
                physicalRoles.addAll(Arrays.asList(physical));
            }
        }
        return physicalRoles.toArray(new String[physicalRoles.size()]);
    }

    protected List<Method> resolveMethods(DeploymentInfo deploymentInfo, MethodInfo methodInfo) {
        List<Method> methods = new ArrayList<Method>();
        if (methodInfo.intf == null) {
            resolveMethods(methods, deploymentInfo.getRemoteInterface(), methodInfo);
            resolveMethods(methods, deploymentInfo.getHomeInterface(), methodInfo);
        } else if (methodInfo.intf.equals("Home")) {
            resolveMethods(methods, deploymentInfo.getHomeInterface(), methodInfo);
        } else if (methodInfo.intf.equals("Remote")) {
            resolveMethods(methods, deploymentInfo.getRemoteInterface(), methodInfo);
        } else {

        }
        return methods;
    }

    protected void resolveMethods(List<Method> methods, Class intf, MethodInfo methodInfo) {
        if (methodInfo.name.equals("*")) {
            for (Method method : intf.getMethods()) {
                methods.add(method);
            }
        } else if (methodInfo.params != null) {
            // specific parameters types specified
            MethodSignature signature = new MethodSignature(methodInfo.name, methodInfo.params);
            Method method = signature.getMethod(intf);
            methods.add(method);
        } else {
            // no paramters specified so may be several methods
            for (Method method : intf.getMethods()) {
                if (method.getName().equals(methodInfo.name)) {
                    methods.add(method);
                }
            }
        }
    }
}
