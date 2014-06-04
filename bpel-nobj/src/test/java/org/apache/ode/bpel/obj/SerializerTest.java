package org.apache.ode.bpel.obj;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

import org.apache.commons.collections.map.HashedMap;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SerializerTest {

	@Test
	public void testSerialize() throws IOException {
		Serializer serializer = new Serializer();
		OProcessWrapper wrapper = new OProcessWrapper(new Date().getTime());
		wrapper.setOProcess(new OProcess());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		serializer.serialize(wrapper, baos, Serializer.FORMAT_SERIALIZED_DEFAULT);
		System.out.println(baos);

		OProcessWrapper wrapper2 = serializer.deserialize(new BufferedInputStream(
				new ByteArrayInputStream(baos.toByteArray())),
				Serializer.FORMAT_SERIALIZED_DEFAULT);
		System.out.println(wrapper2);
	}

	/**
	 * basic use of jackson
	 * 
	 * @throws JsonGenerationException
	 * @throws IOException
	 */
	// @Test
	public void testJakson() throws JsonGenerationException, IOException {
		JsonFactory f = new JsonFactory();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		JsonGenerator g = f.createGenerator(os);
		g.writeStartObject();
		g.writeBooleanField("verified", false);
		g.writeObjectFieldStart("name");
		g.writeStringField("first", "Joe");
		g.writeStringField("last", "Sixpack");
		g.writeEndObject(); // for field 'name'
		g.writeStringField("gender", "male");
		g.writeEndObject();
		g.close();

		// unmarshal
		String content = os.toString();
		System.out.println(content);

		ObjectMapper m = new ObjectMapper();
		Map<String, Object> obj1 = m.readValue(content, Map.class);
		System.out.println(obj1);

		JsonParser jp = f.createParser(content);
		Map<String, Object> obj = new HashedMap();
		jp.nextToken(); // will return JsonToken.START_OBJECT (verify?)
		while (jp.nextToken() != JsonToken.END_OBJECT) {
			String fieldname = jp.getCurrentName();
			jp.nextToken();
			if ("name".equals(fieldname)) {
				ObjectMapper mapper = new ObjectMapper();
				Map<String, String> name = mapper.readValue(jp,
						new TypeReference<Map<String, String>>() {
						});
				obj.put(fieldname, name);
			} else {
				String value = jp.getText();
				obj.put(fieldname, value);
			}
		}
		jp.close();
		System.out.println(obj);
	}
}
