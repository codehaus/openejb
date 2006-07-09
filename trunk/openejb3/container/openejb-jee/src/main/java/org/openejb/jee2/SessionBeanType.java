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
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 * 
 * 	The session-beanType declares an session bean. The
 * 	declaration consists of:
 * 
 * 	    - an optional description
 * 	    - an optional display name
 * 	    - an optional icon element that contains a small and a large
 * 	      icon file name
 * 	    - a name assigned to the enterprise bean
 * 	      in the deployment description
 *             - an optional mapped-name element that can be used to provide
 *               vendor-specific deployment information such as the physical
 *               jndi-name of the session bean's remote home/business interface.
 *               This element is not required to be supported by all
 *               implementations. Any use of this element is non-portable.
 *             - the names of all the remote or local business interfaces,
 *               if any
 * 	    - the names of the session bean's remote home and
 * 	      remote interfaces, if any
 * 	    - the names of the session bean's local home and
 * 	      local interfaces, if any
 * 	    - the name of the session bean's web service endpoint
 * 	      interface, if any
 * 	    - the session bean's implementation class
 * 	    - the session bean's state management type
 *             - an optional declaration of the session bean's timeout method.
 * 	    - the optional session bean's transaction management type.
 *               If it is not present, it is defaulted to Container.
 *             - an optional list of the session bean class and/or
 *               superclass around-invoke methods.
 * 	    - an optional declaration of the bean's
 * 	      environment entries
 * 	    - an optional declaration of the bean's EJB references
 * 	    - an optional declaration of the bean's local
 * 	      EJB references
 * 	    - an optional declaration of the bean's web
 * 	      service references
 * 	    - an optional declaration of the security role
 * 	      references
 * 	    - an optional declaration of the security identity
 * 	      to be used for the execution of the bean's methods
 * 	    - an optional declaration of the bean's resource
 * 	      manager connection factory references
 * 	    - an optional declaration of the bean's resource
 * 	      environment references.
 * 	    - an optional declaration of the bean's message
 * 	      destination references
 * 
 * 	The elements that are optional are "optional" in the sense
 * 	that they are omitted when if lists represented by them are
 * 	empty.
 * 
 * 	Either both the local-home and the local elements or both
 * 	the home and the remote elements must be specified for the
 * 	session bean.
 * 
 * 	The service-endpoint element may only be specified if the
 * 	bean is a stateless session bean.
 * 
 *       
 * 
 * <p>Java class for session-beanType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="session-beanType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;group ref="{http://java.sun.com/xml/ns/javaee}descriptionGroup"/>
 *         &lt;element name="ejb-name" type="{http://java.sun.com/xml/ns/javaee}ejb-nameType"/>
 *         &lt;element name="mapped-name" type="{http://java.sun.com/xml/ns/javaee}xsdStringType" minOccurs="0"/>
 *         &lt;element name="home" type="{http://java.sun.com/xml/ns/javaee}homeType" minOccurs="0"/>
 *         &lt;element name="remote" type="{http://java.sun.com/xml/ns/javaee}remoteType" minOccurs="0"/>
 *         &lt;element name="local-home" type="{http://java.sun.com/xml/ns/javaee}local-homeType" minOccurs="0"/>
 *         &lt;element name="local" type="{http://java.sun.com/xml/ns/javaee}localType" minOccurs="0"/>
 *         &lt;element name="business-local" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="business-remote" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="service-endpoint" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType" minOccurs="0"/>
 *         &lt;element name="ejb-class" type="{http://java.sun.com/xml/ns/javaee}ejb-classType" minOccurs="0"/>
 *         &lt;element name="session-type" type="{http://java.sun.com/xml/ns/javaee}session-typeType" minOccurs="0"/>
 *         &lt;element name="timeout-method" type="{http://java.sun.com/xml/ns/javaee}named-methodType" minOccurs="0"/>
 *         &lt;element name="init-method" type="{http://java.sun.com/xml/ns/javaee}init-methodType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="remove-method" type="{http://java.sun.com/xml/ns/javaee}remove-methodType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="transaction-type" type="{http://java.sun.com/xml/ns/javaee}transaction-typeType" minOccurs="0"/>
 *         &lt;element name="around-invoke" type="{http://java.sun.com/xml/ns/javaee}around-invokeType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;group ref="{http://java.sun.com/xml/ns/javaee}jndiEnvironmentRefsGroup"/>
 *         &lt;element name="post-activate" type="{http://java.sun.com/xml/ns/javaee}lifecycle-callbackType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="pre-passivate" type="{http://java.sun.com/xml/ns/javaee}lifecycle-callbackType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="security-role-ref" type="{http://java.sun.com/xml/ns/javaee}security-role-refType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="security-identity" type="{http://java.sun.com/xml/ns/javaee}security-identityType" minOccurs="0"/>
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
@XmlType(name = "session-beanType", propOrder = {
    "description",
    "displayName",
    "icon",
    "ejbName",
    "mappedName",
    "home",
    "remote",
    "localHome",
    "local",
    "businessLocal",
    "businessRemote",
    "serviceEndpoint",
    "ejbClass",
    "sessionType",
    "timeoutMethod",
    "initMethod",
    "removeMethod",
    "transactionType",
    "aroundInvoke",
    "envEntry",
    "ejbRef",
    "ejbLocalRef",
    "serviceRef",
    "resourceRef",
    "resourceEnvRef",
    "messageDestinationRef",
    "persistenceContextRef",
    "persistenceUnitRef",
    "postConstruct",
    "preDestroy",
    "postActivate",
    "prePassivate",
    "securityRoleRef",
    "securityIdentity"
})
public class SessionBeanType {

    @XmlElement(required = true)
    protected List<Text> description;
    @XmlElement(name = "display-name", required = true)
    protected List<Text> displayName;
    @XmlElement(required = true)
    protected List<IconType> icon;
    @XmlElement(name = "ejb-name", required = true)
    protected String ejbName;
    @XmlElement(name = "mapped-name")
    protected String mappedName;
    protected String home;
    protected String remote;
    @XmlElement(name = "local-home")
    protected String localHome;
    protected String local;
    @XmlElement(name = "business-local", required = true)
    protected List<String> businessLocal;
    @XmlElement(name = "business-remote", required = true)
    protected List<String> businessRemote;
    @XmlElement(name = "service-endpoint")
    protected String serviceEndpoint;
    @XmlElement(name = "ejb-class")
    protected String ejbClass;
    @XmlElement(name = "session-type")
    protected SessionTypeType sessionType;
    @XmlElement(name = "timeout-method")
    protected NamedMethodType timeoutMethod;
    @XmlElement(name = "init-method", required = true)
    protected List<InitMethodType> initMethod;
    @XmlElement(name = "remove-method", required = true)
    protected List<RemoveMethodType> removeMethod;
    @XmlElement(name = "transaction-type")
    protected TransactionTypeType transactionType;
    @XmlElement(name = "around-invoke", required = true)
    protected List<AroundInvokeType> aroundInvoke;
    @XmlElement(name = "env-entry", required = true)
    protected List<EnvEntryType> envEntry;
    @XmlElement(name = "ejb-ref", required = true)
    protected List<EjbRefType> ejbRef;
    @XmlElement(name = "ejb-local-ref", required = true)
    protected List<EjbLocalRefType> ejbLocalRef;
    @XmlElement(name = "service-ref", required = true)
    protected List<ServiceRefType> serviceRef;
    @XmlElement(name = "resource-ref", required = true)
    protected List<ResourceRefType> resourceRef;
    @XmlElement(name = "resource-env-ref", required = true)
    protected List<ResourceEnvRefType> resourceEnvRef;
    @XmlElement(name = "message-destination-ref", required = true)
    protected List<MessageDestinationRefType> messageDestinationRef;
    @XmlElement(name = "persistence-context-ref", required = true)
    protected List<PersistenceContextRefType> persistenceContextRef;
    @XmlElement(name = "persistence-unit-ref", required = true)
    protected List<PersistenceUnitRefType> persistenceUnitRef;
    @XmlElement(name = "post-construct", required = true)
    protected List<LifecycleCallbackType> postConstruct;
    @XmlElement(name = "pre-destroy", required = true)
    protected List<LifecycleCallbackType> preDestroy;
    @XmlElement(name = "post-activate", required = true)
    protected List<LifecycleCallbackType> postActivate;
    @XmlElement(name = "pre-passivate", required = true)
    protected List<LifecycleCallbackType> prePassivate;
    @XmlElement(name = "security-role-ref", required = true)
    protected List<SecurityRoleRefType> securityRoleRef;
    @XmlElement(name = "security-identity")
    protected SecurityIdentityType securityIdentity;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    /**
     * Gets the value of the description property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the description property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDescription().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Text }
     * 
     * 
     */
    public List<Text> getDescription() {
        if (description == null) {
            description = new ArrayList<Text>();
        }
        return this.description;
    }

    /**
     * Gets the value of the displayName property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the displayName property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDisplayName().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Text }
     * 
     * 
     */
    public List<Text> getDisplayName() {
        if (displayName == null) {
            displayName = new ArrayList<Text>();
        }
        return this.displayName;
    }

    /**
     * Gets the value of the icon property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the icon property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getIcon().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link IconType }
     * 
     * 
     */
    public List<IconType> getIcon() {
        if (icon == null) {
            icon = new ArrayList<IconType>();
        }
        return this.icon;
    }

    /**
     * Gets the value of the ejbName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEjbName() {
        return ejbName;
    }

    /**
     * 	  The ejb-nameType specifies an enterprise bean's name. It is
     * 	  used by ejb-name elements. This name is assigned by the
     * 	  ejb-jar file producer to name the enterprise bean in the
     * 	  ejb-jar file's deployment descriptor. The name must be
     * 	  unique among the names of the enterprise beans in the same
     * 	  ejb-jar file.
     *
     * 	  There is no architected relationship between the used
     * 	  ejb-name in the deployment descriptor and the JNDI name that
     * 	  the Deployer will assign to the enterprise bean's home.
     *
     * 	  The name for an entity bean must conform to the lexical
     * 	  rules for an NMTOKEN.
     *
     * 	  Example:
     *
     * 	  <ejb-name>EmployeeService</ejb-name>
     */
    public void setEjbName(String value) {
        this.ejbName = value;
    }

    /**
     * Gets the value of the mappedName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMappedName() {
        return mappedName;
    }

    /**
     * Sets the value of the mappedName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMappedName(String value) {
        this.mappedName = value;
    }

    /**
     * Gets the value of the home property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHome() {
        return home;
    }

    /**
     * Sets the value of the home property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHome(String value) {
        this.home = value;
    }

    /**
     * Gets the value of the remote property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRemote() {
        return remote;
    }

    /**
     * Sets the value of the remote property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRemote(String value) {
        this.remote = value;
    }

    /**
     * Gets the value of the localHome property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLocalHome() {
        return localHome;
    }

    /**
     * Sets the value of the localHome property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLocalHome(String value) {
        this.localHome = value;
    }

    /**
     * Gets the value of the local property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLocal() {
        return local;
    }

    /**
     * Sets the value of the local property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLocal(String value) {
        this.local = value;
    }

    /**
     * Gets the value of the businessLocal property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the businessLocal property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBusinessLocal().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getBusinessLocal() {
        if (businessLocal == null) {
            businessLocal = new ArrayList<String>();
        }
        return this.businessLocal;
    }

    /**
     * Gets the value of the businessRemote property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the businessRemote property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBusinessRemote().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getBusinessRemote() {
        if (businessRemote == null) {
            businessRemote = new ArrayList<String>();
        }
        return this.businessRemote;
    }

    /**
     * Gets the value of the serviceEndpoint property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getServiceEndpoint() {
        return serviceEndpoint;
    }

    /**
     * Sets the value of the serviceEndpoint property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setServiceEndpoint(String value) {
        this.serviceEndpoint = value;
    }

    /**
     * Gets the value of the ejbClass property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEjbClass() {
        return ejbClass;
    }

    /**
     * Sets the value of the ejbClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEjbClass(String value) {
        this.ejbClass = value;
    }

    /**
     * Gets the value of the sessionType property.
     * 
     * @return
     *     possible object is
     *     {@link SessionTypeType }
     *     
     */
    public SessionTypeType getSessionType() {
        return sessionType;
    }

    /**
     * Sets the value of the sessionType property.
     * 
     * @param value
     *     allowed object is
     *     {@link SessionTypeType }
     *     
     */
    public void setSessionType(SessionTypeType value) {
        this.sessionType = value;
    }

    /**
     * Gets the value of the timeoutMethod property.
     * 
     * @return
     *     possible object is
     *     {@link NamedMethodType }
     *     
     */
    public NamedMethodType getTimeoutMethod() {
        return timeoutMethod;
    }

    /**
     * Sets the value of the timeoutMethod property.
     * 
     * @param value
     *     allowed object is
     *     {@link NamedMethodType }
     *     
     */
    public void setTimeoutMethod(NamedMethodType value) {
        this.timeoutMethod = value;
    }

    /**
     * Gets the value of the initMethod property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the initMethod property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInitMethod().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link InitMethodType }
     * 
     * 
     */
    public List<InitMethodType> getInitMethod() {
        if (initMethod == null) {
            initMethod = new ArrayList<InitMethodType>();
        }
        return this.initMethod;
    }

    /**
     * Gets the value of the removeMethod property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the removeMethod property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRemoveMethod().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RemoveMethodType }
     * 
     * 
     */
    public List<RemoveMethodType> getRemoveMethod() {
        if (removeMethod == null) {
            removeMethod = new ArrayList<RemoveMethodType>();
        }
        return this.removeMethod;
    }

    /**
     * Gets the value of the transactionType property.
     * 
     * @return
     *     possible object is
     *     {@link TransactionTypeType }
     *     
     */
    public TransactionTypeType getTransactionType() {
        return transactionType;
    }

    /**
     * Sets the value of the transactionType property.
     * 
     * @param value
     *     allowed object is
     *     {@link TransactionTypeType }
     *     
     */
    public void setTransactionType(TransactionTypeType value) {
        this.transactionType = value;
    }

    /**
     * Gets the value of the aroundInvoke property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the aroundInvoke property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAroundInvoke().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AroundInvokeType }
     * 
     * 
     */
    public List<AroundInvokeType> getAroundInvoke() {
        if (aroundInvoke == null) {
            aroundInvoke = new ArrayList<AroundInvokeType>();
        }
        return this.aroundInvoke;
    }

    /**
     * Gets the value of the envEntry property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the envEntry property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEnvEntry().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link EnvEntryType }
     * 
     * 
     */
    public List<EnvEntryType> getEnvEntry() {
        if (envEntry == null) {
            envEntry = new ArrayList<EnvEntryType>();
        }
        return this.envEntry;
    }

    /**
     * Gets the value of the ejbRef property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the ejbRef property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEjbRef().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link EjbRefType }
     * 
     * 
     */
    public List<EjbRefType> getEjbRef() {
        if (ejbRef == null) {
            ejbRef = new ArrayList<EjbRefType>();
        }
        return this.ejbRef;
    }

    /**
     * Gets the value of the ejbLocalRef property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the ejbLocalRef property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEjbLocalRef().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link EjbLocalRefType }
     * 
     * 
     */
    public List<EjbLocalRefType> getEjbLocalRef() {
        if (ejbLocalRef == null) {
            ejbLocalRef = new ArrayList<EjbLocalRefType>();
        }
        return this.ejbLocalRef;
    }

    /**
     * Gets the value of the serviceRef property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the serviceRef property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getServiceRef().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ServiceRefType }
     * 
     * 
     */
    public List<ServiceRefType> getServiceRef() {
        if (serviceRef == null) {
            serviceRef = new ArrayList<ServiceRefType>();
        }
        return this.serviceRef;
    }

    /**
     * Gets the value of the resourceRef property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the resourceRef property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getResourceRef().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ResourceRefType }
     * 
     * 
     */
    public List<ResourceRefType> getResourceRef() {
        if (resourceRef == null) {
            resourceRef = new ArrayList<ResourceRefType>();
        }
        return this.resourceRef;
    }

    /**
     * Gets the value of the resourceEnvRef property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the resourceEnvRef property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getResourceEnvRef().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ResourceEnvRefType }
     * 
     * 
     */
    public List<ResourceEnvRefType> getResourceEnvRef() {
        if (resourceEnvRef == null) {
            resourceEnvRef = new ArrayList<ResourceEnvRefType>();
        }
        return this.resourceEnvRef;
    }

    /**
     * Gets the value of the messageDestinationRef property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the messageDestinationRef property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMessageDestinationRef().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link MessageDestinationRefType }
     * 
     * 
     */
    public List<MessageDestinationRefType> getMessageDestinationRef() {
        if (messageDestinationRef == null) {
            messageDestinationRef = new ArrayList<MessageDestinationRefType>();
        }
        return this.messageDestinationRef;
    }

    /**
     * Gets the value of the persistenceContextRef property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the persistenceContextRef property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPersistenceContextRef().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PersistenceContextRefType }
     * 
     * 
     */
    public List<PersistenceContextRefType> getPersistenceContextRef() {
        if (persistenceContextRef == null) {
            persistenceContextRef = new ArrayList<PersistenceContextRefType>();
        }
        return this.persistenceContextRef;
    }

    /**
     * Gets the value of the persistenceUnitRef property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the persistenceUnitRef property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPersistenceUnitRef().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PersistenceUnitRefType }
     * 
     * 
     */
    public List<PersistenceUnitRefType> getPersistenceUnitRef() {
        if (persistenceUnitRef == null) {
            persistenceUnitRef = new ArrayList<PersistenceUnitRefType>();
        }
        return this.persistenceUnitRef;
    }

    /**
     * Gets the value of the postConstruct property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the postConstruct property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPostConstruct().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LifecycleCallbackType }
     * 
     * 
     */
    public List<LifecycleCallbackType> getPostConstruct() {
        if (postConstruct == null) {
            postConstruct = new ArrayList<LifecycleCallbackType>();
        }
        return this.postConstruct;
    }

    /**
     * Gets the value of the preDestroy property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the preDestroy property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPreDestroy().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LifecycleCallbackType }
     * 
     * 
     */
    public List<LifecycleCallbackType> getPreDestroy() {
        if (preDestroy == null) {
            preDestroy = new ArrayList<LifecycleCallbackType>();
        }
        return this.preDestroy;
    }

    /**
     * Gets the value of the postActivate property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the postActivate property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPostActivate().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LifecycleCallbackType }
     * 
     * 
     */
    public List<LifecycleCallbackType> getPostActivate() {
        if (postActivate == null) {
            postActivate = new ArrayList<LifecycleCallbackType>();
        }
        return this.postActivate;
    }

    /**
     * Gets the value of the prePassivate property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the prePassivate property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPrePassivate().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LifecycleCallbackType }
     * 
     * 
     */
    public List<LifecycleCallbackType> getPrePassivate() {
        if (prePassivate == null) {
            prePassivate = new ArrayList<LifecycleCallbackType>();
        }
        return this.prePassivate;
    }

    /**
     * Gets the value of the securityRoleRef property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the securityRoleRef property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSecurityRoleRef().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SecurityRoleRefType }
     * 
     * 
     */
    public List<SecurityRoleRefType> getSecurityRoleRef() {
        if (securityRoleRef == null) {
            securityRoleRef = new ArrayList<SecurityRoleRefType>();
        }
        return this.securityRoleRef;
    }

    /**
     * Gets the value of the securityIdentity property.
     * 
     * @return
     *     possible object is
     *     {@link SecurityIdentityType }
     *     
     */
    public SecurityIdentityType getSecurityIdentity() {
        return securityIdentity;
    }

    /**
     * Sets the value of the securityIdentity property.
     * 
     * @param value
     *     allowed object is
     *     {@link SecurityIdentityType }
     *     
     */
    public void setSecurityIdentity(SecurityIdentityType value) {
        this.securityIdentity = value;
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
