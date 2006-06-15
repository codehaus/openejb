package org.openejb.util;

import java.util.HashMap;

/*
* This variation of ThreadLocal accomplishes thread-specific storage by thread as well
* as by object.  Values are associated with both an key and a thread, which allows 
* each value to stored specific to an object and thread. 
*
* @see org.openejb.resource.SharedLocalConnectionManager
* @author <a href="richard@monson-haefel.com">Richard Monson-Haefel</a>
* @version $Rev$ $Id: HashThreadLocal.java,v 1.2 2005/06/19 22:40:33 jlaskowski Exp $
*/

public class HashThreadLocal {
    HashMap keyMap = new HashMap();

    public synchronized void put(Object key, Object value) {
        FastThreadLocal threadLocal = (FastThreadLocal) keyMap.get(key);
        if (threadLocal == null) {
            threadLocal = new FastThreadLocal();
            keyMap.put(key, threadLocal);
        }
        threadLocal.set(value);
    }

    public synchronized Object get(Object key) {
        FastThreadLocal threadLocal = (FastThreadLocal) keyMap.get(key);
        if (threadLocal == null) return null;
        return threadLocal.get();
    }
}