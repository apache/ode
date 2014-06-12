package org.apache.ode.bpel.obj.serde;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.apache.ode.bpel.obj.OProcess;
import org.apache.ode.bpel.obj.OProcessWrapper;
import org.junit.Test;

public class SerializerTest {

	@Test
	public void testBasicSerialize() throws IOException {
		OmSerdeFactory serdeFactory = new OmSerdeFactory();
		OProcessWrapper wrapper = new OProcessWrapper(new Date().getTime());
		wrapper.setOProcess(new OProcess());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		OmSerializer omSerializer = serdeFactory.createOmSerializer(baos, wrapper);
		omSerializer.serialize();
		
		InputStream is = new BufferedInputStream(new ByteArrayInputStream(baos
				.toByteArray()));
		OmDeserializer omDeserializer = serdeFactory.createOmDeserializer(is);
		OProcessWrapper wrapper2 = omDeserializer.deserialize();
		
		assertArrayEquals(wrapper.getMagic(), wrapper2.getMagic());
		assertEquals(wrapper.getFormat(), wrapper2.getFormat());
		assertEquals(wrapper.getProcess().getFieldContainer(), wrapper2.getProcess().getFieldContainer());
	}
}
