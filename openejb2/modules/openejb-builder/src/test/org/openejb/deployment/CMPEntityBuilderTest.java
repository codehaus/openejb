/* ====================================================================
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce this list of
 *    conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
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
 *    (http://openejb.org/).
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
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the OpenEJB Project.  For more information
 * please see <http://openejb.org/>.
 *
 * ====================================================================
 */

package org.openejb.deployment;

import java.io.File;
import java.net.URI;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContext;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContextImpl;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.xbeans.j2ee.EjbJarDocument;
import org.apache.geronimo.xbeans.j2ee.EjbJarType;
import org.apache.xmlbeans.XmlObject;
import org.openejb.corba.compiler.SkeletonGenerator;
import org.openejb.xbeans.ejbjar.OpenejbOpenejbJarDocument;
import org.openejb.xbeans.ejbjar.OpenejbOpenejbJarType;
import org.tranql.cache.GlobalSchema;
import org.tranql.ejb.EJBSchema;
import org.tranql.schema.Association;
import org.tranql.schema.Association.JoinDefinition;
import org.tranql.schema.AssociationEnd;
import org.tranql.schema.Attribute;
import org.tranql.schema.Entity;
import org.tranql.schema.FKAttribute;
import org.tranql.sql.Table;
import org.tranql.sql.sql92.SQL92Schema;

/**
 *
 *
 * @version $Revision$ $Date$
 *
 * */
public class CMPEntityBuilderTest extends TestCase {
    private SkeletonGenerator skeletonGenerator = null;
    private Repository repository = null;
    private Kernel kernel = null;

    private static final File basedir = new File(System.getProperty("basedir", System.getProperty("user.dir")));

    protected void setUp() throws Exception {
        kernel = KernelHelper.getPreparedKernel();
    }

    protected void tearDown() throws Exception {
        kernel.shutdown();
    }

    public void testOneToOne() throws Exception {
        executeOneToOne("src/test-cmp/onetoone/simplepk/ejb-jar.xml", "src/test-cmp/onetoone/simplepk/openejb-jar.xml");
    }

    public void testOneToOneUnidirectional() throws Exception {
        executeOneToOne("src/test-cmp/onetoone/simplepk/ejb-jar.xml", "src/test-cmp/onetoone/simplepk/unidirectional-openejb-jar.xml");
    }
    
    private void executeOneToOne(String ejbJarFileName, String openejbJarFileName) throws Exception {
        File ejbJarFile = new File(basedir, ejbJarFileName);
        File openejbJarFile = new File(basedir, openejbJarFileName);
        EjbJarType ejbJarType = ((EjbJarDocument) XmlObject.Factory.parse(ejbJarFile)).getEjbJar();
        OpenejbOpenejbJarType openejbJarType = ((OpenejbOpenejbJarDocument) XmlObject.Factory.parse(openejbJarFile)).getOpenejbJar();
        
        OpenEJBModuleBuilder moduleBuilder = new OpenEJBModuleBuilder(KernelHelper.DEFAULT_PARENTID, skeletonGenerator, repository);
        CMPEntityBuilder builder = new CMPEntityBuilder(moduleBuilder);
        
        GlobalSchema globalSchema = new GlobalSchema("Test");
        EJBSchema ejbSchema = new EJBSchema("Test");
        SQL92Schema sqlSchema = new SQL92Schema("Test", null);
        
        File tempDir = DeploymentUtil.createTempDir();
        try {
            EARContext earContext = new EARContext(tempDir,
                    new URI("test"),
                    ConfigurationModuleType.EJB,
                    KernelHelper.DEFAULT_PARENTID,
                    kernel,
                    "null",
                    null,
                    null,
                    null,
                    null,
                    null);
            J2eeContext moduleJ2eeContext = new J2eeContextImpl("geronimo.server", "TestGeronimoServer", "null", "MockModule", null, null);
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            builder.buildCMPSchema(earContext, moduleJ2eeContext, ejbJarType, openejbJarType, cl, ejbSchema, sqlSchema, globalSchema);

            assertOneToOneEntity(ejbSchema.getEntity("A"), ejbSchema.getEntity("B"));
            assertOneToOneEntity(sqlSchema.getEntity("A"), sqlSchema.getEntity("B"));
        } finally {
            DeploymentUtil.recursiveDelete(tempDir);
        }
    }

