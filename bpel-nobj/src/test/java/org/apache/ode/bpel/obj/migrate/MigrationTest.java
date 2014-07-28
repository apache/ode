package org.apache.ode.bpel.obj.migrate;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.o.OProcess;
import org.apache.ode.bpel.o.Serializer;
import org.junit.Test;

public class MigrationTest {
    private static final Log __log = LogFactory.getLog(MigrationTest.class);
	
	public void runTest(File file) throws Exception{
		OProcess old = new Serializer(new FileInputStream(file)).readOProcess();
		
//		OProcess old2 = new Serializer(new FileInputStream(file)).readOProcess();
//		TraverseObject traverse = new TraverseObject();
//		EqualityVisitor visitor = new EqualityVisitor(old);
//		traverse.accept(visitor);
//		assertEquals(Boolean.TRUE, traverse.traverseObject(old2));
		
		OmOld2new mig = new OmOld2new();
		org.apache.ode.bpel.obj.OProcess migrated = (org.apache.ode.bpel.obj.OProcess)mig.migrateFrom(old);
	}
	
	@Test
	public void testMigrations() throws Exception{
		String oldPath = "/oldcbp";
		List<File> cbps = findAllCbps(oldPath);
		for (File path : cbps){
			__log.debug("Migrating file " + path);
			runTest(path);
		}
	}

	private List<File> findAllCbps(String oldPath) {
		File seed = new File(this.getClass().getResource(oldPath).getFile());
		List<File> cbps = new ArrayList<File>();
		List<File> dirs = new LinkedList<File>();
		dirs.add(seed);
		while(!dirs.isEmpty()){
			File cur = dirs.remove(0);
			File[] ds = cur.listFiles(new FileFilter() {
				
				@Override
				public boolean accept(File file) {
					return file.isDirectory();
				}
			});
			for (File d : ds){
				dirs.add(d);
			}
			
			ds = cur.listFiles(new FilenameFilter() {
				
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".cbp");
				}
			});
			for (File f : ds){
				cbps.add(f);
			}
		}
		return cbps;
	}
}
