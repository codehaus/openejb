package org.openejb.alt.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.io.FileNotFoundException;
import java.util.Properties;
import java.util.Vector;
import java.util.List;
import java.util.ArrayList;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.exolab.castor.util.Configuration;
import org.exolab.castor.util.LocalConfiguration;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.openejb.OpenEJBException;
import org.openejb.jee.EjbJar;
import org.openejb.jee.EnterpriseBean;
import org.openejb.alt.config.ejb.OpenejbJar;
import org.openejb.alt.config.sys.Container;
import org.openejb.loader.SystemInstance;
import org.openejb.util.JarUtils;
import org.openejb.util.Logger;
import org.openejb.util.Messages;

public class EjbJarUtils {

    public static final Messages messages = new Messages("org.openejb.util.resources");
    public static final Logger logger = Logger.getInstance("OpenEJB.startup", "org.openejb.util.resources");

    private final EjbJar ejbJar;
    private final String jarLocation;
    private OpenejbJar openejbJar;

    static {
        Properties properties = LocalConfiguration.getInstance().getProperties();
        properties.setProperty(Configuration.Property.Indent, "true");
    }

    public EjbJarUtils(String jarLocation) throws OpenEJBException {
        /*[1.1]  Get the jar ***************/
        this.jarLocation = jarLocation;
        this.ejbJar = readEjbJar(jarLocation);
        try {
            this.openejbJar = readOpenEjbJar(jarLocation);
        } catch (OpenEJBException e) {
            if (e.getCause() instanceof FileNotFoundException){
                logger.warning(e.getMessage());
            } else {
                logger.warning("Reading openejb-jar.xml.", e);
            }
        }
    }

    private OpenejbJar readOpenEjbJar(String jarLocation) throws OpenEJBException {
        return (OpenejbJar) JaxbUnmarshaller.unmarshal(OpenejbJar.class, "META-INF/openejb-jar.xml", jarLocation);
    }

    private EjbJar readEjbJar(String jarLocation) throws OpenEJBException {
        return (EjbJar) JaxbUnmarshaller.unmarshal(EjbJar.class, "META-INF/ejb-jar.xml", jarLocation);
    }

    public String getJarLocation() {
        return jarLocation;
    }

    public EjbJar getEjbJar() {
        return ejbJar;
    }

    public OpenejbJar getOpenejbJar() {
        return openejbJar;
    }

    public void setOpenejbJar(OpenejbJar openejbJar) {
        this.openejbJar = openejbJar;
    }

    public void writeEjbJar(String xmlFile) throws OpenEJBException {
//        /* TODO:  Just to be picky, the xml file created by
//        Castor is really hard to read -- it is all on one line.
//        People might want to edit this in the future by hand, so if Castor can
//        make the output look better that would be great!  Otherwise we could
//        just spruce the output up by adding a few new lines and tabs.
//        */
//        Writer writer = null;
//        try {
//            File file = new File(xmlFile);
//            writer = new FileWriter(file);
//            ejbJar.marshal(writer);
//        } catch (IOException e) {
//            throw new OpenEJBException(messages.format("conf.3040", xmlFile, e.getLocalizedMessage()));
//        } catch (MarshalException e) {
//            if (e.getCause() instanceof IOException) {
//                throw new OpenEJBException(messages.format("conf.3040", xmlFile, e.getLocalizedMessage()));
//            } else {
//                throw new OpenEJBException(messages.format("conf.3050", xmlFile, e.getLocalizedMessage()));
//            }
//        } catch (ValidationException e) {
//            /* TODO: Implement informative error handling here.
//               The exception will say "X doesn't match the regular
//               expression Y"
//               This should be checked and more relevant information
//               should be given -- not everyone understands regular
//               expressions.
//             */
//            /* NOTE: This doesn't seem to ever happen. When the object graph
//             * is invalid, the MarshalException is thrown, not this one as you
//             * would think.
//             */
//            throw new OpenEJBException(messages.format("conf.3060", xmlFile, e.getLocalizedMessage()));
//        }
//        try {
//            writer.close();
//        } catch (Exception e) {
//            throw new OpenEJBException(messages.format("file.0020", xmlFile, e.getLocalizedMessage()));
//        }
    }

