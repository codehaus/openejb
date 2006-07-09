//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.0.1-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2006.07.08 at 06:01:06 PM PDT 
//


package org.openejb.jee2;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 * 
 * 	The enterprise-beansType declares one or more enterprise
 * 	beans. Each bean can be a session, entity or message-driven
 * 	bean.
 * 
 *       
 * 
 * <p>Java class for enterprise-beansType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="enterprise-beansType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded">
 *         &lt;element name="session" type="{http://java.sun.com/xml/ns/javaee}session-beanType"/>
 *         &lt;element name="entity" type="{http://java.sun.com/xml/ns/javaee}entity-beanType"/>
 *         &lt;element name="message-driven" type="{http://java.sun.com/xml/ns/javaee}message-driven-beanType"/>
 *       &lt;/choice>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "enterprise-beansType", propOrder = {
    "sessionOrEntityOrMessageDriven"
})
public class EnterpriseBeansType {

    @XmlElements({
        @XmlElement(name = "message-driven", required = true, type = MessageDrivenBeanType.class),
        @XmlElement(name = "session", required = true, type = SessionBeanType.class),
        @XmlElement(name = "entity", required = true, type = EntityBeanType.class)
    })
    protected List<Object> sessionOrEntityOrMessageDriven;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    /**
     * Gets the value of the sessionOrEntityOrMessageDriven property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the sessionOrEntityOrMessageDriven property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSessionOrEntityOrMessageDriven().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link MessageDrivenBeanType }
     * {@link SessionBeanType }
     * {@link EntityBeanType }
     * 
     * 
     */
    public List<Object> getSessionOrEntityOrMessageDriven() {
        if (sessionOrEntityOrMessageDriven == null) {
            sessionOrEntityOrMessageDriven = new ArrayList<Object>();
        }
        return this.sessionOrEntityOrMessageDriven;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

}
