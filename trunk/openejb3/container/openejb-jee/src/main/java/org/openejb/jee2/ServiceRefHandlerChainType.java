/**
 *
 * Copyright 2006 The Apache Software Foundation or its licensors, as applicable.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.openejb.jee2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;


/**
 * The handler-chain element defines the handlerchain.
 * Handlerchain can be defined such that the handlers in the
 * handlerchain operate,all ports of a service, on a specific
 * port or on a list of protocol-bindings. The choice of elements
 * service-name-pattern, port-name-pattern and protocol-bindings
 * are used to specify whether the handlers in handler-chain are
 * for a service, port or protocol binding. If none of these
 * choices are specified with the handler-chain element then the
 * handlers specified in the handler-chain will be applied on
 * everything.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "service-ref_handler-chainType", propOrder = {
        "serviceNamePattern",
        "portNamePattern",
        "protocolBindings",
        "handler"
        })
public class ServiceRefHandlerChainType {

    @XmlElement(name = "service-name-pattern")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String serviceNamePattern;
    @XmlElement(name = "port-name-pattern")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String portNamePattern;
    @XmlList
    @XmlElement(name = "protocol-bindings")
    protected List<String> protocolBindings;
    @XmlElement(required = true)
    protected List<ServiceRefHandlerType> handler;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    public String getServiceNamePattern() {
        return serviceNamePattern;
    }

    public void setServiceNamePattern(String value) {
        this.serviceNamePattern = value;
    }

    public String getPortNamePattern() {
        return portNamePattern;
    }

    public void setPortNamePattern(String value) {
        this.portNamePattern = value;
    }

    /**
     * Gets the value of the protocolBindings property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the protocolBindings property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * getProtocolBindings().add(newItem);
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     */
    public List<String> getProtocolBindings() {
        if (protocolBindings == null) {
            protocolBindings = new ArrayList<String>();
        }
        return this.protocolBindings;
    }

    /**
     * Gets the value of the handler property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the handler property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * getHandler().add(newItem);
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link ServiceRefHandlerType }
     */
    public List<ServiceRefHandlerType> getHandler() {
        if (handler == null) {
            handler = new ArrayList<ServiceRefHandlerType>();
        }
        return this.handler;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

}