    public static String moveJar(String jar, boolean overwrite) throws OpenEJBException {
        File origFile = new File(jar);

        if (!origFile.exists()) {
            handleException("deploy.m.010", origFile.getAbsolutePath());
        }

        if (origFile.isDirectory()) {
            handleException("deploy.m.020", origFile.getAbsolutePath());
        }

        if (!origFile.isFile()) {
            handleException("deploy.m.030", origFile.getAbsolutePath());
        }

        String jarName = origFile.getName();
        File beansDir = null;
        try {
            beansDir = SystemInstance.get().getBase().getDirectory("beans");
        } catch (java.io.IOException ioe) {
            throw new OpenEJBException(messages.format("deploy.m.040", origFile.getAbsolutePath(), ioe.getMessage()));
        }

        File newFile = new File(beansDir, jarName);
        boolean moved = false;

        try {
            if (newFile.exists()) {
                if (overwrite) {
                    newFile.delete();
                } else {
                    throw new OpenEJBException(messages.format("deploy.m.061", origFile.getAbsolutePath(), beansDir.getAbsolutePath()));
                }
            }
            moved = origFile.renameTo(newFile);
        } catch (SecurityException se) {
            throw new OpenEJBException(messages.format("deploy.m.050", origFile.getAbsolutePath(), se.getMessage()));
        }

        if (!moved) {
            throw new OpenEJBException(messages.format("deploy.m.060", origFile.getAbsolutePath(), newFile.getAbsoluteFile()));
        }
        return newFile.getAbsolutePath();
    }

    public static String copyJar(String jar, boolean overwrite) throws OpenEJBException {
        File origFile = new File(jar);

        if (!origFile.exists()) {
            handleException("deploy.c.010", origFile.getAbsolutePath());
            return jar;
        }

        if (origFile.isDirectory()) {
            handleException("deploy.c.020", origFile.getAbsolutePath());
            return jar;
        }

        if (!origFile.isFile()) {
            handleException("deploy.c.030", origFile.getAbsolutePath());
            return jar;
        }

        String jarName = origFile.getName();
        File beansDir = null;
        try {
            beansDir = SystemInstance.get().getBase().getDirectory("beans");
        } catch (java.io.IOException ioe) {
            throw new OpenEJBException(messages.format("deploy.c.040", origFile.getAbsolutePath(), ioe.getMessage()));
        }

        File newFile = new File(beansDir, jarName);

        try {
            if (newFile.exists()) {
                if (overwrite) {
                    newFile.delete();
                } else {
                    throw new OpenEJBException(messages.format("deploy.c.061", origFile.getAbsolutePath(), beansDir.getAbsolutePath()));
                }
            }

            FileInputStream in = new FileInputStream(origFile);
            FileOutputStream out = new FileOutputStream(newFile);

            int b = in.read();
            while (b != -1) {
                out.write(b);
                b = in.read();
            }

            in.close();
            out.close();

        } catch (SecurityException e) {
            throw new OpenEJBException(messages.format("deploy.c.050", origFile.getAbsolutePath(), beansDir.getAbsolutePath(), e.getMessage()));
        } catch (IOException e) {
            handleException("deploy.c.060", origFile.getAbsolutePath(), newFile.getAbsolutePath(), e.getClass().getName(), e.getMessage());
        }

        return newFile.getAbsolutePath();
    }

    public static Container[] getUsableContainers(Container[] containers, Bean bean) {
        Vector c = new Vector();

        for (int i = 0; i < containers.length; i++) {
            if (containers[i].getCtype().equals(bean.getType())) {
                c.add(containers[i]);
            }
        }

        Container[] useableContainers = new Container[c.size()];
        c.copyInto(useableContainers);

        return useableContainers;
    }

