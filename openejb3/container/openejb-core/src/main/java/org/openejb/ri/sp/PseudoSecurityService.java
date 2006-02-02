package org.openejb.ri.sp;

import java.security.Principal;

import org.openejb.spi.SecurityService;
import org.openejb.util.FastThreadLocal;

public class PseudoSecurityService implements SecurityService {

    private FastThreadLocal threadStorage = new FastThreadLocal();

    public void init(java.util.Properties props) {
        props = props;
    }

    public Object getSecurityIdentity() {
        return threadStorage.get();
    }

    public void setSecurityIdentity(Object securityIdentity) {
        threadStorage.set(securityIdentity);
    }

    public boolean isCallerAuthorized(Object securityIdentity, String [] roleNames) {

        return true;
    }

    public Object translateTo(Object securityIdentity, Class type) {
        if (type == java.security.Principal.class) {
            return new java.security.Principal() {
                public String getName() {
                    return "TestRole";
                }
            };
        } else if (type == javax.security.auth.Subject.class) {
            return new javax.security.auth.Subject();
        } else {
            return null;
        }
    }
}