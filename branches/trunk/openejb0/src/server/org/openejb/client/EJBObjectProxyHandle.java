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
import java.io.ObjectStreamException;


public class EJBObjectProxyHandle implements Externalizable{

    EJBObjectHandler handler;

    public EJBObjectProxyHandle(){
    }

    public EJBObjectProxyHandle(EJBObjectHandler handler){
        this.handler = handler;
    }

    /**
     * Writes the instanceHandle to the stream.
     *
     * @param out
     * @exception IOException
     */
    public void writeExternal(ObjectOutput out) throws IOException{
        // Write the full proxy data
        handler.client.writeExternal( out );
        
        EJBMetaDataImpl ejb = handler.ejb;
        out.writeObject( ejb.homeClass );
        out.writeObject( ejb.remoteClass );
        out.writeObject( ejb.keyClass );
        out.writeByte(   ejb.type );
        out.writeUTF(    ejb.deploymentID );
        out.writeShort(  ejb.deploymentCode );
        handler.server.writeExternal( out );
        out.writeObject( handler.primaryKey );
    }

    /**
     * Reads the instanceHandle from the stream
     *
     * @param in
     * @exception IOException
     */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException{
        ClientMetaData client = new ClientMetaData();
        EJBMetaDataImpl   ejb = new EJBMetaDataImpl();
        ServerMetaData server = new ServerMetaData();        

        client.readExternal( in );

        ejb.homeClass      = (Class) in.readObject();
        ejb.remoteClass    = (Class) in.readObject();
        ejb.keyClass       = (Class) in.readObject();
        ejb.type           = in.readByte();
        ejb.deploymentID   = in.readUTF();
        ejb.deploymentCode = in.readShort();

        server.readExternal( in );
        Object primaryKey  = in.readObject();

        handler = EJBObjectHandler.createEJBObjectHandler(ejb, server, client, primaryKey);

    }


    /**
     *
     * @return
     * @exception ObjectStreamException
     */
    private Object readResolve() throws ObjectStreamException{
        return handler.createEJBObjectProxy();
    }

}
