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
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _EjbJar_QNAME = new QName("http://java.sun.com/xml/ns/javaee", "ejb-jar");
    private final static QName _EjbRelationTypeEjbRelationName_QNAME = new QName("http://java.sun.com/xml/ns/javaee", "ejb-relation-name");
    private final static QName _EjbRelationTypeEjbRelationshipRole_QNAME = new QName("http://java.sun.com/xml/ns/javaee", "ejb-relationship-role");
    private final static QName _EjbRelationTypeDescription_QNAME = new QName("http://java.sun.com/xml/ns/javaee", "description");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.openejb.jee2
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EjbJarType }{@code >}}
     */
    @XmlElementDecl(namespace = "http://java.sun.com/xml/ns/javaee", name = "ejb-jar")
    public JAXBElement<EjbJarType> createEjbJar(EjbJarType value) {
        return new JAXBElement<EjbJarType>(_EjbJar_QNAME, EjbJarType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     */
    @XmlElementDecl(namespace = "http://java.sun.com/xml/ns/javaee", name = "ejb-relation-name", scope = EjbRelationType.class)
    public JAXBElement<String> createEjbRelationTypeEjbRelationName(String value) {
        return new JAXBElement<String>(_EjbRelationTypeEjbRelationName_QNAME, String.class, EjbRelationType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EjbRelationshipRoleType }{@code >}}
     */
    @XmlElementDecl(namespace = "http://java.sun.com/xml/ns/javaee", name = "ejb-relationship-role", scope = EjbRelationType.class)
    public JAXBElement<EjbRelationshipRoleType> createEjbRelationTypeEjbRelationshipRole(EjbRelationshipRoleType value) {
        return new JAXBElement<EjbRelationshipRoleType>(_EjbRelationTypeEjbRelationshipRole_QNAME, EjbRelationshipRoleType.class, EjbRelationType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Text }{@code >}}
     */
    @XmlElementDecl(namespace = "http://java.sun.com/xml/ns/javaee", name = "description", scope = EjbRelationType.class)
    public JAXBElement<Text> createEjbRelationTypeDescription(Text value) {
        return new JAXBElement<Text>(_EjbRelationTypeDescription_QNAME, Text.class, EjbRelationType.class, value);
    }

}
