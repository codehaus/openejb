/**
 *
 * Copyright 2005 The Apache Software Foundation
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
package org.openejb.test;

import org.openejb.client.RemoteInitialContextFactory;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

/**
 * @version $Revision$ $Date$
 */
public class TomcatRemoteTestServer implements TestServer {
    private Properties properties;
    private String servletUrl;
    private File tomcatHome;

    private boolean serverHasAlreadyBeenStarted = true;

    public void init(Properties props) {
        properties = props;
        servletUrl = System.getProperty("remote.serlvet.url", "http://127.0.0.1:8080/openejb/remote");
//        props.put("test.server.class", TomcatRemoteTestServer.class.getName());
        props.put("java.naming.factory.initial", RemoteInitialContextFactory.class.getName());
        props.put("java.naming.provider.url", servletUrl);

        String homeProperty = System.getProperty("tomcat.home");
        if (homeProperty == null){
            throw new IllegalStateException("The system property tomcat.home must be defined.");
        }

        tomcatHome = new File(homeProperty);

        if (!tomcatHome.exists()) {
            throw new IllegalStateException("The tomcat.home directory does not exist: "+tomcatHome.getAbsolutePath());
        }
    }

    public void start() {
        if (connect()){
            return;
        }

        try{
            System.out.println("[] START TOMCAT SERVER");
            System.out.println("CATALINA_HOME = "+tomcatHome.getAbsolutePath());

            String systemInfo = "Java " + System.getProperty("java.version") + "; " + System.getProperty("os.name") + "/" + System.getProperty("os.version");
            System.out.println("SYSTEM_INFO   = "+systemInfo);

            serverHasAlreadyBeenStarted = false;

            FilePathBuilder tomcat = new FilePathBuilder(tomcatHome);
            OutputStream catalinaOut = new FileOutputStream(tomcat.l("logs").f("catalina.out"));

            execBootstrap("start", catalinaOut);
        } catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("Cannot start the server: "+e.getClass().getName()+": "+e.getMessage(), e);
        }
        connect(10);
        // Wait a wee bit longer for good measure
        try {
            Thread.sleep(5000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop(){
        if ( !serverHasAlreadyBeenStarted ) {
            try{
                System.out.println("[] STOP TOMCAT SERVER");
                execBootstrap("stop", System.out);

                disconnect(10);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void execBootstrap(String command, OutputStream catalinaOut) throws IOException {
        String[] bootstrapCommand = getBootstrapCommand(tomcatHome, command);
        Process server = Runtime.getRuntime().exec(bootstrapCommand);

        // Pipe the processes STDOUT to ours
        InputStream out = server.getInputStream();
        Thread serverOut = new Thread(new Pipe(out, catalinaOut));

        serverOut.setDaemon(true);
        serverOut.start();

        // Pipe the processes STDERR to ours
        InputStream err = server.getErrorStream();
        Thread serverErr = new Thread(new Pipe(err, System.err));

        serverErr.setDaemon(true);
        serverErr.start();
    }

    public Properties getContextEnvironment() {
        return (Properties) properties.clone();
    }

    private boolean disconnect(int tries) {
        if (connect()){
            tries--;
            if (tries < 1) {
                return false;
            } else {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                }
                disconnect(tries);
            }
        }

        return true;
    }

    private boolean connect() {
        return connect(1);
    }

    private boolean connect(int tries) {
        //System.out.println("CONNECT "+ tries);
        try {
            URL url = new URL(servletUrl);
            url.openStream();
        } catch (Exception e) {
            tries--;
            //System.out.println(e.getMessage());
            if (tries < 1) {
                return false;
            } else {
                try {
                    Thread.sleep(5000);
                } catch (Exception e2) {
                    e.printStackTrace();
                }
                return connect(tries);
            }
        }

        return true;
    }

    private String[] getBootstrapCommand(File tomcatHome, String command) {
        FilePathBuilder tomcat = new FilePathBuilder(tomcatHome);
        FilePathBuilder tomcatBin = tomcat.l("bin");

        String path = tomcatHome.getAbsolutePath();

        if (path.indexOf("tomcat-5.5") != -1) {
            return new String[]{ "java",
                    "-Djava.util.logging.manager=org.apache.juli.ClassLoaderLogManager",
                    "-Djava.util.logging.config.file=" + tomcat.l("conf").l("logging.properties"),
                    "-Djava.endorsed.dirs=" + tomcat.l("common").l("endorsed"),
                    "-classpath", tomcatBin.l("bootstrap.jar") +":" + tomcatBin.l("commons-logging-api.jar"),
                    "-Dcatalina.base=" + tomcat,
                    "-Dcatalina.home=" + tomcat,
                    "-Djava.io.tmpdir=" + tomcat.l("temp"),
                    "org.apache.catalina.startup.Bootstrap", command};
        } else if (path.indexOf("tomcat-5.0") != -1) {
            return new String[]{ "java",
                    "-Djava.endorsed.dirs=" + tomcat.l("common").l("endorsed"),
                    "-classpath", tomcatBin.l("bootstrap.jar") +":" + tomcatBin.l("commons-logging-api.jar"),
                    "-Dcatalina.base=" + tomcat,
                    "-Dcatalina.home=" + tomcat,
                    "-Djava.io.tmpdir=" + tomcat.l("temp"),
                    "org.apache.catalina.startup.Bootstrap", command};
        } else if (path.indexOf("tomcat-4.1") != -1) {
            return new String[]{ "java",
                    "-Djava.endorsed.dirs=" + tomcat.l("common").l("endorsed"),
                    "-classpath", tomcatBin.l("bootstrap.jar") +"",
                    "-Dcatalina.base=" + tomcat,
                    "-Dcatalina.home=" + tomcat,
                    "-Djava.io.tmpdir=" + tomcat.l("temp"),
                    "org.apache.catalina.startup.Bootstrap", command};
        } else {
            throw new IllegalArgumentException("Unsupported Tomcat version: " + tomcatHome.getName());
        }
    }

    public static class FilePathBuilder {
        private final File file;

        public FilePathBuilder(File file) {
            this.file = file;
        }

        public FilePathBuilder l(String name){
            return new FilePathBuilder(f(name));
        }

        public File f(String name){
            return new File(file, name);
        }

        public String toString() {
            return file.getAbsolutePath();
        }
    }

    private static final class Pipe implements Runnable {
        private final InputStream is;
        private final OutputStream out;

        private Pipe(InputStream is, OutputStream out) {
            super();
            this.is = is;
            this.out = out;
        }

        public void run() {
            try{
                int i = is.read();
                out.write( i );

                while ( i != -1 ){
                    i = is.read();
                    out.write( i );
                }

            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }


/**

 5.5.x startup



 5.0.x

 -Djava.endorsed.dirs=/Users/dblevins/work/openejb1/target/jakarta-tomcat-5.0.30/common/endorsed
 -classpath
 /System/Library/Frameworks/JavaVM.framework/Versions/1.4/Home/lib/tools.jar:/Users/dblevins/work/openejb1/target/jakarta-tomcat-5.0.30/bin/bootstrap.jar:/Users/dblevins/work/openejb1/target/jakarta-tomcat-5.0.30/bin/commons-logging-api.jar
 -Dcatalina.base=/Users/dblevins/work/openejb1/target/jakarta-tomcat-5.0.30
 -Dcatalina.home=/Users/dblevins/work/openejb1/target/jakarta-tomcat-5.0.30
 -Djava.io.tmpdir=/Users/dblevins/work/openejb1/target/jakarta-tomcat-5.0.30/temp
 org.apache.catalina.startup.Bootstrap
 start

 4.1.x

 -Djava.endorsed.dirs=/Users/dblevins/work/openejb1/target/jakarta-tomcat-4.1.31/common/endorsed
 -classpath
 /System/Library/Frameworks/JavaVM.framework/Versions/1.4/Home/lib/tools.jar:/Users/dblevins/work/openejb1/target/jakarta-tomcat-4.1.31/bin/bootstrap.jar
 -Dcatalina.base=/Users/dblevins/work/openejb1/target/jakarta-tomcat-4.1.31
 -Dcatalina.home=/Users/dblevins/work/openejb1/target/jakarta-tomcat-4.1.31
 -Djava.io.tmpdir=/Users/dblevins/work/openejb1/target/jakarta-tomcat-4.1.31/temp
 org.apache.catalina.startup.Bootstrap
 start

 */


}
