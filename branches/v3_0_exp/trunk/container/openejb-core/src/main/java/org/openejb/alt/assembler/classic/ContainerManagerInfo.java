package org.openejb.alt.assembler.classic;

public class ContainerManagerInfo extends InfoObject{

    public String name;

    public ContainerInfo[] containers;

    public EntityContainerInfo[] entityContainers;
    public StatelessSessionContainerInfo[] statelessContainers;
    public StatefulSessionContainerInfo[] statefulContainers;
}
