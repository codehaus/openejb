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
package org.openejb.spi;

import java.util.Properties;

import javax.transaction.TransactionManager;

import org.openejb.OpenEJBException;

public interface Assembler {

    public void init(Properties props)throws OpenEJBException;
    
    public void build()throws OpenEJBException;

    /**
     * Gets the ID of the default container system, or null if none was
     * specified.
     */
    public String getDefaultContainerSystemID();

    /**
     * Gets all of the container systems configured by this assembler.  There
     * may be multiple, for example, if several distinct applications are
     * configured at startup.
     */
    public ContainerSystem[] getContainerSystems();

    /**
     * Gets the <code>TransactionManager</code> that this container manager exposes to the <code>Container</code>s it manages.
     *
     * @return the TransactionManager to be used by this container manager's containers when servicing beans
     * @see "javax.transaction.TransactionManager"
     * @see org.openejb.spi.TransactionService#getTransactionManager() TransactionService.getTransactionManager()
     */
    public TransactionManager getTransactionManager( );

    /**
     * Gets the <code>Deployer</code> that OpenEJB should use to manage ongoing
     * deployment activity.  This may be null if this configuration does not
     * support deployment activity at runtime.
     */
    public Deployer getDeployer();
}