package org.openejb.spi;

import org.openejb.ProxyInfo;
import javax.ejb.EJBMetaData;
import javax.ejb.Handle;
//import javax.ejb.HomeHandle;
import javax.ejb.EJBObject;
import javax.ejb.EJBHome;

public interface ApplicationServer {
    
    public EJBMetaData getEJBMetaData(ProxyInfo proxyInfo);
    
    public Handle getHandle(ProxyInfo proxyInfo);
       
    public EJBObject getEJBObject(ProxyInfo proxyInfo);
    
    public EJBHome getEJBHome(ProxyInfo proxyInfo);
    
}