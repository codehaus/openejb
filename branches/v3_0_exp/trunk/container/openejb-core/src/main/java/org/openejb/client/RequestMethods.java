package org.openejb.client;

public interface RequestMethods {

    public static final byte EJB_REQUEST = (byte) 0;
    public static final byte JNDI_REQUEST = (byte) 1;
    public static final byte AUTH_REQUEST = (byte) 2;
    public static final byte STOP_REQUEST_Quit = (byte) 'Q';
    public static final byte STOP_REQUEST_quit = (byte) 'q';
    public static final byte STOP_REQUEST_Stop = (byte) 'S';
    public static final byte STOP_REQUEST_stop = (byte) 's';

    public static final int EJB_HOME_GET_EJB_META_DATA = 1;
    public static final int EJB_HOME_GET_HOME_HANDLE = 2;
    public static final int EJB_HOME_REMOVE_BY_HANDLE = 3;
    public static final int EJB_HOME_REMOVE_BY_PKEY = 4;

    public static final int EJB_HOME_FIND = 9;
    public static final int EJB_HOME_CREATE = 10;

    public static final int EJB_OBJECT_GET_EJB_HOME = 14;
    public static final int EJB_OBJECT_GET_HANDLE = 15;
    public static final int EJB_OBJECT_GET_PRIMARY_KEY = 16;
    public static final int EJB_OBJECT_IS_IDENTICAL = 17;
    public static final int EJB_OBJECT_REMOVE = 18;

    public static final int EJB_OBJECT_BUSINESS_METHOD = 23;

    public static final int JNDI_LOOKUP = 27;
    public static final int JNDI_LIST = 28;
    public static final int JNDI_LIST_BINDINGS = 29;

}

