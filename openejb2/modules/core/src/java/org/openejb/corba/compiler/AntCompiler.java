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
 * Copyright 2004 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.corba.compiler;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.Set;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.util.FileUtils;


/**
 * @version $Revision$ $Date$
 */
public class AntCompiler implements Compiler {

    public void compileDirectory(File srcDirectory, File destDirectory, Set classpaths) throws CompilerException {
        Project project = new Project();

        Path path = new Path(project);
        path.setLocation(srcDirectory);

        Javac javac = new Javac();
        javac.setProject(project);
        javac.setSrcdir(path);
        javac.setDestdir(destDirectory);
        javac.setFork(false);
        javac.setDebug(true);

        FileUtils utils = FileUtils.newFileUtils();
        Path classPath = new Path(project);

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        addPathsFromClassLoader(project, classPath, utils, cl);
        
        for (Iterator iter = classpaths.iterator(); iter.hasNext();) {
            URL url = (URL) iter.next();
            // We only can add file based paths.
            if( url.getProtocol().equals("file") ) {
                Path p = new Path(project);
                p.setLocation(utils.normalize(url.getPath()));
                classPath.addExisting(p);
            }
        }
        javac.setClasspath(classPath);

        javac.execute();
    }

    private void addPathsFromClassLoader(Project project, Path classPath, FileUtils utils, ClassLoader cl) {
        if( cl == null ) {
            classPath.addJavaRuntime();
        } else {
            // Add the parent first.
            addPathsFromClassLoader(project, classPath, utils, cl.getParent());
            // We can only add paths if it is a URLClassLoader
            if( cl instanceof URLClassLoader ) {
                URLClassLoader ucl = (URLClassLoader)cl;
                URL[] urls = ucl.getURLs();
                for (int i = 0; i < urls.length; i++) {
                    URL url = urls[i];
                    // We only can add file based paths.
                    if( url.getProtocol().equals("file") ) {
                        Path p = new Path(project);
                        p.setLocation(utils.normalize(url.getPath()));
                        classPath.addExisting(p);               
                    }
                }
            }
        }
        
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(AntCompiler.class, NameFactory.CORBA_SERVICE);

        infoFactory.addInterface(Compiler.class);

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