    public void testOneToMany() throws Exception {
        executeOneToMany("src/test-cmp/onetomany/simplepk/ejb-jar.xml", "src/test-cmp/onetomany/simplepk/openejb-jar.xml");
    }
    
    public void testOneToManyUnidirectional() throws Exception {
        executeOneToMany("src/test-cmp/onetomany/simplepk/ejb-jar.xml", "src/test-cmp/onetomany/simplepk/unidirectional-openejb-jar.xml");
    }
    
    public void executeOneToMany(String ejbJarFileName, String openejbJarFileName) throws Exception {
        File ejbJarFile = new File(basedir, ejbJarFileName);
        File openejbJarFile = new File(basedir, openejbJarFileName);
        EjbJarType ejbJarType = ((EjbJarDocument) XmlObject.Factory.parse(ejbJarFile)).getEjbJar();
        OpenejbOpenejbJarType openejbJarType = ((OpenejbOpenejbJarDocument) XmlObject.Factory.parse(openejbJarFile)).getOpenejbJar();
        
        OpenEJBModuleBuilder moduleBuilder = new OpenEJBModuleBuilder(KernelHelper.DEFAULT_PARENTID, skeletonGenerator, repository);
        CMPEntityBuilder builder = new CMPEntityBuilder(moduleBuilder);
        
        GlobalSchema globalSchema = new GlobalSchema("Test");
        EJBSchema ejbSchema = new EJBSchema("Test");
        SQL92Schema sqlSchema = new SQL92Schema("Test", null);

        File tempDir = DeploymentUtil.createTempDir();
        try {
            EARContext earContext = new EARContext(tempDir,
                    new URI("test"),
                    ConfigurationModuleType.EJB,
                    KernelHelper.DEFAULT_PARENTID,
                    kernel,
                    "null",
                    null,
                    null,
                    null,
                    null,
                    null);

            J2eeContext moduleJ2eeContext = new J2eeContextImpl("geronimo.server", "TestGeronimoServer", "null", "MockModule", null, null);
             ClassLoader cl = Thread.currentThread().getContextClassLoader();
            builder.buildCMPSchema(earContext, moduleJ2eeContext, ejbJarType, openejbJarType, cl, ejbSchema, sqlSchema, globalSchema);

            assertOneToManyEntity(ejbSchema.getEntity("A"), ejbSchema.getEntity("B"));
            assertOneToManyEntity(sqlSchema.getEntity("A"), sqlSchema.getEntity("B"));
        } finally {
            DeploymentUtil.recursiveDelete(tempDir);
        }
    }

    public void testManyToMany() throws Exception {
        executeManyToMany("src/test-cmp/manytomany/simplepk/ejb-jar.xml", "src/test-cmp/manytomany/simplepk/openejb-jar.xml");
    }
    
    public void testManyToManyUnidirectional() throws Exception {
        executeManyToMany("src/test-cmp/manytomany/simplepk/ejb-jar.xml", "src/test-cmp/manytomany/simplepk/unidirectional-openejb-jar.xml");
    }

    public void executeManyToMany(String ejbJarFileName, String openejbJarFileName) throws Exception {
        File ejbJarFile = new File(basedir, ejbJarFileName);
        File openejbJarFile = new File(basedir, openejbJarFileName);
        EjbJarType ejbJarType = ((EjbJarDocument) XmlObject.Factory.parse(ejbJarFile)).getEjbJar();
        OpenejbOpenejbJarType openejbJarType = ((OpenejbOpenejbJarDocument) XmlObject.Factory.parse(openejbJarFile)).getOpenejbJar();
        
        OpenEJBModuleBuilder moduleBuilder = new OpenEJBModuleBuilder(KernelHelper.DEFAULT_PARENTID, skeletonGenerator, repository);
        CMPEntityBuilder builder = new CMPEntityBuilder(moduleBuilder);
        
        GlobalSchema globalSchema = new GlobalSchema("Test");
        EJBSchema ejbSchema = new EJBSchema("Test");
        SQL92Schema sqlSchema = new SQL92Schema("Test", null);
        
        File tempDir = DeploymentUtil.createTempDir();
        try {
            EARContext earContext = new EARContext(tempDir,
                    new URI("test"),
                    ConfigurationModuleType.EJB,
                    KernelHelper.DEFAULT_PARENTID,
                    kernel,
                    "null",
                    null,
                    null,
                    null,
                    null,
                    null);

            J2eeContext moduleJ2eeContext = new J2eeContextImpl("geronimo.server", "TestGeronimoServer", "null", "MockModule", null, null);
             ClassLoader cl = Thread.currentThread().getContextClassLoader();
            builder.buildCMPSchema(earContext, moduleJ2eeContext, ejbJarType, openejbJarType, cl, ejbSchema, sqlSchema, globalSchema);

            assertManyToManyEntity(ejbSchema.getEntity("A"), ejbSchema.getEntity("B"));
            assertManyToManyEntity(sqlSchema.getEntity("A"), sqlSchema.getEntity("B"));
        } finally {
            DeploymentUtil.recursiveDelete(tempDir);
        }
    }

