//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.0.1-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2006.07.08 at 06:01:06 PM PDT 
//


package org.openejb.jee2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for init-methodType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="init-methodType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="create-method" type="{http://java.sun.com/xml/ns/javaee}named-methodType"/>
 *         &lt;element name="bean-method" type="{http://java.sun.com/xml/ns/javaee}named-methodType"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "init-methodType", propOrder = {
    "createMethod",
    "beanMethod"
})
public class InitMethodType {

    @XmlElement(name = "create-method", required = true)
    protected NamedMethodType createMethod;
    @XmlElement(name = "bean-method", required = true)
    protected NamedMethodType beanMethod;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    public NamedMethodType getCreateMethod() {
        return createMethod;
    }

    public void setCreateMethod(NamedMethodType value) {
        this.createMethod = value;
    }

    public NamedMethodType getBeanMethod() {
        return beanMethod;
    }

    public void setBeanMethod(NamedMethodType value) {
        this.beanMethod = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

}
