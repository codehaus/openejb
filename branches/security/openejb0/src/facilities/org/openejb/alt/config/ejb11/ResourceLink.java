/*
 * This class was automatically generated with 
 * <a href="http://castor.exolab.org">Castor 0.9.3.9+</a>, using an
 * XML Schema.
 * $Id$
 */

package org.openejb.alt.config.ejb11;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import org.exolab.castor.xml.*;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.xml.sax.DocumentHandler;

/**
 * 
 * @version $Revision$ $Date$
**/
public class ResourceLink implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    private java.lang.String _resRefName;

    private java.lang.String _resId;


      //----------------/
     //- Constructors -/
    //----------------/

    public ResourceLink() {
        super();
    } //-- org.openejb.alt.config.ejb11.ResourceLink()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Returns the value of field 'resId'.
     * @return the value of field 'resId'.
    **/
    public java.lang.String getResId()
    {
        return this._resId;
    } //-- java.lang.String getResId() 

    /**
     * Returns the value of field 'resRefName'.
     * @return the value of field 'resRefName'.
    **/
    public java.lang.String getResRefName()
    {
        return this._resRefName;
    } //-- java.lang.String getResRefName() 

    /**
    **/
    public boolean isValid()
    {
        try {
            validate();
        }
        catch (org.exolab.castor.xml.ValidationException vex) {
            return false;
        }
        return true;
    } //-- boolean isValid() 

    /**
     * 
     * @param out
    **/
    public void marshal(java.io.Writer out)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        
        Marshaller.marshal(this, out);
    } //-- void marshal(java.io.Writer) 

    /**
     * 
     * @param handler
    **/
    public void marshal(org.xml.sax.DocumentHandler handler)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        
        Marshaller.marshal(this, handler);
    } //-- void marshal(org.xml.sax.DocumentHandler) 

    /**
     * Sets the value of field 'resId'.
     * @param resId the value of field 'resId'.
    **/
    public void setResId(java.lang.String resId)
    {
        this._resId = resId;
    } //-- void setResId(java.lang.String) 

    /**
     * Sets the value of field 'resRefName'.
     * @param resRefName the value of field 'resRefName'.
    **/
    public void setResRefName(java.lang.String resRefName)
    {
        this._resRefName = resRefName;
    } //-- void setResRefName(java.lang.String) 

    /**
     * 
     * @param reader
    **/
    public static org.openejb.alt.config.ejb11.ResourceLink unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (org.openejb.alt.config.ejb11.ResourceLink) Unmarshaller.unmarshal(org.openejb.alt.config.ejb11.ResourceLink.class, reader);
    } //-- org.openejb.alt.config.ejb11.ResourceLink unmarshal(java.io.Reader) 

    /**
    **/
    public void validate()
        throws org.exolab.castor.xml.ValidationException
    {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    } //-- void validate() 

}
