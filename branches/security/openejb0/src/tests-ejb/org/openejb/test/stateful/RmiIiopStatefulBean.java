/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "Exolab" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of Exoffice Technologies.  For written permission,
 *    please contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Exoffice Technologies. Exolab is a registered
 *    trademark of Exoffice Technologies.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.exolab.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY EXOFFICE TECHNOLOGIES AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * EXOFFICE TECHNOLOGIES OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 1999 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.test.stateful;

import java.rmi.RemoteException;
import javax.ejb.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.openejb.test.object.ObjectGraph;

/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class RmiIiopStatefulBean implements javax.ejb.SessionBean{
    
    private String name;
    private SessionContext ejbContext;
    
    
    //=============================
    // Home interface methods
    //    
    //    
    // Home interface methods
    //=============================
    

    //=============================
    // Remote interface methods
    //    
    /*-------------------------------------------------*/
    /*  String                                         */  
    /*-------------------------------------------------*/
    
    public String returnStringObject(String data) {
        return data;
    }
    
    public String[] returnStringObjectArray(String[] data) {
        return data;
    }
    
    /*-------------------------------------------------*/
    /*  Character                                      */  
    /*-------------------------------------------------*/
    
    public Character returnCharacterObject(Character data) {
        return data;
    }
    
    public char returnCharacterPrimitive(char data) {
        return data;
    }
    
    public Character[] returnCharacterObjectArray(Character[] data) {
        return data;
    }
    
    public char[] returnCharacterPrimitiveArray(char[] data) {
        return data;
    }
    /*-------------------------------------------------*/
    /*  Boolean                                        */  
    /*-------------------------------------------------*/
    
    public Boolean returnBooleanObject(Boolean data) {
        return data;
    }
    
    public boolean returnBooleanPrimitive(boolean data) {
        return data;
    }
    
    public Boolean[] returnBooleanObjectArray(Boolean[] data) {
        return data;
    }
    
    public boolean[] returnBooleanPrimitiveArray(boolean[] data) {
        return data;
    }
    
    /*-------------------------------------------------*/
    /*  Byte                                           */  
    /*-------------------------------------------------*/
    
    public Byte returnByteObject(Byte data) {
        return data;
    }
    
    public byte returnBytePrimitive(byte data) {
        return data;
    }
    
    public Byte[] returnByteObjectArray(Byte[] data) {
        return data;
    }
    
    public byte[] returnBytePrimitiveArray(byte[] data) {
        return data;
    }
    
    /*-------------------------------------------------*/
    /*  Short                                          */  
    /*-------------------------------------------------*/
    
    public Short returnShortObject(Short data) {
        return data;
    }
    
    public short returnShortPrimitive(short data) {
        return data;
    }
    
    public Short[] returnShortObjectArray(Short[] data) {
        return data;
    }
    
    public short[] returnShortPrimitiveArray(short[] data) {
        return data;
    }
    
    /*-------------------------------------------------*/
    /*  Integer                                        */  
    /*-------------------------------------------------*/
    
    public Integer returnIntegerObject(Integer data) {
        return data;
    }
    
    public int returnIntegerPrimitive(int data) {
        return data;
    }
    
    public Integer[] returnIntegerObjectArray(Integer[] data) {
        return data;
    }
    
    public int[] returnIntegerPrimitiveArray(int[] data) {
        return data;
    }
    
    /*-------------------------------------------------*/
    /*  Long                                           */  
    /*-------------------------------------------------*/
    
    public Long returnLongObject(Long data) {
        return data;
    }
    
    public long returnLongPrimitive(long data) {
        return data;
    }
    
    public Long[] returnLongObjectArray(Long[] data) {
        return data;
    }
    
    public long[] returnLongPrimitiveArray(long[] data) {
        return data;
    }
    
    /*-------------------------------------------------*/
    /*  Float                                          */  
    /*-------------------------------------------------*/
    
    public Float returnFloatObject(Float data) {
        return data;
    }
    
    public float returnFloatPrimitive(float data) {
        return data;
    }
    
    public Float[] returnFloatObjectArray(Float[] data) {
        return data;
    }
    
    public float[] returnFloatPrimitiveArray(float[] data) {
        return data;
    }
    
    /*-------------------------------------------------*/
    /*  Double                                         */  
    /*-------------------------------------------------*/
    
    public Double returnDoubleObject(Double data) {
        return data;
    }
    
    public double returnDoublePrimitive(double data) {
        return data;
    }
    
    public Double[] returnDoubleObjectArray(Double[] data) {
        return data;
    }
    
    public double[] returnDoublePrimitiveArray(double[] data) {
        return data;
    }
    
    
    /*-------------------------------------------------*/
    /*  EJBHome                                         */  
    /*-------------------------------------------------*/
    
    public EJBHome returnEJBHome(EJBHome data) {
        return data;
    }
    
    public EJBHome returnEJBHome() throws javax.ejb.EJBException{
        EJBHome data = null;

        try{
        InitialContext ctx = new InitialContext();

        data = (EJBHome)ctx.lookup("java:comp/env/stateful/rmi-iiop/home");

        } catch (Exception e){
            e.printStackTrace();
            throw new javax.ejb.EJBException(e);
        }
        return data;
    }

    public ObjectGraph returnNestedEJBHome() throws javax.ejb.EJBException{
        ObjectGraph data = null; 

        try{
        InitialContext ctx = new InitialContext();

        Object object = ctx.lookup("java:comp/env/stateful/rmi-iiop/home");
        data = new ObjectGraph(object);

        } catch (Exception e){
            throw new javax.ejb.EJBException(e);
        }
        return data;
    }

    public EJBHome[] returnEJBHomeArray(EJBHome[] data) {
        return data;
    }
    
    /*-------------------------------------------------*/
    /*  EJBObject                                      */  
    /*-------------------------------------------------*/
    
    public EJBObject returnEJBObject(EJBObject data) {
        return data;
    }
    
    public EJBObject returnEJBObject() throws javax.ejb.EJBException{
        EncStatefulObject data = null;

        try{
        InitialContext ctx = new InitialContext();

        EncStatefulHome home = (EncStatefulHome)ctx.lookup("java:comp/env/stateful/rmi-iiop/home");
        data = home.create("Test01 StatefulBean");

        } catch (Exception e){
            throw new javax.ejb.EJBException(e);
        }
        return data;
    }
    
    public ObjectGraph returnNestedEJBObject() throws javax.ejb.EJBException{
        ObjectGraph data = null;

        try{
        InitialContext ctx = new InitialContext();

        EncStatefulHome home = (EncStatefulHome)ctx.lookup("java:comp/env/stateful/rmi-iiop/home");
        EncStatefulObject object = home.create("Test02 StatefulBean");
        data = new ObjectGraph(object);

        } catch (Exception e){
            throw new javax.ejb.EJBException(e);
        }
        return data;
    }
    
    public EJBObject[] returnEJBObjectArray(EJBObject[] data) {
        return data;
    }
    
    /*-------------------------------------------------*/
    /*  EJBMetaData                                         */  
    /*-------------------------------------------------*/
    
    public EJBMetaData returnEJBMetaData(EJBMetaData data) {
        return data;
    }
    
    public EJBMetaData returnEJBMetaData() throws javax.ejb.EJBException{
        EJBMetaData data = null;

        try{
        InitialContext ctx = new InitialContext();

        EncStatefulHome home = (EncStatefulHome)ctx.lookup("java:comp/env/stateful/rmi-iiop/home");
        data = home.getEJBMetaData();

        } catch (Exception e){
            throw new javax.ejb.EJBException(e);
        }
        return data;
    }
    
    public ObjectGraph returnNestedEJBMetaData() throws javax.ejb.EJBException{
        ObjectGraph data = null;

        try{
        InitialContext ctx = new InitialContext();

        EncStatefulHome home = (EncStatefulHome)ctx.lookup("java:comp/env/stateful/rmi-iiop/home");
        EJBMetaData object = home.getEJBMetaData();
        data = new ObjectGraph(object);

        } catch (Exception e){
            throw new javax.ejb.EJBException(e);
        }
        return data;
    }
    
    public EJBMetaData[] returnEJBMetaDataArray(EJBMetaData[] data) {
        return data;
    }
    
    /*-------------------------------------------------*/
    /*  Handle                                         */  
    /*-------------------------------------------------*/
    
    public Handle returnHandle(Handle data) {
        return data;
    }
    
    public Handle returnHandle() throws javax.ejb.EJBException{
        Handle data = null;

        try{
        InitialContext ctx = new InitialContext();

        EncStatefulHome home = (EncStatefulHome)ctx.lookup("java:comp/env/stateful/rmi-iiop/home");
        EncStatefulObject object = home.create("Test03 StatefulBean");
        data = object.getHandle();

        } catch (Exception e){
            throw new javax.ejb.EJBException(e);
        }
        return data;
    }
    
    public ObjectGraph returnNestedHandle() throws javax.ejb.EJBException{
        ObjectGraph data = null;

        try{
        InitialContext ctx = new InitialContext();

        EncStatefulHome home = (EncStatefulHome)ctx.lookup("java:comp/env/stateful/rmi-iiop/home");
        EncStatefulObject object = home.create("Test04 StatefulBean");
        data = new ObjectGraph(object.getHandle());

        } catch (Exception e){
            throw new javax.ejb.EJBException(e);
        }
        return data;
    }
    
    public Handle[] returnHandleArray(Handle[] data) {
        return data;
    }
    
    /*-------------------------------------------------*/
    /*  ObjectGraph                                         */  
    /*-------------------------------------------------*/
    
    public ObjectGraph returnObjectGraph(ObjectGraph data) {
        return data;
    }
    
    public ObjectGraph[] returnObjectGraphArray(ObjectGraph[] data) {
        return data;
    }
    //    
    // Remote interface methods
    //=============================


    //================================
    // SessionBean interface methods
    //    
    /**
     * Set the associated session context. The container calls this method
     * after the instance creation.
     */
    public void setSessionContext(SessionContext ctx) throws EJBException,RemoteException {
        ejbContext = ctx;
    }
    /**
     * 
     * @param name
     * @exception javax.ejb.CreateException
     */
    public void ejbCreate(String name) throws javax.ejb.CreateException{
        this.name = name;
    }
    /**
     * A container invokes this method before it ends the life of the session
     * object. This happens as a result of a client's invoking a remove
     * operation, or when a container decides to terminate the session object
     * after a timeout.
     */
    public void ejbRemove() throws EJBException,RemoteException {
    }

    /**
     * The activate method is called when the instance is activated
     * from its "passive" state. The instance should acquire any resource
     * that it has released earlier in the ejbPassivate() method.
     */
    public void ejbActivate() throws EJBException,RemoteException {
        // Should never called.
    }
    /**
     * The passivate method is called before the instance enters
     * the "passive" state. The instance should release any resources that
     * it can re-acquire later in the ejbActivate() method.
     */
    public void ejbPassivate() throws EJBException,RemoteException {
        // Should never called.
    }

    //    
    // SessionBean interface methods
    //================================
}
