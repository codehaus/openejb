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
package org.openejb.ui.jedi.openejb11.server;

import javax.swing.ImageIcon;
import org.opentools.deployer.plugins.*;
import org.opentools.deployer.plugins.j2ee12.ejb11.EJB11Plugin;

/**
 * The main Plugin for OpenEJB server configuration.  This class provides the
 * name to display on tabs holding OpenEJB information, the XML file name to
 * save and load data from, toolbar icons, access to metadata, and the entries
 * and categories to load into the tree.
 *
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 * @version 1.0
 */
public class ServerPlugin extends Plugin {
    public MetaData getMetaDataInstance() {
        return new OpenEjbMetaData();
    }

    public String getName() {
        return "OpenEJB 1.1 Server";
    }

    public String getXMLFileName() {
        return "openejb-config.xml";
    }

    public ClassDescriptor[] getInterestingClasses() {
        // Don't open a JAR, so we don't care about classes
        return new ClassDescriptor[0];
    }
    public FileDescriptor[] getInterestingFiles() {
        // Don't open a JAR, so we don't care about files
        return new FileDescriptor[0];
    }
    public Category[] getCategories(MetaData data) {
        return new Category[] {
            new CategoryContainer(this, (OpenEjbMetaData)data),
            new CategoryServer(this, (OpenEjbMetaData)data),
            new CategoryTransaction(this, (OpenEjbMetaData)data),
            new CategorySecurity(this, (OpenEjbMetaData)data),
            new CategoryCM(this, (OpenEjbMetaData)data),
            new CategoryConnector(this, (OpenEjbMetaData)data)
        };
    }

    public Plugin[] getDependencies() {
        return new Plugin[] {
            new EJB11Plugin()
        };
    }

    public ToolBarCommand[] getToolBarCommands(Category[] cats) {
        return new ToolBarCommand[] {
            new ToolBarCommand(new ImageIcon(getClass().getClassLoader().getResource("images/container-yellow-text.jpg")),
                               "Create Container", PluginUtils.getCategory("Containers", cats)),
        };
    }

    public Entry getMainConfigEntry(MetaData data) {
        return null;
    }
}
