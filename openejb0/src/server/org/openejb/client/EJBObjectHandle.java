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
 * Copyright 2001 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.client;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.rmi.RemoteException;
import javax.ejb.EJBObject;

/**
 * -------------------------------------
 * EJB 1.1
 *
 * 9.3.4 Handle class
 *
 * The deployment tools are responsible for implementing the handle class for
 * the entity bean. The handle class must be serializable by the Java
 * programming language Serialization protocol.
 *
 * As the handle class is not entity bean specific, the container may, but is
 * not required to, use a single class for all deployed entity beans.
 * -------------------------------------
 *
 * The handle class for all deployed beans, not just entity beans.
 *
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @since 11/25/2001
 */
public class EJBObjectHandle implements java.io.Externalizable , javax.ejb.Handle {

    protected transient EJBObjectProxy ejbObjectProxy;
    protected transient EJBObjectHandler handler;

    /** Public no-arg constructor required by Externalizable API */
    public EJBObjectHandle() {}

    public EJBObjectHandle(EJBObjectProxy proxy) {
        this.ejbObjectProxy = proxy;
        this.handler = ejbObjectProxy.getEJBObjectHandler();
    }

    protected void setEJBObjectProxy(EJBObjectProxy ejbObjectProxy) {
        this.ejbObjectProxy = ejbObjectProxy;
        this.handler = ejbObjectProxy.getEJBObjectHandler();
    }

    /**
     * Obtain the EJB object reference represented by this handle.
     *
     * @exception RemoteException The EJB object could not be obtained
     *    because of a system-level failure.
     */
    public EJBObject getEJBObject() throws RemoteException {
        return ejbObjectProxy;
    }

    //========================================
    // Externalizable object implementation
    //
    public void writeExternal(ObjectOutput out) throws IOException{

        // Write the full proxy data
        out.writeObject( handler.client );

        EJBMetaDataImpl ejb = handler.ejb;
        out.writeObject( ejb.homeClass );
        out.writeObject( ejb.remoteClass );
        out.writeObject( ejb.keyClass );
        out.writeByte(   ejb.type );
        out.writeUTF(    ejb.deploymentID );
        out.writeShort(  ejb.deploymentCode );
        out.writeObject( handler.server );
        out.writeObject( handler.primaryKey );
    }

    /**
     * Reads the instanceHandle from the stream
     *
     * @param in
     * @exception IOException
     */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException{
        ClientMetaData client = null;
        EJBMetaDataImpl   ejb = new EJBMetaDataImpl();
        ServerMetaData server = null;

        client = (ClientMetaData)in.readObject();

        ejb.homeClass      = (Class) in.readObject();
        ejb.remoteClass    = (Class) in.readObject();
        ejb.keyClass       = (Class) in.readObject();
        ejb.type           = in.readByte();
        ejb.deploymentID   = in.readUTF();
        ejb.deploymentCode = in.readShort();

        server = (ServerMetaData)in.readObject();
        Object primaryKey  = in.readObject();

        handler = EJBObjectHandler.createEJBObjectHandler(ejb, server, client, primaryKey);
        ejbObjectProxy = handler.createEJBObjectProxy();
    }

}