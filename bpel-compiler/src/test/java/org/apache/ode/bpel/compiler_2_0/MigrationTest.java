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
import org.apache.ode.bpel.obj.migrate.EqualityVisitor;
import org.apache.ode.bpel.obj.migrate.ExtensibeImplEqualityComp;
import org.apache.ode.bpel.obj.migrate.OmOld2new;
import org.apache.ode.bpel.obj.migrate.TraverseObject;
import org.junit.Assert;

public class MigrationTest extends GoodCompileTest{
    private static final Log __log = LogFactory.getLog(MigrationTest.class);

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
    		OProcess migrated = (OProcess) mig.migrateFrom(old);
    		__log.debug("migrated new OProcess " + migrated.getFieldContainer());
    		TraverseObject traverse = new TraverseObject();
    		EqualityVisitor visitor = new EqualityVisitor(nu);
    		visitor.addCustomComparator(new ExtensibeImplEqualityComp(visitor));
    		traverse.accept(visitor);
    		boolean res = (Boolean)traverse.traverseObject(migrated);
    		System.out.print(visitor.getFalseChain());
    		assertEquals(Boolean.TRUE, res);   		
     } catch (Exception ex) {
            ex.printStackTrace();
            Assert.fail("Compilation did not succeed.");
        }
    }
}
