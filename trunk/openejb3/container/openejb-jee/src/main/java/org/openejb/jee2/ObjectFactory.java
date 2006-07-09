//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.0.1-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2006.07.08 at 06:01:06 PM PDT 
//


package org.openejb.jee2;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.openejb.jee2 package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _EjbJar_QNAME = new QName("http://java.sun.com/xml/ns/javaee", "ejb-jar");
    private final static QName _EjbRelationTypeEjbRelationName_QNAME = new QName("http://java.sun.com/xml/ns/javaee", "ejb-relation-name");
    private final static QName _EjbRelationTypeEjbRelationshipRole_QNAME = new QName("http://java.sun.com/xml/ns/javaee", "ejb-relationship-role");
    private final static QName _EjbRelationTypeDescription_QNAME = new QName("http://java.sun.com/xml/ns/javaee", "description");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.openejb.jee2
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EjbJarType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://java.sun.com/xml/ns/javaee", name = "ejb-jar")
    public JAXBElement<EjbJarType> createEjbJar(EjbJarType value) {
        return new JAXBElement<EjbJarType>(_EjbJar_QNAME, EjbJarType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link JeeString }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://java.sun.com/xml/ns/javaee", name = "ejb-relation-name", scope = EjbRelationType.class)
    public JAXBElement<java.lang.String> createEjbRelationTypeEjbRelationName(java.lang.String value) {
        return new JAXBElement<java.lang.String>(_EjbRelationTypeEjbRelationName_QNAME, java.lang.String.class, EjbRelationType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EjbRelationshipRoleType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://java.sun.com/xml/ns/javaee", name = "ejb-relationship-role", scope = EjbRelationType.class)
    public JAXBElement<EjbRelationshipRoleType> createEjbRelationTypeEjbRelationshipRole(EjbRelationshipRoleType value) {
        return new JAXBElement<EjbRelationshipRoleType>(_EjbRelationTypeEjbRelationshipRole_QNAME, EjbRelationshipRoleType.class, EjbRelationType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DescriptionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://java.sun.com/xml/ns/javaee", name = "description", scope = EjbRelationType.class)
    public JAXBElement<DescriptionType> createEjbRelationTypeDescription(DescriptionType value) {
        return new JAXBElement<DescriptionType>(_EjbRelationTypeDescription_QNAME, DescriptionType.class, EjbRelationType.class, value);
    }

}