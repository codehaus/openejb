package org.openejb.alt.assembler.classic;

public class StatefulSessionContainerInfo extends ContainerInfo {

    public StatefulBeanInfo[] beans;

    public StatefulSessionContainerInfo() {
        containerType = STATEFUL_SESSION_CONTAINER;
    }

}
