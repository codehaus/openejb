/*
 * This class was automatically generated with 
 * <a href="http://castor.exolab.org">Castor 0.9.2</a>, using an
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
import java.util.Enumeration;
import java.util.Vector;
import org.exolab.castor.xml.*;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.xml.sax.DocumentHandler;

/**
 * 
 * @version $Revision$ $Date$
**/
public class MethodParams implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    private java.lang.String _id;

    private java.util.Vector _methodParamList;


      //----------------/
     //- Constructors -/
    //----------------/

    public MethodParams() {
        super();
        _methodParamList = new Vector();
    } //-- org.openejb.alt.config.ejb11.MethodParams()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * @param vMethodParam
    **/
    public void addMethodParam(java.lang.String vMethodParam)
        throws java.lang.IndexOutOfBoundsException
    {
        _methodParamList.addElement(vMethodParam);
    } //-- void addMethodParam(java.lang.String) 

    /**
     * 
     * @param index
     * @param vMethodParam
    **/
    public void addMethodParam(int index, java.lang.String vMethodParam)
        throws java.lang.IndexOutOfBoundsException
    {
        _methodParamList.insertElementAt(vMethodParam, index);
    } //-- void addMethodParam(int, java.lang.String) 

    /**
    **/
    public java.util.Enumeration enumerateMethodParam()
    {
        return _methodParamList.elements();
    } //-- java.util.Enumeration enumerateMethodParam() 

    /**
     * Returns the value of field 'id'.
     * @return the value of field 'id'.
    **/
    public java.lang.String getId()
    {
        return this._id;
    } //-- java.lang.String getId() 

    /**
     * 
     * @param index
    **/
    public java.lang.String getMethodParam(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _methodParamList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (String)_methodParamList.elementAt(index);
    } //-- java.lang.String getMethodParam(int) 

    /**
    **/
    public java.lang.String[] getMethodParam()
    {
        int size = _methodParamList.size();
        java.lang.String[] mArray = new java.lang.String[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (String)_methodParamList.elementAt(index);
        }
        return mArray;
    } //-- java.lang.String[] getMethodParam() 

    /**
    **/
    public int getMethodParamCount()
    {
        return _methodParamList.size();
    } //-- int getMethodParamCount() 

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
    **/
    public void removeAllMethodParam()
    {
        _methodParamList.removeAllElements();
    } //-- void removeAllMethodParam() 

    /**
     * 
     * @param index
    **/
    public java.lang.String removeMethodParam(int index)
    {
        java.lang.Object obj = _methodParamList.elementAt(index);
        _methodParamList.removeElementAt(index);
        return (String)obj;
    } //-- java.lang.String removeMethodParam(int) 

    /**
     * Sets the value of field 'id'.
     * @param id the value of field 'id'.
    **/
    public void setId(java.lang.String id)
    {
        this._id = id;
    } //-- void setId(java.lang.String) 

    /**
     * 
     * @param index
     * @param vMethodParam
    **/
    public void setMethodParam(int index, java.lang.String vMethodParam)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _methodParamList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _methodParamList.setElementAt(vMethodParam, index);
    } //-- void setMethodParam(int, java.lang.String) 

    /**
     * 
     * @param methodParamArray
    **/
    public void setMethodParam(java.lang.String[] methodParamArray)
    {
        //-- copy array
        _methodParamList.removeAllElements();
        for (int i = 0; i < methodParamArray.length; i++) {
            _methodParamList.addElement(methodParamArray[i]);
        }
    } //-- void setMethodParam(java.lang.String) 

    /**
     * 
     * @param reader
    **/
    public static org.openejb.alt.config.ejb11.MethodParams unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (org.openejb.alt.config.ejb11.MethodParams) Unmarshaller.unmarshal(org.openejb.alt.config.ejb11.MethodParams.class, reader);
    } //-- org.openejb.alt.config.ejb11.MethodParams unmarshal(java.io.Reader) 

    /**
    **/
    public void validate()
        throws org.exolab.castor.xml.ValidationException
    {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    } //-- void validate() 

}
