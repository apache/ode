package org.apache.ode.bpel.obj.serde;

import static org.junit.Assert.assertEquals;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.ode.bpel.obj.OProcess;
import org.junit.Test;

public class SerializerTest {

	@Test
	public void testBasicSerialize() throws IOException {
		OmSerdeFactory serdeFactory = new OmSerdeFactory();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		OProcess original = new OProcess("0");
		original.setProcessName("process1");
      	DeSerializer serializer = new DeSerializer();
    	serializer.serialize(baos, original);
		
		InputStream is = new BufferedInputStream(new ByteArrayInputStream(baos
				.toByteArray()));
		DeSerializer deSerializer = new DeSerializer(is);
		OProcess desered = deSerializer.deserialize();
		assertEquals(original.getFieldContainer(), desered.getFieldContainer());
	}
}
