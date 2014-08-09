package org.apache.ode.bpel.compiler_2_0;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.o.Serializer;
import org.apache.ode.bpel.obj.OProcess;
import org.apache.ode.bpel.obj.migrate.DeepEqualityHelper;
import org.apache.ode.bpel.obj.migrate.DomElementComparator;
import org.apache.ode.bpel.obj.migrate.ExtensibeImplEqualityComp;
import org.apache.ode.bpel.obj.migrate.ObjectTraverser;
import org.apache.ode.bpel.obj.migrate.OmOld2new;
import org.apache.ode.bpel.obj.migrate.OmUpgradeVisitor;
import org.apache.ode.bpel.obj.migrate.UpgradeChecker;
import org.junit.Assert;
import org.junit.Test;

public class MigrationTest extends GoodCompileTest{
    private static final Log __log = LogFactory.getLog(MigrationTest.class);

    /**
     * compare compiled OProcess with migrated ones.
     */
    public void runTest(String bpel) throws Exception {
        try {
            Class testClass = getClass();
            URL url = testClass.getResource(bpel);
            URI uri = url.toURI();
            String path = uri.getPath();
            File bpelFile = new File(path);
            String cbpPath = bpel.substring(0, bpel.lastIndexOf(".")) + ".cbp";
            String oldCbpPath = "/oldcbp" + cbpPath;
            File oldCbpFile = new File(testClass.getResource(oldCbpPath).getFile());
            
            OProcess nu = _compiler.compile2OProcess(bpelFile, 0);
    		__log.debug("compiled new OProcess " + nu.getFieldContainer());
    		org.apache.ode.bpel.o.OProcess old = new Serializer(new FileInputStream(oldCbpFile)).readOProcess();
    		OmOld2new mig = new OmOld2new();
    		ObjectTraverser mtraverse = new ObjectTraverser();
    		mtraverse.accept(mig);
    		OProcess migrated = (OProcess) mtraverse.traverseObject(old);
    		__log.debug("migrated new OProcess " + migrated.getFieldContainer());
    		
       		//upgrade
			OmUpgradeVisitor upgrader = new OmUpgradeVisitor();
			ObjectTraverser traverser = new ObjectTraverser();
			traverser.accept(upgrader);
			traverser.traverseObject(migrated);

			//check
    		DeepEqualityHelper de = new DeepEqualityHelper();
    		de.addCustomComparator(new ExtensibeImplEqualityComp());
    		de.addCustomComparator(new DomElementComparator());
    		boolean res = de.deepEquals(nu, migrated);
       		assertEquals(Boolean.TRUE, res);
       		
       		UpgradeChecker checker = new UpgradeChecker();
    		traverser = new ObjectTraverser();
    		traverser.accept(checker);
    		traverser.traverseObject(migrated);
      		assertEquals(true, checker.isNewest());
     } catch (Exception ex) {
            ex.printStackTrace();
            Assert.fail("Compilation or migration did not succeed.");
        }
    }
    
    
	@Test
	public void testCompensation1() throws Exception {
    	//skip this test for no corresponding cbp from 1.3.x
//		runTest("/2.0/good/compensation/comp1-2.0.bpel");
	}
  
    @Test
	public void testMultipleEmbeddedSchemas() throws Exception {
    	//skip this test for no corresponding cbp from 1.3.x
//		runTest("/org/apache/ode/bpel/compiler/MultipleEmbeddedSchemas.bpel");
	}
}
