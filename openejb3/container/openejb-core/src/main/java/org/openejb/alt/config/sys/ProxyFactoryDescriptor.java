/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.5.3</a>, using an XML
 * Schema.
 * $Id: ProxyFactoryDescriptor.java,v 1.2 2004/03/31 00:45:22 dblevins Exp $
 */

package org.openejb.alt.config.sys;

//---------------------------------/

import org.exolab.castor.mapping.AccessMode;
import org.exolab.castor.xml.TypeValidator;
import org.exolab.castor.xml.XMLFieldDescriptor;
import org.exolab.castor.xml.validators.*;

public class ProxyFactoryDescriptor extends org.exolab.castor.xml.util.XMLClassDescriptorImpl {


    private java.lang.String nsPrefix;

    private java.lang.String nsURI;

    private java.lang.String xmlName;

    private org.exolab.castor.xml.XMLFieldDescriptor identity;


    public ProxyFactoryDescriptor() {
        super();
        nsURI = "http://www.openejb.org/System/Configuration";
        xmlName = "ProxyFactory";
        org.exolab.castor.xml.util.XMLFieldDescriptorImpl desc = null;
        org.exolab.castor.xml.XMLFieldHandler handler = null;
        org.exolab.castor.xml.FieldValidator fieldValidator = null;

        desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(java.lang.String.class, "_content", "PCDATA", org.exolab.castor.xml.NodeType.Text);
        desc.setImmutable(true);
        handler = (new org.exolab.castor.xml.XMLFieldHandler() {
            public java.lang.Object getValue(java.lang.Object object)
                    throws IllegalStateException {
                ProxyFactory target = (ProxyFactory) object;
                return target.getContent();
            }

            public void setValue(java.lang.Object object, java.lang.Object value)
                    throws IllegalStateException, IllegalArgumentException {
                try {
                    ProxyFactory target = (ProxyFactory) object;
                    target.setContent((java.lang.String) value);
                }
                catch (java.lang.Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }

            public java.lang.Object newInstance(java.lang.Object parent) {
                return null;
            }
        });
        desc.setHandler(handler);
        addFieldDescriptor(desc);


        fieldValidator = new org.exolab.castor.xml.FieldValidator();
        {
            StringValidator typeValidator = new StringValidator();
            typeValidator.setWhiteSpace("preserve");
            fieldValidator.setValidator(typeValidator);
        }
        desc.setValidator(fieldValidator);


        desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(java.lang.String.class, "_id", "id", org.exolab.castor.xml.NodeType.Attribute);
        desc.setImmutable(true);
        handler = (new org.exolab.castor.xml.XMLFieldHandler() {
            public java.lang.Object getValue(java.lang.Object object)
                    throws IllegalStateException {
                ProxyFactory target = (ProxyFactory) object;
                return target.getId();
            }

            public void setValue(java.lang.Object object, java.lang.Object value)
                    throws IllegalStateException, IllegalArgumentException {
                try {
                    ProxyFactory target = (ProxyFactory) object;
                    target.setId((java.lang.String) value);
                }
                catch (java.lang.Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }

            public java.lang.Object newInstance(java.lang.Object parent) {
                return null;
            }
        });
        desc.setHandler(handler);
        desc.setRequired(true);
        addFieldDescriptor(desc);


        fieldValidator = new org.exolab.castor.xml.FieldValidator();
        fieldValidator.setMinOccurs(1);
        {
            StringValidator typeValidator = new StringValidator();
            typeValidator.setWhiteSpace("preserve");
            fieldValidator.setValidator(typeValidator);
        }
        desc.setValidator(fieldValidator);

        desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(java.lang.String.class, "_provider", "provider", org.exolab.castor.xml.NodeType.Attribute);
        desc.setImmutable(true);
        handler = (new org.exolab.castor.xml.XMLFieldHandler() {
            public java.lang.Object getValue(java.lang.Object object)
                    throws IllegalStateException {
                ProxyFactory target = (ProxyFactory) object;
                return target.getProvider();
            }

            public void setValue(java.lang.Object object, java.lang.Object value)
                    throws IllegalStateException, IllegalArgumentException {
                try {
                    ProxyFactory target = (ProxyFactory) object;
                    target.setProvider((java.lang.String) value);
                }
                catch (java.lang.Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }

            public java.lang.Object newInstance(java.lang.Object parent) {
                return null;
            }
        });
        desc.setHandler(handler);
        addFieldDescriptor(desc);


        fieldValidator = new org.exolab.castor.xml.FieldValidator();
        {
            StringValidator typeValidator = new StringValidator();
            typeValidator.setWhiteSpace("preserve");
            fieldValidator.setValidator(typeValidator);
        }
        desc.setValidator(fieldValidator);

        desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(java.lang.String.class, "_jar", "jar", org.exolab.castor.xml.NodeType.Attribute);
        desc.setImmutable(true);
        handler = (new org.exolab.castor.xml.XMLFieldHandler() {
            public java.lang.Object getValue(java.lang.Object object)
                    throws IllegalStateException {
                ProxyFactory target = (ProxyFactory) object;
                return target.getJar();
            }

            public void setValue(java.lang.Object object, java.lang.Object value)
                    throws IllegalStateException, IllegalArgumentException {
                try {
                    ProxyFactory target = (ProxyFactory) object;
                    target.setJar((java.lang.String) value);
                }
                catch (java.lang.Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }

            public java.lang.Object newInstance(java.lang.Object parent) {
                return null;
            }
        });
        desc.setHandler(handler);
        addFieldDescriptor(desc);


        fieldValidator = new org.exolab.castor.xml.FieldValidator();
        {
            StringValidator typeValidator = new StringValidator();
            typeValidator.setWhiteSpace("preserve");
            typeValidator.setPattern(".*\\.jar$");
            fieldValidator.setValidator(typeValidator);
        }
        desc.setValidator(fieldValidator);


    }


    public org.exolab.castor.mapping.AccessMode getAccessMode() {
        return null;
    }

    public org.exolab.castor.mapping.ClassDescriptor getExtends() {
        return null;
    }

    public org.exolab.castor.mapping.FieldDescriptor getIdentity() {
        return identity;
    }

    public java.lang.Class getJavaClass() {
        return org.openejb.alt.config.sys.ProxyFactory.class;
    }

    public java.lang.String getNameSpacePrefix() {
        return nsPrefix;
    }

    public java.lang.String getNameSpaceURI() {
        return nsURI;
    }

    public org.exolab.castor.xml.TypeValidator getValidator() {
        return this;
    }

    public java.lang.String getXMLName() {
        return xmlName;
    }

}
