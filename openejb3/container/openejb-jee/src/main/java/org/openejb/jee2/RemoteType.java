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


/**
 * 
 * 	
 * 
 * 	  The remote element contains the fully-qualified name
 * 	  of the enterprise bean's remote interface.
 * 
 * 	  Example:
 * 
 * 	      <remote>com.wombat.empl.EmployeeService</remote>
 * 
 * 	  
 *       
 * 
 * <p>Java class for remoteType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="remoteType">
 *   &lt;simpleContent>
 *     &lt;restriction base="&lt;http://java.sun.com/xml/ns/javaee>fully-qualified-classType">
 *     &lt;/restriction>
 *   &lt;/simpleContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "remoteType")
public class RemoteType
    extends FullyQualifiedClassType
{


}
