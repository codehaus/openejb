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
 * Copyright 2003 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Helper class to dynamically create the full classpath and launch an OpenEJB application
 * 
 * TODO Introduce aliases so that 'org.openejb.EjbServer' would become 'server' TODO Introduce boolean variables to
 * indicate whether stdout/stdin/stderr shall be grabbed
 * 
 * More insight at http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps_p.html
 * 
 * @version $Revision$ $Date$
 */
public final class Launcher {

    public static void main(String[] args) {
        
        if (args == null || args.length == 0) {
            printHelp();
            System.exit(-1);
        }
        
        StringBuffer classpath = new StringBuffer();
        
        FileUtils home = FileUtils.getHome();
        
        //TODO make it configurable what directories to include in classpath
        try {
            File dir = home.getDirectory("lib");
            appendJarsToClasspath(classpath, listDirectory(dir));
        } catch (IOException e) {
            System.out.println("[ERROR] Unable to add jars from 'lib' directory to classpath.");
        }


        // TODO shall it be prepended or appended? - make it configurable
        classpath.append(System.getProperty("java.class.path"));
        
        ArrayList cmd = new ArrayList();
        cmd.add("java");
        cmd.add("-classpath");
        cmd.add(classpath.toString());
        cmd.addAll(getVmParameters());
        boolean wait = true;
        
        for (int i = 0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("-nowait")){
                wait = false;
            } else {
                cmd.add(args[i]);
            }
        }
        
        Process process = null;
        try {
            Runtime rt = Runtime.getRuntime();
//            System.err.println();
//            System.err.println();
//            System.err.println();
//            System.err.println();
//            for (Iterator iterator = cmd.iterator(); iterator.hasNext();) {
//                String s = (String) iterator.next();
//                System.err.println(s);
//            }
//            System.err.println();
//            System.err.println();
//            System.err.println();
//            System.err.println();
            process = rt.exec((String[])cmd.toArray(new String[0]));

            StreamGrabber stderr = new StreamGrabber(process.getErrorStream(), System.err);
            StreamGrabber stdout = new StreamGrabber(process.getInputStream(), System.out);

            stderr.setName("Subprocess-STDERR");
            stdout.setName("Subprocess-STDOUT");
            
            stderr.setDaemon(true);
            stderr.start();
            stdout.setDaemon(true);
            stdout.start();

            // It seems not to work on Cygwin when the class is invoked directly
            // Once it'd been invoked from the shell script, it has started to work well 
            // Has anyone any clue about a possible solution?
            rt.addShutdownHook(new KillProcessShutdownHook(process));

            if (wait) process.waitFor();
        } catch (Throwable t) {
            throw new RuntimeException("Could not start the executable: " + args[0], t);
        } finally {
            if (wait && process != null) {
                try {
                    process.getErrorStream().close();
                    process.getOutputStream().close();
                    process.getInputStream().close();
                } catch (IOException ioe) {
                    //ignore it
                }
            }
        }
    }

    public static ArrayList getVmParameters(){
        ArrayList options = new ArrayList();
        
        Set set = System.getProperties().entrySet();
        for (Iterator iter = set.iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            if (entry.getKey().toString().startsWith("openejb")){
                options.add("-D"+entry.getKey()+"="+entry.getValue());
            }
        }
        return options;
    }
    /**
	 * List directory
	 * 
	 * @param dir
	 * @return list of jars
	 */
    private static File[] listDirectory(File dir) {
        return dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        });
    }

    /**
	 * Append jars to classpath
     * 
     * Leaves a path separator char (; or :) on the end
     * of the StringBuffer so that this method can be 
     * called several times in a row without creating
     * a bad path. 
	 */
    private static void appendJarsToClasspath(StringBuffer classpath, File[] jars) {
        for (int i = 0; i < jars.length; i++) {
            classpath.append(jars[i].getAbsolutePath());
            classpath.append(File.pathSeparator);
        }
    }

    public final static void printHelp() {
        System.err.println("No class specified");
        System.err.println("exiting...");
    }

    private static final class StreamGrabber extends Thread {
        InputStream is;
        OutputStream os;

        public StreamGrabber(InputStream is, OutputStream os) {
            this.is = is;
            this.os = os;
        }

        public void run() {
            //BufferedReader in = new BufferedReader(new InputStreamReader(is));
            BufferedInputStream in = new BufferedInputStream(is);
            try {
                int i;
                while ((i = in.read()) != -1) {
                    os.write(i);
                }
                os.flush();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    /**
	 * Kill subprocess as Launcher finishes
	 */
    private static final class KillProcessShutdownHook extends Thread {
        Process process;

        public KillProcessShutdownHook(Process process) {
            this.process = process;
        }

        public void run() {
            process.destroy();
        }
    }
}
