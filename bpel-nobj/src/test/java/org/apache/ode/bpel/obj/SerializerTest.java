package org.apache.ode.bpel.obj;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;

import org.apache.ode.bpel.obj.serde.Serializer;
import org.junit.Test;

public class SerializerTest {

	@Test
	public void testSerialize() throws IOException {
		Serializer serializer = new Serializer();
		OProcessWrapper wrapper = new OProcessWrapper(new Date().getTime());
		wrapper.setOProcess(new OProcess());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		serializer.serialize(wrapper, baos,
				Serializer.FORMAT_SERIALIZED_DEFAULT);
		System.out.println(baos);

		OProcessWrapper wrapper2 = serializer.deserialize(
				new BufferedInputStream(new ByteArrayInputStream(baos
						.toByteArray())), Serializer.FORMAT_SERIALIZED_DEFAULT);
		System.out.println(wrapper2);
	}

}
