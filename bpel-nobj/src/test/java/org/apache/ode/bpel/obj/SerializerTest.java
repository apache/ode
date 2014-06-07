package org.apache.ode.bpel.obj;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.collections.map.HashedMap;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.smile.SmileFactory;

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

	@Test
	public void testReference2() throws JsonGenerationException,
			JsonMappingException, IOException {
		Map<String, Object> map = new LinkedHashMap<>();
		A a = new A();
		a.next = a;
		a.val = 100;
		map.put("i1", 100);
		map.put("i2", a);
		map.put("i4", a);
		Map<String, Object> map2 = new LinkedHashMap<>();
		map2.put("i31", "a string");
		map2.put("i32", a);
		map.put("i3", map2);
		B b = new B();
		b.parent = a;
		A a2 = b;
		map.put("i5", b);
		map.put("i6", a2);
		ObjectMapper mapper = new ObjectMapper();
		mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		mapper.writeValue(os, map);
		System.out.println(os);

		ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
		ObjectMapper m2 = new ObjectMapper();
		m2.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
		Map dem = m2.readValue(is, Map.class);
		System.out.println(dem);
	}

	@Test
	public void testReference() throws JsonGenerationException,
			JsonMappingException, IOException {
		A a = new A();
		a.val = 1;
//		a.next = a;
		//		JsonFactory f = new SmileFactory();
		JsonFactory f = new JsonFactory();
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("item1", a);
//		map.put("item2", a);
		//		map.put("item3", new A());
//		map.put("item4", 1);
		ObjectMapper mapper = new ObjectMapper(f);
		mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		mapper.writeValue(os, map);
		System.out.println(os);

		ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
		ObjectMapper m2 = new ObjectMapper(f);
		m2.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
		//		Map dem = m2.readValue(is, new TypeReference<Map<String,A>>() { });
		Map dem = m2.readValue(is, Map.class);
		System.out.println(dem);
	}

	@Test
	public void testReference3() throws JsonParseException, JsonMappingException, IOException{
		String jsonStr = "[\"java.util.LinkedHashMap\",{\"item1\":[\"org.apache.ode.bpel.obj.SerializerTest$A\",{\"@id\":1,\"val\":1}]," + 
		"\"item2\":[\"org.apache.ode.bpel.obj.SerializerTest$A\",1]}]";
		ByteArrayInputStream is = new ByteArrayInputStream(jsonStr.getBytes());
		ObjectMapper m2 = new ObjectMapper();
		m2.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
		Map dem = m2.readValue(is, Map.class);
		System.out.println(dem);
	}

	@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
	static class A {
		private int val;
		private A next;

		public int getVal() {
			return val;
		}

		public String toString() {
			String nextstr;
			if (next == null) nextstr = "null";
			else nextstr = "" + next.hashCode();
			return "|type = " + this.getClass() + ", val = " + val
					+ ", next = " + nextstr + "|";
		}

		public A getNext() {
			return next;
		}

	}
	@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
	static class B extends A{
		private A parent;
		public A getParent(){
			return parent;
		}
	}
}
