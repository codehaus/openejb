//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.0.1-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2006.07.08 at 06:01:06 PM PDT 
//


package org.openejb.jee2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 
 * 	An injection target specifies a class and a name within
 * 	that class into which a resource should be injected.
 * 
 * 	The injection target class specifies the fully qualified
 * 	class name that is the target of the injection.  The
 * 	Java EE specifications describe which classes can be an
 * 	injection target.
 * 
 * 	The injection target name specifies the target within
 * 	the specified class.  The target is first looked for as a
 * 	JavaBeans property name.  If not found, the target is
 * 	looked for as a field name.
 * 
 * 	The specified resource will be injected into the target
 * 	during initialization of the class by either calling the
 * 	set method for the target property or by setting a value
 * 	into the named field.
 * 
 *       
 * 
 * <p>Java class for injection-targetType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="injection-targetType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="injection-target-class" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType"/>
 *         &lt;element name="injection-target-name" type="{http://java.sun.com/xml/ns/javaee}java-identifierType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "injection-targetType", propOrder = {
    "injectionTargetClass",
    "injectionTargetName"
})
public class InjectionTargetType {

    @XmlElement(name = "injection-target-class", required = true)
    protected String injectionTargetClass;
    @XmlElement(name = "injection-target-name", required = true)
    protected String injectionTargetName;

    public String getInjectionTargetClass() {
        return injectionTargetClass;
    }

    public void setInjectionTargetClass(String value) {
        this.injectionTargetClass = value;
    }

    public String getInjectionTargetName() {
        return injectionTargetName;
    }

    public void setInjectionTargetName(String value) {
        this.injectionTargetName = value;
    }

}
