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
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact openejb-group@openejb.sf.net.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.sf.net/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE OPENEJB GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE OPENEJB GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2005 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.corba;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.rmi.RemoteException;
import javax.ejb.EJBHome;
import javax.ejb.HomeHandle;
import javax.ejb.spi.HandleDelegate;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import org.openejb.corba.util.Util;


/**
 * EJB v2.1 spec, section 19.5.5.1
 * <p/>
 * The <code>javax.ejb.spi.HandleDelegate</code> service provider interface
 * defines methods that enable portable implementations of <code>Handle</code>
 * and <code>HomeHandle</code> that are instantiated in a different vendor’s
 * container to serialize and deserialize EJBObject and EJBHome references.
 * The <code>HandleDelegate</code> interface is not used by enterprise beans
 * or J2EE application components directly.
 *
 * @version $Revision$ $Date$
 */
public class CORBAHomeHandle implements HomeHandle, Serializable {

    private String ior;

    public CORBAHomeHandle(String ior) {
        this.ior = ior;
    }

    public EJBHome getEJBHome() throws RemoteException {

        try {
            return (EJBHome) PortableRemoteObject.narrow(Util.getORB().string_to_object(ior), EJBHome.class);
        } catch (Exception e) {
            throw new RemoteException("Unable to convert IOR into home", e);
        }

    }

    private void writeObject(ObjectOutputStream out) throws IOException {

        HandleDelegate handleDelegate;

        try {
            handleDelegate = Util.getHandleDelegate();
        } catch (NamingException e) {
            throw new IOException("Unable to lookup java:comp/HandleDelegate");
        }

        handleDelegate.writeEJBHome(getEJBHome(), out);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {

        HandleDelegate handleDelegate;

        try {
            handleDelegate = Util.getHandleDelegate();
        } catch (NamingException e) {
            throw new IOException("Cannot get HandleDelegate");
        }

        EJBHome home = handleDelegate.readEJBHome(in);

        try {
            ior = Util.getORB().object_to_string((org.omg.CORBA.Object) home);
        } catch (Exception e) {
            throw new RemoteException("Unable to convert object to IOR", e);
        }
    }
}