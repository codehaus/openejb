package org.openejb.client;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.rmi.NoSuchObjectException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

import org.openejb.client.proxy.InvocationHandler;

public abstract class EJBInvocationHandler implements InvocationHandler, Serializable, ResponseCodes, RequestMethods {

    protected static final Method EQUALS = getMethod(Object.class, "equals", null);
    protected static final Method HASHCODE = getMethod(Object.class, "hashCode", null);
    protected static final Method TOSTRING = getMethod(Object.class, "toString", null);

    protected static final Hashtable liveHandleRegistry = new Hashtable();

    protected transient boolean inProxyMap = false;

    protected transient boolean isInvalidReference = false;

    protected transient EJBRequest request;

    protected transient EJBMetaDataImpl ejb;
    protected transient ServerMetaData server;
    protected transient ClientMetaData client;

    protected transient Object primaryKey;

    public EJBInvocationHandler() {
    }

    public EJBInvocationHandler(EJBMetaDataImpl ejb, ServerMetaData server, ClientMetaData client) {
        this.ejb = ejb;
        this.server = server;
        this.client = client;
    }

    public EJBInvocationHandler(EJBMetaDataImpl ejb, ServerMetaData server, ClientMetaData client, Object primaryKey) {
        this(ejb, server, client);
        this.primaryKey = primaryKey;
    }

    protected static Method getMethod(Class c, String method, Class[] params) {
        try {
            return c.getMethod(method, params);
        } catch (NoSuchMethodException nse) {

        }
        return null;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (isInvalidReference) throw new NoSuchObjectException("reference is invalid");

        Object returnObj = null;
        returnObj = _invoke(proxy, method, args);
        return returnObj;
    }

    protected abstract Object _invoke(Object proxy, Method method, Object[] args) throws Throwable;

    public static void print(String s) {

    }

    public static void println(String s) {

    }

    protected EJBResponse request(EJBRequest req) throws Exception {
        return (EJBResponse) Client.request(req, new EJBResponse(), server);
    }

    protected void invalidateReference() {
        this.server = null;
        this.client = null;
        this.ejb = null;
        this.inProxyMap = false;
        this.isInvalidReference = true;
        this.primaryKey = null;
    }

    protected static void invalidateAllHandlers(Object key) {

        HashSet set = (HashSet) liveHandleRegistry.remove(key);
        if (set == null) return;

        synchronized (set) {
            Iterator handlers = set.iterator();
            while (handlers.hasNext()) {
                EJBInvocationHandler handler = (EJBInvocationHandler) handlers.next();
                handler.invalidateReference();
            }
            set.clear();
        }
    }

    protected static void registerHandler(Object key, EJBInvocationHandler handler) {
        HashSet set = (HashSet) liveHandleRegistry.get(key);

        if (set == null) {
            set = new HashSet();
            liveHandleRegistry.put(key, set);
        }

        synchronized (set) {
            set.add(handler);
        }
    }
}