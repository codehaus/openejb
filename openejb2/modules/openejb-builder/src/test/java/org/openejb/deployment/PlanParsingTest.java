package org.openejb.deployment;

import java.io.File;

import junit.framework.TestCase;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.SingleElementCollection;
import org.openejb.xbeans.ejbjar.OpenejbOpenejbJarType;

/**
 */
public class PlanParsingTest extends TestCase {
    private OpenEjbModuleBuilder builder;
    File basedir = new File(System.getProperty("basedir", "."));

    protected void setUp() throws Exception {
        super.setUp();
        builder = new OpenEjbModuleBuilder(null, 
                null,
                null,
                null,
                null,
                null,
                null,
                (GBeanData) null,
                (SingleElementCollection)null,
                null);
    }

    public void testResourceRef() throws Exception {
        File resourcePlan = new File(basedir, "src/test-resources/plans/plan1.xml");
        assertTrue(resourcePlan.exists());
        OpenejbOpenejbJarType openejbJar = builder.getOpenejbJar(resourcePlan, null, true, null, null);
        assertEquals(1, openejbJar.getEnterpriseBeans().getSessionArray()[0].getResourceRefArray().length);
        System.out.println(openejbJar.toString());
    }

}
