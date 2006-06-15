package org.openejb.core.ivm.naming;

import javax.naming.NamingException;

import org.openejb.loader.SystemInstance;
import org.openejb.spi.ContainerSystem;

public class IntraVmJndiReference implements Reference {

    private String jndiName;

    public IntraVmJndiReference(String jndiName) {
        this.jndiName = jndiName;
    }

    public Object getObject() throws NamingException {
        ContainerSystem containerSystem = (ContainerSystem) SystemInstance.get().getComponent(ContainerSystem.class);
        return containerSystem.getJNDIContext().lookup(jndiName);
    }
}