    private void assertEntity(Entity leftEntity, Entity rightEntity) {
        List attributes = leftEntity.getAttributes();
        assertEquals(2, attributes.size());
        for (Iterator iter = attributes.iterator(); iter.hasNext();) {
            Attribute att = (Attribute) iter.next();
            if ( leftEntity instanceof Table ) {
                if ( att.getName().equals("field1") ) {
                    assertTrue(att.getPhysicalName().equals("a1"));
                    assertTrue(att.isIdentity());
                } else if ( att.getName().equals("field2") ) {
                    assertTrue(att.getPhysicalName().equals("a2"));
                } else {
                    fail("Unknow field.");
                }
            } else {
                if ( att.getName().equals("field1") ) {
                    assertTrue(att.getPhysicalName().equals("field1"));
                    assertTrue(att.isIdentity());
                } else if ( att.getName().equals("field2") ) {
                    assertTrue(att.getPhysicalName().equals("field2"));
                } else {
                    fail("Unknow field.");
                }
            }
        }
        attributes = rightEntity.getAttributes();
        assertEquals(2, attributes.size());
        for (Iterator iter = attributes.iterator(); iter.hasNext();) {
            Attribute att = (Attribute) iter.next();
            if ( rightEntity instanceof Table ) {
                if ( att.getName().equals("field1") ) {
                    assertTrue(att.getPhysicalName().equals("b1"));
                    assertTrue(att.isIdentity());
                } else if ( att.getName().equals("field2") ) {
                    assertTrue(att.getPhysicalName().equals("b2"));
                } else {
                    fail("Unknow field.");
                }
            } else {
                if ( att.getName().equals("field1") ) {
                    assertTrue(att.getPhysicalName().equals("field1"));
                    assertTrue(att.isIdentity());
                } else if ( att.getName().equals("field2") ) {
                    assertTrue(att.getPhysicalName().equals("field2"));
                } else {
                    fail("Unknow field.");
                }
            }
        }

        List pkEnds = leftEntity.getAssociationEnds();
        List fkEnds = rightEntity.getAssociationEnds();
        assertEquals(1, pkEnds.size());
        assertEquals(1, fkEnds.size());
        AssociationEnd pkEnd = (AssociationEnd) pkEnds.get(0);
        AssociationEnd fkEnd = (AssociationEnd) fkEnds.get(0);
        assertEquals("b", pkEnd.getName());
        assertEquals("a", fkEnd.getName());
        assertEquals(rightEntity, pkEnd.getEntity());
        assertEquals(leftEntity, fkEnd.getEntity());
        
        Association association = pkEnd.getAssociation();
        assertTrue(association == fkEnd.getAssociation());
    }

    private void assertOneToOneEntity(Entity pkEntity, Entity fkEntity) {
        assertEntity(pkEntity, fkEntity);
        
        List pkEnds = pkEntity.getAssociationEnds();
        List fkEnds = fkEntity.getAssociationEnds();
        AssociationEnd pkEnd = (AssociationEnd) pkEnds.get(0);
        assertTrue(pkEnd.isSingle());
        AssociationEnd fkEnd = (AssociationEnd) fkEnds.get(0);
        assertTrue(fkEnd.isSingle());
        
        Association association = pkEnd.getAssociation();
        assertTrue(association.isOneToOne());
        JoinDefinition joinDefinition = association.getJoinDefinition();
        assertEquals(pkEntity, joinDefinition.getPKEntity());
        assertEquals(fkEntity, joinDefinition.getFKEntity());
        LinkedHashMap linkedHashMap = joinDefinition.getPKToFKMapping();
        for (Iterator iter = linkedHashMap.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            if ( pkEntity instanceof Table ) {
                assertEquals("a1", ((Attribute) entry.getKey()).getPhysicalName());
            } else {
                assertEquals("field1", ((Attribute) entry.getKey()).getPhysicalName());
            }
            assertEquals("fka1", ((FKAttribute) entry.getValue()).getPhysicalName());
        }
    }

