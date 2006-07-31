/**
 *
 * Copyright 2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.openejb.test.security.sfsb;

import java.rmi.RemoteException;
import javax.ejb.EJBObject;


/**
 * @version $Revision$ $Date$
 */
public interface BasicStateful extends EJBObject {

    public String noAccessMethod(String argument1) throws RemoteException;

    public String noAccessMethod(String argument1, String argument2) throws RemoteException;

    public String highSecurityMethod(String argument1) throws RemoteException;

    public String highSecurityMethod(String argument1, String argument2) throws RemoteException;

    public String mediumSecurityMethod(String argument1) throws RemoteException;

    public String mediumSecurityMethod(String argument1, String argument2) throws RemoteException;

    public String lowSecurityMethod(String argument1) throws RemoteException;

    public String lowSecurityMethod(String argument1, String argument2) throws RemoteException;

    public String allAccessMethod(String argument1) throws RemoteException;

    public String allAccessMethod(String argument1, String argument2) throws RemoteException;

    public String unassignedMethod(String argument1) throws RemoteException;

    public String unassignedMethod(String argument1, String argument2) throws RemoteException;

    public boolean isInRole(String roleName) throws RemoteException;

}
