//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.0.1-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2006.07.08 at 06:01:06 PM PDT 
//


package org.openejb.jee2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlEnumValue;


/**
 * 	The cmr-field-type element specifies the class of a
 * 	collection-valued logical relationship field in the entity
 * 	bean class. The value of an element using cmr-field-typeType
 * 	must be either: java.util.Collection or java.util.Set.
 */
public enum CmrFieldTypeType {
    @XmlEnumValue("java.util.Collection") COLLECTION,
    @XmlEnumValue("java.util.Set") SET
}