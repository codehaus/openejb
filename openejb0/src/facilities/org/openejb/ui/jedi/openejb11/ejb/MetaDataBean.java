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
package org.openejb.ui.jedi.openejb11.ejb;

import java.util.List;
import java.util.LinkedList;

/**
 * The metadata for an EJB.  Metadata is loaded by the XMLReader,
 * modified by the Actions, created and destroyed by the Categories,
 * and saved by the XMLWriter.
 *
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 * @version $Revision 1.0 $
 */
public class MetaDataBean {
    private String ejbName;
    private boolean isEntity;
    private String container;
    private List ejbs;
    private List resourceRefs;

    public MetaDataBean() {
        ejbs = new LinkedList();
        resourceRefs = new LinkedList();
    }

    public void setEntity(boolean entity) {isEntity = entity;}
    public boolean isEntity() {return isEntity;}

    public void setEJBName(String name) {ejbName = name;}
    public String getEJBName() {return ejbName;}

    public void setContainerName(String name) {container = name;}
    public String getContainerName() {return container;}

    public void addEjbRef(MetaDataEjbRef ref) {
        ejbs.add(ref);
    }
    public void removeEjbRef(MetaDataEjbRef ref) {
        ejbs.remove(ref);
    }
    public MetaDataEjbRef[] getEjbRefs() {
        return (MetaDataEjbRef[])ejbs.toArray(new MetaDataEjbRef[ejbs.size()]);
    }

    public void addResourceRef(MetaDataResourceRef ref) {
        resourceRefs.add(ref);
    }
    public void removeResourceRef(MetaDataResourceRef ref) {
        resourceRefs.remove(ref);
    }
    public MetaDataResourceRef[] getResourceRefs() {
        return (MetaDataResourceRef[])resourceRefs.toArray(new MetaDataResourceRef[resourceRefs.size()]);
    }
}
