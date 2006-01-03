package org.openejb.alt.config;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.openejb.OpenEJBException;
import org.openejb.alt.config.sys.ServiceProvider;
import org.openejb.alt.config.sys.ServicesJar;
import org.openejb.util.Logger;
import org.openejb.util.Messages;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ServiceUtils {

    public static final String defaultProviderURL = "org.openejb";
    private static Map loadedServiceJars = new HashMap();
    public static Messages messages = new Messages("org.openejb.util.resources");
    public static Logger logger = Logger.getInstance("OpenEJB", "org.openejb.util.resources");

    public static ServiceProvider getServiceProvider(Service service) throws OpenEJBException {
        return getServiceProvider(service.getProvider());
    }

    public static ServiceProvider getServiceProvider(String id) throws OpenEJBException {

        String providerName = null;
        String serviceName = null;

        if (id.indexOf("#") == -1) {
            providerName = defaultProviderURL;
            serviceName = id;
        } else {
            providerName = id.substring(0, id.indexOf("#"));
            serviceName = id.substring(id.indexOf("#") + 1);
        }

        ServiceProvider service = null;

        if (loadedServiceJars.get(providerName) == null) {
            ServicesJar sj = readServicesJar(providerName);
            ServiceProvider[] sp = sj.getServiceProvider();
            HashMap services = new HashMap(sj.getServiceProviderCount());

            for (int i = 0; i < sp.length; i++) {
                services.put(sp[i].getId(), sp[i]);
            }

            loadedServiceJars.put(providerName, services);

            service = (ServiceProvider) services.get(serviceName);
        } else {
            Map provider = (Map) loadedServiceJars.get(providerName);
            service = (ServiceProvider) provider.get(serviceName);
        }

        if (service == null) {
            throw new OpenEJBException(messages.format("conf.4901", serviceName, providerName));
        }

        return service;
    }

    public static ServicesJar readServicesJar(String providerName) throws OpenEJBException {
        try {
            Unmarshaller unmarshaller = new Unmarshaller(ServicesJar.class, "service-jar.xml");
            URL serviceURL = new URL("resource:/META-INF/" + providerName + "/");
            return (ServicesJar) unmarshaller.unmarshal(serviceURL);
        } catch (MalformedURLException e) {
            throw new OpenEJBException(e);
        }
    }

    public static void writeServicesJar(String xmlFile, ServicesJar servicesJarObject) throws OpenEJBException {

        /* TODO:  Just to be picky, the xml file created by
        Castor is really hard to read -- it is all on one line.
        People might want to edit this in the future by hand, so if Castor can
        make the output look better that would be great!  Otherwise we could
        just spruce the output up by adding a few new lines and tabs.
        */
        Writer writer = null;

        try {
            File file = new File(xmlFile);
            writer = new FileWriter(file);
            servicesJarObject.marshal(writer);
        } catch (IOException e) {
            throw new OpenEJBException(messages.format("conf.4040", xmlFile, e.getLocalizedMessage()));
        } catch (MarshalException e) {
            if (e.getException() instanceof IOException) {
                throw new OpenEJBException(messages.format("conf.4040", xmlFile, e.getLocalizedMessage()));
            } else {
                throw new OpenEJBException(messages.format("conf.4050", xmlFile, e.getLocalizedMessage()));
            }
        } catch (ValidationException e) {

            /* TODO: Implement informative error handling here.
               The exception will say "X doesn't match the regular
               expression Y"
               This should be checked and more relevant information
               should be given -- not everyone understands regular
               expressions.
             */

            /* NOTE: This doesn't seem to ever happen. When the object graph
             * is invalid, the MarshalException is thrown, not this one as you
             * would think.
             */

            throw new OpenEJBException(messages.format("conf.4060", xmlFile, e.getLocalizedMessage()));
        }

        try {
            writer.close();
        } catch (Exception e) {
            throw new OpenEJBException(messages.format("file.0020", xmlFile, e.getLocalizedMessage()));
        }
    }

    public static Properties assemblePropertiesFor(String confItem, String itemId, String itemContent,
                                                   String confFile, ServiceProvider service) throws OpenEJBException {

        Properties props = new Properties();

        try {
            /*
             * 1. Load properties from the properties file referenced
             *    by the service provider
             */
            if (service.getPropertiesFile() != null) {
                props = loadProperties(service.getPropertiesFile().getFile());
            }

            /*
             * 2. Load properties from the content in the service provider
             *    element of the service-jar.xml
             */

            if (service.getContent() != null) {
                StringBufferInputStream in = new StringBufferInputStream(service.getContent());
                props = loadProperties(in, props);
            }
        } catch (OpenEJBException ex) {
            throw new OpenEJBException(messages.format("conf.0013", service.getId(), null, ex.getLocalizedMessage()));
        }

        /* 3. Load properties from the content in the Container
         *    element of the configuration file.
         */
        try {
            if (itemContent != null) {
                StringBufferInputStream in = new StringBufferInputStream(itemContent);
                props = loadProperties(in, props);
            }
        } catch (OpenEJBException ex) {
            throw new OpenEJBException(messages.format("conf.0014", confItem, itemId, confFile, ex.getLocalizedMessage()));
        }

        return props;
    }

    public static Properties loadProperties(String pFile) throws OpenEJBException {
        return loadProperties(pFile, new Properties());
    }

    public static Properties loadProperties(String propertiesFile, Properties defaults) throws OpenEJBException {
        try {
            File pfile = new File(propertiesFile);
            InputStream in = new FileInputStream(pfile);
            return loadProperties(in, defaults);
        } catch (FileNotFoundException ex) {
            throw new OpenEJBException(messages.format("conf.0006", propertiesFile, ex.getLocalizedMessage()));
        } catch (IOException ex) {
            throw new OpenEJBException(messages.format("conf.0007", propertiesFile, ex.getLocalizedMessage()));
        } catch (SecurityException ex) {
            throw new OpenEJBException(messages.format("conf.0005", propertiesFile, ex.getLocalizedMessage()));
        }
    }

    public static Properties loadProperties(InputStream in, Properties defaults)
            throws OpenEJBException {

        try {
            /*
            This may not work as expected.  The desired effect is that
            the load method will read in the properties and overwrite
            the values of any properties that may have previously been
            defined.
            */
            defaults.load(in);
        } catch (IOException ex) {
            throw new OpenEJBException(messages.format("conf.0012", ex.getLocalizedMessage()));
        }

        return defaults;
    }

}
