package org.openejb.server;

import org.openejb.loader.SystemInstance;
import org.openejb.loader.FileUtils;
import org.openejb.util.Logger;
import org.openejb.util.Messages;
import org.apache.xbean.finder.ResourceFinder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Properties;
import java.util.Map;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
public class ServiceManager {

    static Messages messages = new Messages("org.openejb.server.util.resources");
    static Logger logger = Logger.getInstance("OpenEJB.server.remote", "org.openejb.server.util.resources");

    private static ServiceManager manager;

    private static HashMap propsByFile = new HashMap();
    private static HashMap fileByProps = new HashMap();

    private static ServerService[] daemons;

    private boolean stop = false;
    private final ResourceFinder resourceFinder;

    private ServiceManager() {
        resourceFinder = new ResourceFinder("META-INF/");
    }

    public static ServiceManager getManager() {
        if (manager == null) {
            manager = new ServiceManager();
        }

        return manager;
    }

    // Have properties files (like xinet.d) that specifies what daemons to 
    // Look into the xinet.d file structure again
    // conf/server.d/
    //    admin.properties
    //    ejbd.properties
    //    webadmin.properties
    //    telnet.properties
    //    corba.properties
    //    soap.properties
    //    xmlrpc.properties
    //    httpejb.properties
    //    webejb.properties
    //    xmlejb.properties
    // Each contains the class name of the daemon implamentation
    // The port to use
    // whether it's turned on


    // May be reusable elsewhere, move if another use occurs
    public static class ServiceFinder {
        private final ResourceFinder resourceFinder;
        private ClassLoader classLoader;

        public ServiceFinder(String basePath) {
            this(basePath, Thread.currentThread().getContextClassLoader());
        }

        public ServiceFinder(String basePath, ClassLoader classLoader) {
            this.resourceFinder = new ResourceFinder(basePath, classLoader);
            this.classLoader = classLoader;
        }

        public Map mapAvailableServices(Class interfase) throws IOException, ClassNotFoundException {
            Map services = resourceFinder.mapAvailableProperties(ServerService.class.getName());

            for (Iterator iterator = services.entrySet().iterator(); iterator.hasNext();) {
                Map.Entry entry = (Map.Entry) iterator.next();
                String name = (String) entry.getKey();
                Properties properties = (Properties) entry.getValue();

                String className = properties.getProperty("className");
                if (className == null) {
                    className = properties.getProperty("classname");
                    if (className == null) {
                        className = properties.getProperty("server");
                    }
                }

                Class impl = classLoader.loadClass(className);

                if (!interfase.isAssignableFrom(impl)) {
                    services.remove(name);
                    continue;
                }

                properties.put(interfase, impl);
                String rawProperties = resourceFinder.findString(interfase.getName() + "/" + name);
                properties.put(Properties.class, rawProperties);

            }
            return services;
        }
    }

    public void init() throws Exception {
        try {
            org.apache.log4j.MDC.put("SERVER", "main");
            InetAddress localhost = InetAddress.getLocalHost();
            org.apache.log4j.MDC.put("HOST", localhost.getHostName());
        } catch (Exception e) {
        }

        ServiceFinder serviceFinder = new ServiceFinder("META-INF/");

        Map availableServices = serviceFinder.mapAvailableServices(ServerService.class);
        List enabledServers = new ArrayList();

        for (Iterator iterator = availableServices.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String serviceName = (String) entry.getKey();
            Properties serviceProperties = (Properties) entry.getValue();

            overrideProperties(serviceName, serviceProperties);
            serviceProperties.setProperty("name", serviceName);

            if (isEnabled(serviceProperties)) {

                // Create Service
                ServerService service = null;

                Class serviceClass = (Class) serviceProperties.get(ServerService.class);

        try {
                    service = (ServerService) serviceClass.newInstance();
                } catch (Throwable t) {
                    String msg1 = messages.format("service.instantiation.err", serviceClass.getName(), t.getClass().getName(), t.getMessage());
                    throw new ServiceException(msg1, t);
                }

                // Wrap Service
                service = new ServiceLogger(service);
                service = new ServiceAccessController(service);
                service = new ServiceDaemon(service);

                // Initialize it
                service.init(serviceProperties);
                enabledServers.add(service);
            }

        }

        daemons = (ServerService[]) enabledServers.toArray(new ServerService[]{});
    }

