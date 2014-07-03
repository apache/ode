package org.apache.ode.bpel.obj.serde.jacksonhack;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonTest {
	/**
	 * Test the TypeBeanSerializer
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@Test
	public void testReference() throws JsonGenerationException,
			JsonMappingException, IOException {
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		A a = new A();
		a.next = a;
		a.val = 100;
		map.put("i1", 100);
		map.put("i2", a);
		map.put("i4", a);
		Map<String, Object> map2 = new LinkedHashMap<String, Object>();
		String s = "a string";
		map2.put("i31", s);
		map2.put("i33", s);
		map2.put("i32", a);
		map.put("i3", map2);
		B b = new B();
		b.parent = a;
		A a2 = b;
		map.put("i5", b);
		map.put("i6", a2);
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializerFactory(TypeBeanSerializerFactory.instance);
		mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		mapper.writeValue(os, map);
		ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
		ObjectMapper m2 = new ObjectMapper();
		m2.setSerializerFactory(TypeBeanSerializerFactory.instance);
		m2.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
		@SuppressWarnings("rawtypes")
		Map dem = m2.readValue(is, Map.class);
		assertEquals(100, ((Integer)dem.get("i1")).intValue());
		assertEquals(dem.get("i2"), dem.get("i4"));
		assertEquals(B.class, dem.get("i5").getClass());
		@SuppressWarnings("rawtypes")
		Map dem2 = (Map)dem.get("i3");
		assertEquals(dem2.get("i31"), dem2.get("i33"));
		assertEquals(dem2.get("i32"), dem.get("i2"));
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
			return "|type = " + this.getClass() + "@" + hashCode() + ", val = " + val
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
