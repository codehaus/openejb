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
 *    please contact dev@openejb.org.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://www.openejb.org/).
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
 * $Id: DeployerObject.java,v 1.3 2005/06/19 22:40:34 jlaskowski Exp $
 */
package org.openejb.webadmin.main;

import java.rmi.RemoteException;

import javax.ejb.EJBObject;

import org.openejb.OpenEJBException;

/**
 * This is a stateful session bean which holds deployment information
 * for the web deployment of an EJB. 
 *
 * @see org.openejb.webadmin.main.DeployerBean
 * @author  <a href="mailto:tim_urberg@yahoo.com">Tim Urberg</a>
 */
public interface DeployerObject extends EJBObject {
	//action methods
	public void setBooleanValues(boolean[] booleanValues) throws RemoteException;
	public boolean[] getBooleanValues() throws RemoteException;
	public void setJarFile(String jarFile) throws RemoteException;
	public String getJarFile() throws RemoteException;
	public void startDeployment() throws RemoteException, OpenEJBException;
	public void finishDeployment() throws RemoteException, OpenEJBException;
	public String getDeploymentHTML() throws RemoteException;
	public DeployData[] getDeployDataArray() throws RemoteException;
	public String createIdTable() throws RemoteException, OpenEJBException;
	public void setDeployAndContainerIds(DeployData[] deployDataArray)
		throws RemoteException, OpenEJBException;
}