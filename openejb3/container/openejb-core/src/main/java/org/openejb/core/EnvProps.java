package org.openejb.core;

public class EnvProps {

    public final static String IM_CLASS_NAME = "InstanceManager";

    public final static String IM_TIME_OUT = "TimeOut";

    public final static String IM_PASSIVATOR_PATH_PREFIX = "org/openejb/core/InstanceManager/PASSIVATOR_PATH_PREFIX";

    public final static String IM_POOL_SIZE = "PoolSize";

    public final static String IM_PASSIVATE_SIZE = "BulkPassivate";

    public final static String IM_PASSIVATOR = "Passivator";

    public final static String IM_CONCURRENT_ATTEMPTS = "org/openejb/core/InstanceManager/CONCURRENT_ATTEMPTS";

    public final static String IM_STRICT_POOLING = "StrictPooling";

    public final static String THREAD_CONTEXT_IMPL = "org/openejb/core/ThreadContext/IMPL_CLASS";

    /*
    * The EJB 1.1 specification requires that arguments and return values between beans adhere to the
    * Java RMI copy semantics which requires that the all arguments be passed by value (copied) and 
    * never passed as references.  However, it is possible for the system administrator to turn off the
    * copy operation so that arguments and return values are passed by reference as a performance optimization.
    * Simply setting the org.openejb.core.EnvProps.INTRA_VM_COPY property to FALSE will cause  
    * IntraVM to bypass the copy operations; arguments and return values will be passed by reference not value. 
    * This property is, by default, alwasy TRUE but it can be changed to FALSE by setting it as a System property
    * or a property of the Property argument when invoking OpenEJB.init(props).
    */
    public final static String INTRA_VM_COPY = "org/openejb/core/ivm/BaseEjbProxyHandler/INTRA_VM_COPY";


    public static final String JDBC_DRIVER = "JdbcDriver";


    public static final String JDBC_URL = "JdbcUrl";


    public static final String USER_NAME = "UserName";


    public static final String PASSWORD = "Password";


    public static final String GLOBAL_TX_DATABASE = "Global_TX_Database";


    public static final String LOCAL_TX_DATABASE = "Local_TX_Database";
}