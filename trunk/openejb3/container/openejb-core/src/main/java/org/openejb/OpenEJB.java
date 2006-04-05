package org.openejb;

import org.openejb.loader.SystemInstance;
import org.openejb.spi.ApplicationServer;
import org.openejb.spi.Assembler;
import org.openejb.spi.ContainerSystem;
import org.openejb.spi.SecurityService;
import org.openejb.util.JarUtils;
import org.openejb.util.Logger;
import org.openejb.util.Messages;
import org.openejb.util.SafeToolkit;

import javax.transaction.TransactionManager;
import java.net.URL;
import java.util.Date;
import java.util.Properties;

public final class OpenEJB {

    private static Instance instance;

    public static ApplicationServer getApplicationServer() {
        return ((ApplicationServer) SystemInstance.get().getComponent(ApplicationServer.class));
    }

    public static class Instance {
        private static Messages messages = new Messages("org.openejb.util.resources");

        /**
         * 1 usage
         * org.openejb.core.ivm.naming.InitContextFactory
         */
        public Instance(Properties props) throws OpenEJBException {
            this(props, null);
        }

        /**
         * 2 usages
         */
        public Instance(Properties initProps, ApplicationServer appServer) throws OpenEJBException {
            JarUtils.setHandlerSystemProperty();

            Logger.initialize(initProps);

            Logger logger = Logger.getInstance("OpenEJB.startup", "org.openejb.util.resources");

            try {
                SystemInstance.init(initProps);
            } catch (Exception e) {
                throw new OpenEJBException(e);
            }
            SystemInstance system = SystemInstance.get();

            if (appServer == null) {
                logger.i18n.warning("startup.noApplicationServerSpecified");
            } else {
                system.setComponent(ApplicationServer.class, appServer);
            }

            /*
            * Output startup message
            */
            Properties versionInfo = new Properties();

            try {
                versionInfo.load(new URL("resource:/openejb-version.properties").openConnection().getInputStream());
            } catch (java.io.IOException e) {
            }
            if (initProps.getProperty("openejb.nobanner") == null) {
                System.out.println("OpenEJB " + versionInfo.get("version") + "    build: " + versionInfo.get("date") + "-" + versionInfo.get("time"));
                System.out.println("" + versionInfo.get("url"));
            }

            logger.i18n.info("startup.banner", versionInfo.get("url"), new Date(), versionInfo.get("copyright"),
                    versionInfo.get("version"), versionInfo.get("date"), versionInfo.get("time"));

            logger.info("openejb.home = " + SystemInstance.get().getHome().getDirectory().getAbsolutePath());
            logger.info("openejb.base = " + SystemInstance.get().getBase().getDirectory().getAbsolutePath());

            Properties props = new Properties(System.getProperties());

            if (initProps == null) {
                logger.i18n.debug("startup.noInitializationProperties");
            } else {
                props.putAll(initProps);
            }


            SafeToolkit toolkit = SafeToolkit.getToolkit("OpenEJB");

            /* Uses the EnvProps.ASSEMBLER property to obtain the Assembler impl.
               Default is org.openejb.assembler.classic.Assembler */
            String className = props.getProperty(EnvProps.ASSEMBLER);
            if (className == null) {
                className = props.getProperty("openejb.assembler", "org.openejb.assembler.classic.Assembler");
            } else {
                logger.i18n.warning("startup.deprecatedPropertyName", EnvProps.ASSEMBLER);
            }

            logger.i18n.debug("startup.instantiatingAssemberClass", className);
            Assembler assembler = null;

            try {
                assembler = (Assembler) toolkit.newInstance(className);
            } catch (OpenEJBException oe) {
                logger.i18n.fatal("startup.assemblerCannotBeInstanitated", oe);
                throw oe;
            } catch (Throwable t) {
                String msg = messages.message("startup.openEjbEncounterUnexpectedError");
                logger.i18n.fatal(msg, t);
                throw new OpenEJBException(msg, t);
            }

            try {
                assembler.init(props);
            } catch (OpenEJBException oe) {
                logger.i18n.fatal("startup.assemblerFailedToInitialize", oe);
                throw oe;
            } catch (Throwable t) {
                String msg = messages.message("startup.assemblerEncounterUnexpectedError");
                logger.i18n.fatal(msg, t);
                throw new OpenEJBException(msg, t);
            }

            try {
                assembler.build();
            } catch (OpenEJBException oe) {
                logger.i18n.fatal("startup.assemblerFailedToBuild", oe);
                throw oe;
            } catch (Throwable t) {
                String msg = messages.message("startup.assemblerEncounterUnexpectedBuildError");
                logger.i18n.fatal(msg, t);
                throw new OpenEJBException(msg, t);
            }

            ContainerSystem containerSystem = assembler.getContainerSystem();

            if (containerSystem == null) {
                String msg = messages.message("startup.assemblerReturnedNullContainer");
                logger.i18n.fatal(msg);
                throw new OpenEJBException(msg);
            }

            system.setComponent(ContainerSystem.class, containerSystem);

            if (logger.isDebugEnabled()) {
                logger.i18n.debug("startup.debugContainers", new Integer(containerSystem.containers().length));

                if (containerSystem.containers().length > 0) {
                    Container[] c = containerSystem.containers();
                    logger.i18n.debug("startup.debugContainersType");
                    for (int i = 0; i < c.length; i++) {
                        String entry = "   ";
                        switch (c[i].getContainerType()) {
                            case Container.ENTITY:
                                entry += "ENTITY      ";
                                break;
                            case Container.STATEFUL:
                                entry += "STATEFUL    ";
                                break;
                            case Container.STATELESS:
                                entry += "STATELESS   ";
                                break;
                        }
                        entry += c[i].getContainerID();
                        logger.i18n.debug("startup.debugEntry", entry);
                    }
                }

                logger.i18n.debug("startup.debugDeployments", new Integer(containerSystem.deployments().length));
                if (containerSystem.deployments().length > 0) {
                    logger.i18n.debug("startup.debugDeploymentsType");
                    DeploymentInfo[] d = containerSystem.deployments();
                    for (int i = 0; i < d.length; i++) {
                        String entry = "   ";
                        switch (d[i].getComponentType()) {
                            case DeploymentInfo.BMP_ENTITY:
                                entry += "BMP_ENTITY  ";
                                break;
                            case DeploymentInfo.CMP_ENTITY:
                                entry += "CMP_ENTITY  ";
                                break;
                            case DeploymentInfo.STATEFUL:
                                entry += "STATEFUL    ";
                                break;
                            case DeploymentInfo.STATELESS:
                                entry += "STATELESS   ";
                                break;
                        }
                        entry += d[i].getDeploymentID();
                        logger.i18n.debug("startup.debugEntry", entry);
                    }
                }
            }

            SecurityService securityService = assembler.getSecurityService();
            if (securityService == null) {
                String msg = messages.message("startup.assemblerReturnedNullSecurityService");
                logger.i18n.fatal(msg);
                throw new OpenEJBException(msg);
            } else {
                logger.i18n.debug("startup.securityService", securityService.getClass().getName());
            }
            system.setComponent(SecurityService.class, securityService);

            TransactionManager transactionManager = assembler.getTransactionManager();
            if (transactionManager == null) {
                String msg = messages.message("startup.assemblerReturnedNullTransactionManager");
                logger.i18n.fatal(msg);
                throw new OpenEJBException(msg);
            } else {
                logger.i18n.debug("startup.transactionManager", transactionManager.getClass().getName());
            }

            logger.i18n.info("startup.ready");

            String loader = initProps.getProperty("openejb.loader");
            String nobanner = initProps.getProperty("openejb.nobanner");
            if (nobanner == null && (loader == null || (loader != null && loader.startsWith("tomcat")))) {
                System.out.println(messages.message("startup.ready"));
            }

        }
    }

    public static void destroy() {
        instance = null;
    }

    /**
     * 1 usage
     * org.openejb.core.ivm.naming.InitContextFactory
     */
    public static void init(Properties props) throws OpenEJBException {
        init(props, null);
    }

    private static Messages messages = new Messages("org.openejb.util.resources");
    private static Logger logger = Logger.getInstance("OpenEJB.startup", "org.openejb.util.resources");

    /**
     * 2 usages
     */
    public static void init(Properties initProps, ApplicationServer appServer) throws OpenEJBException {
        if (instance != null) {

            String msg = messages.message("startup.alreadyInitialzied");
            logger.i18n.error(msg);
            throw new OpenEJBException(msg);
        } else {
            instance = new Instance(initProps, appServer);
        }
    }

    /**
     * 1 usages
     */
    public static boolean isInitialized() {
        return instance != null;
    }
}
