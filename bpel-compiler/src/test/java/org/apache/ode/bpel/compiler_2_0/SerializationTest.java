package org.apache.ode.bpel.compiler_2_0;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.URL;

import org.apache.ode.bpel.obj.OProcess;
import org.apache.ode.bpel.obj.OProcessWrapper;
import org.apache.ode.bpel.obj.migrate.EqualityVisitor;
import org.apache.ode.bpel.obj.migrate.ExtensibeImplEqualityComp;
import org.apache.ode.bpel.obj.migrate.ObjectTraverser;
import org.apache.ode.bpel.obj.serde.DeSerializer;
import org.apache.ode.bpel.obj.serde.OmSerdeFactory;
import org.junit.Assert;

public class SerializationTest extends GoodCompileTest{
	public void runTest(String bpel) throws Exception {
		try {
			Class testClass = getClass();
			URL url = testClass.getResource(bpel);
			URI uri = url.toURI();
			String path = uri.getPath();
			File bpelFile = new File(path);
			OProcess origi = _compiler.compile2OProcess(bpelFile, 0);
			String bpelPath = bpelFile.getAbsolutePath();
			String cbpPath = bpelPath.substring(0, bpelPath.lastIndexOf("."))
					+ ".json.cbp";
			DeSerializer serializer = new DeSerializer();
			serializer.serialize(new FileOutputStream(cbpPath), 
					origi, OmSerdeFactory.SerializeFormat.FORMAT_SERIALIZED_JSON);

			DeSerializer deserializer = new DeSerializer(new FileInputStream(cbpPath));
			OProcess desered = deserializer.deserialize();

			ObjectTraverser traverse = new ObjectTraverser();
			EqualityVisitor visitor = new EqualityVisitor(desered);
			visitor.addCustomComparator(new ExtensibeImplEqualityComp(visitor));
			traverse.accept(visitor);
			boolean res = (Boolean) traverse.traverseObject(origi);
			assertEquals(Boolean.TRUE, res);
		} catch (Exception ex) {
			ex.printStackTrace();
			Assert.fail("Compilation or deserialization did not succeed.");
		}
	}

}
