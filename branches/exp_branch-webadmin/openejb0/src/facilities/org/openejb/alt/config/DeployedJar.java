package org.openejb.alt.config;

import java.io.*;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import org.openejb.OpenEJB;
import org.openejb.OpenEJBException;
import org.openejb.alt.assembler.classic.*;
import org.openejb.alt.config.ejb11.*;
import org.openejb.alt.config.sys.*;
import org.openejb.util.FileUtils;

public class DeployedJar {

    EjbJar ejbJar;
    OpenejbJar openejbJar;
    String jarURI;


    public DeployedJar(String jar, EjbJar ejbJar, OpenejbJar openejbJar) {
        this.ejbJar = ejbJar;
        this.openejbJar = openejbJar;
        this.jarURI = jar;
    }
}
