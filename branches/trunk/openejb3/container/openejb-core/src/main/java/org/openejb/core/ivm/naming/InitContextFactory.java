package org.openejb.core.ivm.naming;

import java.util.Hashtable;
import java.util.Properties;

import javax.naming.Context;

import org.openejb.EnvProps;
import org.openejb.loader.SystemInstance;
import org.openejb.spi.ContainerSystem;

public class InitContextFactory implements javax.naming.spi.InitialContextFactory {

    public Context getInitialContext(Hashtable env) throws javax.naming.NamingException {
        if (!org.openejb.OpenEJB.isInitialized()) {
            initializeOpenEJB(env);
        }

        ContainerSystem containerSystem = (ContainerSystem) SystemInstance.get().getComponent(ContainerSystem.class);
        Context context = containerSystem.getJNDIContext();
        context = (Context) context.lookup("java:openejb/ejb");
        return context;

    }

    private void initializeOpenEJB(Hashtable env) throws javax.naming.NamingException {
        try {
            Properties props = new Properties();

            /* DMB: We should get the defaults from the functionality
            *      Alan is working on.  This is temporary.
            *      When that logic is finished, this block should
            *      probably just be deleted.
            */
            props.put(EnvProps.ASSEMBLER, "org.openejb.assembler.classic.Assembler");
            props.put(EnvProps.CONFIGURATION_FACTORY, "org.openejb.alt.config.ConfigurationFactory");
            props.put(EnvProps.CONFIGURATION, "conf/default.openejb.conf");

            props.putAll(System.getProperties());

            props.putAll(env);

            org.openejb.OpenEJB.init(props);

        }
        catch (org.openejb.OpenEJBException e) {
            throw new NamingException("Cannot initailize OpenEJB", e);
        }
        catch (Exception e) {
            throw new NamingException("Cannot initailize OpenEJB", e);
        }
    }

}

