package org.openejb.alt.config;

import java.lang.reflect.Method;

import org.openejb.jee.ResourceRef;
import org.openejb.OpenEJBException;
import org.openejb.alt.config.ejb.EjbDeployment;
import org.openejb.alt.config.ejb.OpenejbJar;
import org.openejb.alt.config.ejb.ResourceLink;
import org.openejb.alt.config.sys.Connector;
import org.openejb.alt.config.sys.Container;
import org.openejb.alt.config.sys.Openejb;
import org.openejb.util.SafeToolkit;

public class AutoDeployer implements DynamicDeployer {

    private Openejb config;
    private String configFile;
    private Container[] containers;
    private Connector[] resources;
    private ClassLoader classLoader;
    private String jarLocation;

    public AutoDeployer(Openejb config) {
        this.config = config;

        /* Load container list */
        this.containers = config.getContainer();

        /* Load resource list */
        this.resources = config.getConnector();
    }

    public void init() throws OpenEJBException {
    }

    public OpenejbJar deploy(EjbJarUtils ejbJarUtils, String jarLocation, ClassLoader classLoader) throws OpenEJBException {
        if (ejbJarUtils.getOpenejbJar() != null){
            return ejbJarUtils.getOpenejbJar();
        }

        this.jarLocation = jarLocation;
        this.classLoader = classLoader;
        OpenejbJar openejbJar = new OpenejbJar();

        Bean[] beans = ejbJarUtils.getBeans();

        for (int i = 0; i < beans.length; i++) {
            openejbJar.getEjbDeployment().add(deployBean(beans[i], jarLocation));
        }
        return openejbJar;
    }

    private EjbDeployment deployBean(Bean bean, String jarLocation) throws OpenEJBException {
        EjbDeployment deployment = new EjbDeployment();

        deployment.setEjbName(bean.getEjbName());

        deployment.setDeploymentId(autoAssignDeploymentId(bean));

        deployment.setContainerId(autoAssignContainerId(bean));

        ResourceRef[] refs = bean.getResourceRef();

        if (refs.length > 1) {
            throw new OpenEJBException("Beans with more that one resource-ref cannot be autodeployed;  there is no accurate way to determine how the references should be mapped.");
        }

        for (int i = 0; i < refs.length; i++) {
            deployment.getResourceLink().add(autoAssingResourceRef(refs[i]));
        }

        if (bean.getType().equals("CMP_ENTITY")) {
            if (bean.getHome() != null) {
                Class tempBean = loadClass(bean.getHome());
                if (hasFinderMethods(tempBean)) {
                    throw new OpenEJBException("CMP 1.1 Beans with finder methods cannot be autodeployed; finder methods require OQL Select statements which cannot be generated accurately.");
                }
            }
            if (bean.getLocalHome() != null) {
                Class tempBean = loadClass(bean.getLocalHome());
                if (hasFinderMethods(tempBean)) {
                    throw new OpenEJBException("CMP 1.1 Beans with finder methods cannot be autodeployed; finder methods require OQL Select statements which cannot be generated accurately.");
                }
            }
        }

        return deployment;
    }

    private Class loadClass(String className) throws OpenEJBException {
        try {
            return classLoader.loadClass(className);
        } catch (ClassNotFoundException cnfe) {
            throw new OpenEJBException(SafeToolkit.messages.format("cl0007", className, this.jarLocation));
        }
    }

    private boolean hasFinderMethods(Class bean)
            throws OpenEJBException {

        Method[] methods = bean.getMethods();

        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().startsWith("find")
                    && !methods[i].getName().equals("findByPrimaryKey")) {
                return true;
            }
        }
        return false;
    }

    private String autoAssignDeploymentId(Bean bean) throws OpenEJBException {
        return bean.getEjbName();
    }

    private String autoAssignContainerId(Bean bean) throws OpenEJBException {
        String answer = null;
        boolean replied = false;

        Container[] cs = getUsableContainers(bean);

        if (cs.length == 0) {
            throw new OpenEJBException("A container of type " + bean.getType() + " must be declared in the configuration file.");
        }
        return cs[0].getId();
    }

    private ResourceLink autoAssingResourceRef(ResourceRef ref) throws OpenEJBException {
        if (resources.length == 0) {
            throw new OpenEJBException("A Connector must be declared in the configuration file to satisfy the resource-ref " + ref.getResRefName());
        }

        ResourceLink link = new ResourceLink();
        link.setResRefName(ref.getResRefName());
        link.setResId(resources[0].getId());
        return link;
    }

    private Container[] getUsableContainers(Bean bean) {
        return EjbJarUtils.getUsableContainers(containers, bean);
    }
}
