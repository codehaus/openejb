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
 * Copyright 2004-2005 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.corba;

import java.util.HashMap;
import java.util.Map;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.PortableServer.POA;

import org.openejb.EJBComponentType;
import org.openejb.EJBContainer;
import org.openejb.corba.util.TieLoader;


/**
 * @version $Revision$ $Date$
 */
public final class AdapterWrapper {
    private final static Map adapters = new HashMap();
    private final EJBContainer container;
    private Adapter generator;

    public AdapterWrapper(EJBContainer container) {
        this.container = container;

    }

    public EJBContainer getContainer() {
        return container;
    }

    public void start(ORB orb, POA poa, NamingContextExt initialContext, TieLoader tieLoader) throws CORBAException {
        switch (container.getProxyInfo().getComponentType()) {
            case EJBComponentType.STATELESS:
                generator = new AdapterStateless(container, orb, poa, tieLoader);
                break;
            case EJBComponentType.STATEFUL:
                generator = new AdapterStateful(container, orb, poa, tieLoader);
                break;
            case EJBComponentType.BMP_ENTITY:
            case EJBComponentType.CMP_ENTITY:
                generator = new AdapterEntity(container, orb, poa, tieLoader);
                break;
            default:
                throw new CORBAException("CORBA Adapter does not handle MDB containers");
        }
        adapters.put(container.getContainerID(), generator);
    }

    public void stop() throws CORBAException {
        generator.stop();
        adapters.remove(container.getContainerID());
    }

    public static RefGenerator getRefGenerator(String containerId) {
        return (RefGenerator) adapters.get(containerId);
    }
}