    private void assertOneToManyEntity(Entity pkEntity, Entity fkEntity) {
        assertEntity(pkEntity, fkEntity);
        
        List pkEnds = pkEntity.getAssociationEnds();
        List fkEnds = fkEntity.getAssociationEnds();
        AssociationEnd pkEnd = (AssociationEnd) pkEnds.get(0);
        assertTrue(pkEnd.isMulti());
        AssociationEnd fkEnd = (AssociationEnd) fkEnds.get(0);
        assertTrue(fkEnd.isSingle());
        
        Association association = pkEnd.getAssociation();
        assertTrue(association.isOneToMany(pkEnd));
        assertTrue(association.isManyToOne(fkEnd));
        JoinDefinition joinDefinition = association.getJoinDefinition();
        assertEquals(pkEntity, joinDefinition.getPKEntity());
        assertEquals(fkEntity, joinDefinition.getFKEntity());
        LinkedHashMap linkedHashMap = joinDefinition.getPKToFKMapping();
        for (Iterator iter = linkedHashMap.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            if ( pkEntity instanceof Table ) {
                assertEquals("a1", ((Attribute) entry.getKey()).getPhysicalName());
            } else {
                assertEquals("field1", ((Attribute) entry.getKey()).getPhysicalName());
            }
            assertEquals("fka1", ((FKAttribute) entry.getValue()).getPhysicalName());
        }
    }

    private void assertManyToManyEntity(Entity leftEntity, Entity rightEntity) {
        assertEntity(leftEntity, rightEntity);
        
        List leftEnds = leftEntity.getAssociationEnds();
        List rightEnds = rightEntity.getAssociationEnds();
        AssociationEnd leftEnd = (AssociationEnd) leftEnds.get(0);
        assertTrue(leftEnd.isMulti());
        AssociationEnd rightEnd = (AssociationEnd) rightEnds.get(0);
        assertTrue(rightEnd.isMulti());
        
        Association association = leftEnd.getAssociation();
        Entity mtmEntity = association.getManyToManyEntity();
        assertTrue(association.isManyToMany());
        assertNotNull(mtmEntity);
        assertEquals("MTM", mtmEntity.getName());
        JoinDefinition joinDefinition = association.getLeftJoinDefinition();
        if ( leftEntity == joinDefinition.getPKEntity() ) {
            LinkedHashMap linkedHashMap = joinDefinition.getPKToFKMapping();
            for (Iterator iter = linkedHashMap.entrySet().iterator(); iter.hasNext();) {
                Map.Entry entry = (Map.Entry) iter.next();
                if ( leftEntity instanceof Table ) {
                    assertEquals("a1", ((Attribute) entry.getKey()).getPhysicalName());
                } else {
                    assertEquals("field1", ((Attribute) entry.getKey()).getPhysicalName());
                }
                assertEquals("fka1", ((FKAttribute) entry.getValue()).getPhysicalName());
            }
        } else if ( rightEntity == joinDefinition.getPKEntity() ) {
            LinkedHashMap linkedHashMap = joinDefinition.getPKToFKMapping();
            for (Iterator iter = linkedHashMap.entrySet().iterator(); iter.hasNext();) {
                Map.Entry entry = (Map.Entry) iter.next();
                if ( rightEntity instanceof Table ) {
                    assertEquals("b1", ((Attribute) entry.getKey()).getPhysicalName());
                } else {
                    assertEquals("field1", ((Attribute) entry.getKey()).getPhysicalName());
                }
                assertEquals("fkb1", ((FKAttribute) entry.getValue()).getPhysicalName());
            }
        } else {
            fail("JoinDefinitions are misconfigured.");
        }
    }
}