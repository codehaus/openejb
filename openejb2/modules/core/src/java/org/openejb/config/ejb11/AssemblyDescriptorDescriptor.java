/*
 * This class was automatically generated with 
 * <a href="http://castor.exolab.org">Castor 0.9.2</a>, using an
 * XML Schema.
 * $Id$
 */

package org.openejb.config.ejb11;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import org.exolab.castor.xml.FieldValidator;
import org.exolab.castor.xml.NodeType;
import org.exolab.castor.xml.XMLFieldHandler;
import org.exolab.castor.xml.util.XMLFieldDescriptorImpl;

/**
 * 
 * @version $Revision$ $Date$
**/
public class AssemblyDescriptorDescriptor extends org.exolab.castor.xml.util.XMLClassDescriptorImpl {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    private java.lang.String nsPrefix;

    private java.lang.String nsURI;

    private java.lang.String xmlName;

    private org.exolab.castor.xml.XMLFieldDescriptor identity;


      //----------------/
     //- Constructors -/
    //----------------/

    public AssemblyDescriptorDescriptor() {
        super();
        nsURI = "http://www.openejb.org/ejb-jar/1.1";
        xmlName = "assembly-descriptor";
        XMLFieldDescriptorImpl  desc           = null;
        XMLFieldHandler         handler        = null;
        FieldValidator          fieldValidator = null;
        
        //-- set grouping compositor
        setCompositorAsSequence();
        //-- initialize attribute descriptors
        
        //-- _id
        desc = new XMLFieldDescriptorImpl(java.lang.String.class, "_id", "id", NodeType.Attribute);
        this.identity = desc;
        handler = (new XMLFieldHandler() {
            public Object getValue( Object object ) 
                throws IllegalStateException
            {
                AssemblyDescriptor target = (AssemblyDescriptor) object;
                return target.getId();
            }
            public void setValue( Object object, Object value) 
                throws IllegalStateException, IllegalArgumentException
            {
                try {
                    AssemblyDescriptor target = (AssemblyDescriptor) object;
                    target.setId( (java.lang.String) value);
                }
                catch (Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }
            public Object newInstance( Object parent ) {
                return new java.lang.String();
            }
        } );
        desc.setHandler(handler);
        desc.setNameSpaceURI("http://www.openejb.org/ejb-jar/1.1");
        addFieldDescriptor(desc);
        
        //-- validation code for: _id
        fieldValidator = new FieldValidator();
        desc.setValidator(fieldValidator);
        
        //-- initialize element descriptors
        
        //-- _securityRoleList
        desc = new XMLFieldDescriptorImpl(SecurityRole.class, "_securityRoleList", "security-role", NodeType.Element);
        handler = (new XMLFieldHandler() {
            public Object getValue( Object object ) 
                throws IllegalStateException
            {
                AssemblyDescriptor target = (AssemblyDescriptor) object;
                return target.getSecurityRole();
            }
            public void setValue( Object object, Object value) 
                throws IllegalStateException, IllegalArgumentException
            {
                try {
                    AssemblyDescriptor target = (AssemblyDescriptor) object;
                    target.addSecurityRole( (SecurityRole) value);
                }
                catch (Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }
            public Object newInstance( Object parent ) {
                return new SecurityRole();
            }
        } );
        desc.setHandler(handler);
        desc.setNameSpaceURI("http://www.openejb.org/ejb-jar/1.1");
        desc.setMultivalued(true);
        addFieldDescriptor(desc);
        
        //-- validation code for: _securityRoleList
        fieldValidator = new FieldValidator();
        fieldValidator.setMinOccurs(0);
        desc.setValidator(fieldValidator);
        
        //-- _methodPermissionList
        desc = new XMLFieldDescriptorImpl(MethodPermission.class, "_methodPermissionList", "method-permission", NodeType.Element);
        handler = (new XMLFieldHandler() {
            public Object getValue( Object object ) 
                throws IllegalStateException
            {
                AssemblyDescriptor target = (AssemblyDescriptor) object;
                return target.getMethodPermission();
            }
            public void setValue( Object object, Object value) 
                throws IllegalStateException, IllegalArgumentException
            {
                try {
                    AssemblyDescriptor target = (AssemblyDescriptor) object;
                    target.addMethodPermission( (MethodPermission) value);
                }
                catch (Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }
            public Object newInstance( Object parent ) {
                return new MethodPermission();
            }
        } );
        desc.setHandler(handler);
        desc.setNameSpaceURI("http://www.openejb.org/ejb-jar/1.1");
        desc.setMultivalued(true);
        addFieldDescriptor(desc);
        
        //-- validation code for: _methodPermissionList
        fieldValidator = new FieldValidator();
        fieldValidator.setMinOccurs(0);
        desc.setValidator(fieldValidator);
        
        //-- _containerTransactionList
        desc = new XMLFieldDescriptorImpl(ContainerTransaction.class, "_containerTransactionList", "container-transaction", NodeType.Element);
        handler = (new XMLFieldHandler() {
            public Object getValue( Object object ) 
                throws IllegalStateException
            {
                AssemblyDescriptor target = (AssemblyDescriptor) object;
                return target.getContainerTransaction();
            }
            public void setValue( Object object, Object value) 
                throws IllegalStateException, IllegalArgumentException
            {
                try {
                    AssemblyDescriptor target = (AssemblyDescriptor) object;
                    target.addContainerTransaction( (ContainerTransaction) value);
                }
                catch (Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }
            public Object newInstance( Object parent ) {
                return new ContainerTransaction();
            }
        } );
        desc.setHandler(handler);
        desc.setNameSpaceURI("http://www.openejb.org/ejb-jar/1.1");
        desc.setMultivalued(true);
        addFieldDescriptor(desc);
        
        //-- validation code for: _containerTransactionList
        fieldValidator = new FieldValidator();
        fieldValidator.setMinOccurs(0);
        desc.setValidator(fieldValidator);
        
    } //-- org.openejb.config.ejb11.AssemblyDescriptorDescriptor()


      //-----------/
     //- Methods -/
    //-----------/

    /**
    **/
    public org.exolab.castor.mapping.AccessMode getAccessMode()
    {
        return null;
    } //-- org.exolab.castor.mapping.AccessMode getAccessMode() 

    /**
    **/
    public org.exolab.castor.mapping.ClassDescriptor getExtends()
    {
        return null;
    } //-- org.exolab.castor.mapping.ClassDescriptor getExtends() 

    /**
    **/
    public org.exolab.castor.mapping.FieldDescriptor getIdentity()
    {
        return identity;
    } //-- org.exolab.castor.mapping.FieldDescriptor getIdentity() 

    /**
    **/
    public java.lang.Class getJavaClass()
    {
        return org.openejb.config.ejb11.AssemblyDescriptor.class;
    } //-- java.lang.Class getJavaClass() 

    /**
    **/
    public java.lang.String getNameSpacePrefix()
    {
        return nsPrefix;
    } //-- java.lang.String getNameSpacePrefix() 

    /**
    **/
    public java.lang.String getNameSpaceURI()
    {
        return nsURI;
    } //-- java.lang.String getNameSpaceURI() 

    /**
    **/
    public org.exolab.castor.xml.TypeValidator getValidator()
    {
        return this;
    } //-- org.exolab.castor.xml.TypeValidator getValidator() 

    /**
    **/
    public java.lang.String getXMLName()
    {
        return xmlName;
    } //-- java.lang.String getXMLName() 

}
