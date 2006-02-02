package org.openejb.util;

import org.openejb.OpenEJBException;

import java.io.File;
import java.util.HashMap;
import java.util.Properties;

public class SafeToolkit {

    private String systemLocation;
    public static final Messages messages = new Messages("org.openejb.util.resources");
    public static final HashMap codebases = new HashMap();

    protected SafeToolkit(String systemLocation) {
        this.systemLocation = systemLocation;
    }

    public static SafeToolkit getToolkit(String systemLocation) {
        return new SafeToolkit(systemLocation);
    }

    public Class forName(String className) throws OpenEJBException {
        Class clazz = null;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException cnfe) {
            OpenEJBErrorHandler.classNotFound(systemLocation, className);
        }
        return clazz;
    }

    public Class forName(String className, String codebase) throws OpenEJBException {

        ClassLoader cl = getContextClassLoader();

        if (codebase != null) {
            try {
                java.net.URL[] urlCodebase = new java.net.URL[1];
                urlCodebase[0] = new java.net.URL(codebase);
                cl = new java.net.URLClassLoader(urlCodebase, cl);
            } catch (java.net.MalformedURLException mue) {
                OpenEJBErrorHandler.classCodebaseNotFound(systemLocation, className, codebase, mue);
            } catch (SecurityException se) {
                OpenEJBErrorHandler.classCodebaseNotFound(systemLocation, className, codebase, se);
            }
        }

        Class clazz = null;
        try {
            clazz = Class.forName(className, true, cl);
        } catch (ClassNotFoundException cnfe) {
            OpenEJBErrorHandler.classNotFound(systemLocation, className);
        }
        return clazz;
    }

    public Object newInstance(String className) throws OpenEJBException {
        return newInstance(forName(className));
    }

    public Object newInstance(String className, String codebase) throws OpenEJBException {
        return newInstance(forName(className, codebase));
    }

    public Object newInstance(Class clazz) throws OpenEJBException {
        Object instance = null;
        try {
            instance = clazz.newInstance();
        } catch (InstantiationException ie) {
            OpenEJBErrorHandler.classNotIntantiateable(systemLocation, clazz.getName());
        } catch (IllegalAccessException iae) {
            OpenEJBErrorHandler.classNotAccessible(systemLocation, clazz.getName());
        }

        catch (Throwable exception) {
            exception.printStackTrace();
            ClassLoader classLoader = clazz.getClassLoader();
            if (classLoader instanceof java.net.URLClassLoader) {
                OpenEJBErrorHandler.classNotIntantiateableFromCodebaseForUnknownReason(systemLocation, clazz.getName(), getCodebase((java.net.URLClassLoader) classLoader),
                        exception.getClass().getName(), exception.getMessage());
            } else {
                OpenEJBErrorHandler.classNotIntantiateableForUnknownReason(systemLocation, clazz.getName(), exception.getClass().getName(), exception.getMessage());
            }
        }
        return instance;

    }

    public SafeProperties getSafeProperties(Properties props) throws OpenEJBException {
        return new SafeProperties(props, systemLocation);
    }

    public static Class loadClass(String className, String codebase) throws OpenEJBException {
        return loadClass(className, codebase, true);
    }

    public static Class loadClass(String className, String codebase, boolean cache) throws OpenEJBException {

        ClassLoader cl = (cache) ? getCodebaseClassLoader(codebase) : getClassLoader(codebase);
        Class clazz = null;
        try {
            clazz = cl.loadClass(className);
        } catch (ClassNotFoundException cnfe) {
            throw new OpenEJBException(messages.format("cl0007", className, codebase));
        }
        return clazz;
    }

    public static ClassLoader getCodebaseClassLoader(String codebase) throws OpenEJBException {
        if (codebase == null) codebase = "CLASSPATH";

        ClassLoader cl = (ClassLoader) codebases.get(codebase);
        if (cl == null) {
            synchronized (codebases) {
                cl = (ClassLoader) codebases.get(codebase);
                if (cl == null) {
                    try {
                        java.net.URL[] urlCodebase = new java.net.URL[1];
                        urlCodebase[0] = new java.net.URL("file", null, codebase);
// make sure everything works if we were not loaded by the system class loader
                        cl = new java.net.URLClassLoader(urlCodebase, SafeToolkit.class.getClassLoader());
//cl = SafeToolkit.class.getClassLoader();
                        codebases.put(codebase, cl);
                    } catch (java.net.MalformedURLException mue) {
                        throw new OpenEJBException(messages.format("cl0001", codebase, mue.getMessage()));
                    } catch (SecurityException se) {
                        throw new OpenEJBException(messages.format("cl0002", codebase, se.getMessage()));
                    }
                }
            }
        }
        return cl;
    }

    public static ClassLoader getClassLoader(String codebase) throws OpenEJBException {
        ClassLoader cl = null;
        try {
            java.net.URL[] urlCodebase = new java.net.URL[1];
            urlCodebase[0] = new java.net.URL("file", null, codebase);

            cl = new java.net.URLClassLoader(urlCodebase, SafeToolkit.class.getClassLoader());
        } catch (java.net.MalformedURLException mue) {
            throw new OpenEJBException(messages.format("cl0001", codebase, mue.getMessage()));
        } catch (SecurityException se) {
            throw new OpenEJBException(messages.format("cl0002", codebase, se.getMessage()));
        }
        return cl;
    }

    private static String getCodebase(java.net.URLClassLoader urlClassLoader) {
        StringBuffer codebase = new StringBuffer();
        java.net.URL urlList[] = urlClassLoader.getURLs();
        codebase.append(urlList[0].toString());
        for (int i = 1; i < urlList.length; ++i) {
            codebase.append(';');
            codebase.append(urlList[i].toString());
        }
        return codebase.toString();
    }

    public static ClassLoader getContextClassLoader() {
        return (ClassLoader) java.security.AccessController.doPrivileged(new java.security.PrivilegedAction() {
            public Object run() {
                return Thread.currentThread().getContextClassLoader();
            }
        });
    }

}