    public Bean[] getBeans() {
        List<Bean> beans = new ArrayList<Bean>();
        for (EnterpriseBean enterpriseBean : ejbJar.getEnterpriseBeans()) {
            if (enterpriseBean instanceof org.openejb.jee.EntityBean) {
                beans.add(new EntityBean((org.openejb.jee.EntityBean) enterpriseBean));
            } else if (enterpriseBean instanceof org.openejb.jee.SessionBean) {
                beans.add(new SessionBean((org.openejb.jee.SessionBean) enterpriseBean));
            }
        }
        return beans.toArray(new Bean[]{});
    }

    /*------------------------------------------------------*/
    /*    Methods for easy exception handling               */
    /*------------------------------------------------------*/
    public static void handleException(String errorCode, Object arg0, Object arg1, Object arg2, Object arg3) throws OpenEJBException {
        throw new OpenEJBException(messages.format(errorCode, arg0, arg1, arg2, arg3));
    }

    public static void handleException(String errorCode, Object arg0) throws OpenEJBException {
        throw new OpenEJBException(messages.format(errorCode, arg0));
    }

    public static void handleException(String errorCode) throws OpenEJBException {
        throw new OpenEJBException(messages.message(errorCode));
    }

    public static boolean checkForOpenejbJar(String jarFile) throws OpenEJBException {
        /*[1.1]  Get the jar ***************/
        JarFile jar = JarUtils.getJarFile(jarFile);

        /*[1.2]  Find the openejb-jar.xml from the jar ***************/
        JarEntry entry = jar.getJarEntry("META-INF/openejb-jar.xml");
        if (entry == null) entry = jar.getJarEntry("openejb-jar.xml");
        if (entry == null) return false;

        return true;
    }

    public static void writeOpenejbJar(String xmlFile, OpenejbJar openejbJarObject) throws OpenEJBException {
//        /* TODO:  Just to be picky, the xml file created by
//        Castor is really hard to read -- it is all on one line.
//        People might want to edit this in the future by hand, so if Castor can
//        make the output look better that would be great!  Otherwise we could
//        just spruce the output up by adding a few new lines and tabs.
//        */
//        Writer writer = null;
//        try {
//            File file = new File(xmlFile);
//            File dirs = file.getParentFile();
//            if (dirs != null) dirs.mkdirs();
//            writer = new FileWriter(file);
//            openejbJarObject.marshal(writer);
//        } catch (SecurityException e) {
//            throw new OpenEJBException(messages.format("conf.2040", xmlFile, e.getLocalizedMessage()));
//        } catch (IOException e) {
//            throw new OpenEJBException(messages.format("conf.2040", xmlFile, e.getLocalizedMessage()));
//        } catch (MarshalException e) {
//            if (e.getCause() instanceof IOException) {
//                throw new OpenEJBException(messages.format("conf.2040", xmlFile, e.getLocalizedMessage()));
//            } else {
//                throw new OpenEJBException(messages.format("conf.2050", xmlFile, e.getLocalizedMessage()));
//            }
//        } catch (ValidationException e) {
//            /* TODO: Implement informative error handling here.
//               The exception will say "X doesn't match the regular
//               expression Y"
//               This should be checked and more relevant information
//               should be given -- not everyone understands regular
//               expressions.
//             */
//            /* NOTE: This doesn't seem to ever happen. When the object graph
//             * is invalid, the MarshalException is thrown, not this one as you
//             * would think.
//             */
//            throw new OpenEJBException(messages.format("conf.2060", xmlFile, e.getLocalizedMessage()));
//        }
//        try {
//            writer.close();
//        } catch (Exception e) {
//            throw new OpenEJBException(messages.format("file.0020", xmlFile, e.getLocalizedMessage()));
//        }
    }
}