    private void overrideProperties(String serviceName, Properties serviceProperties) throws IOException {
        FileUtils base = SystemInstance.get().getBase();

        // Override with file from conf dir
        File conf = base.getDirectory("conf");
        if (conf.exists()) {
            File serviceConfig = new File(conf, serviceName + ".properties");
            if (serviceConfig.exists()){
                FileInputStream in = new FileInputStream(serviceConfig);
            try {
                    serviceProperties.load(in);
            } finally {
                        in.close();
                    }
            } else {
                FileOutputStream out = new FileOutputStream(serviceConfig);
                try {
                    String rawPropsContent = (String) serviceProperties.get(Properties.class);
                    out.write(rawPropsContent.getBytes());
                } finally {
                        out.close();
                    }
            }
        }

        // Override with system properties
        String prefix = serviceName + ".";
        Properties sysProps = System.getProperties();
        for (Iterator iterator1 = sysProps.entrySet().iterator(); iterator1.hasNext();) {
            Map.Entry entry1 = (Map.Entry) iterator1.next();
            String key = (String) entry1.getKey();
            String value = (String) entry1.getValue();
            if (key.startsWith(prefix)){
                key = key.replaceFirst(prefix, "");
                serviceProperties.setProperty(key, value);
            }
        }

    }

    private boolean isEnabled(Properties props) throws ServiceException {
        // if it should be started, continue
        String disabled = props.getProperty("disabled", "");

        if (disabled.equalsIgnoreCase("yes") || disabled.equalsIgnoreCase("true")) {
            return false;
        } else {
            return true;
        }
    }

    public synchronized void start() throws ServiceException {
        boolean display = System.getProperty("openejb.nobanner") == null;

        if (display) {
            System.out.println("  ** Starting Services **");
            printRow("NAME", "IP", "PORT");
        }

        for (int i = 0; i < daemons.length; i++) {
            ServerService d = daemons[i];
            try {
                d.start();
                if (display) {
                    printRow(d.getName(), d.getIP(), d.getPort() + "");
                }
            } catch (Exception e) {
                logger.error(d.getName() + " " + d.getIP() + " " + d.getPort() + ": " + e.getMessage());
                if (display) {
                    printRow(d.getName(), "----", "FAILED");
                }
            }
        }
        if (display) {
            System.out.println("-------");
            System.out.println("Ready!");
        }
        /*
         * This will cause the user thread (the thread that keeps the
         *  vm alive) to go into a state of constant waiting.
         *  Each time the thread is woken up, it checks to see if
         *  it should continue waiting.
         *
         *  To stop the thread (and the VM), just call the stop method
         *  which will set 'stop' to true and notify the user thread.
         */
        try {
            while (!stop) {

                this.wait(Long.MAX_VALUE);
            }
        } catch (Throwable t) {
            logger.fatal("Unable to keep the server thread alive. Received exception: " + t.getClass().getName() + " : " + t.getMessage());
        }
        System.out.println("[] exiting vm");
        logger.info("Stopping Remote Server");

    }

    public synchronized void stop() throws ServiceException {
        System.out.println("[] received stop signal");
        stop = true;
        for (int i = 0; i < daemons.length; i++) {
            daemons[i].stop();
        }
        notifyAll();
    }

    private void printRow(String col1, String col2, String col3) {

        col1 += "                    ";
        col1 = col1.substring(0, 20);

        col2 += "                    ";
        col2 = col2.substring(0, 15);

        col3 += "                    ";
        col3 = col3.substring(0, 6);

        StringBuffer sb = new StringBuffer(50);
        sb.append("  ").append(col1);
        sb.append(" ").append(col2);
        sb.append(" ").append(col3);

        System.out.println(sb.toString());
    }
}
