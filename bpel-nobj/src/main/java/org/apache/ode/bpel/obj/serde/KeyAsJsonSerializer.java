package org.apache.ode.bpel.obj.serde;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;

public class KeyAsJsonSerializer extends JsonSerializer<Object> {
	static ObjectMapper mapper = new ObjectMapper();
	static {
		mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
	}
	@Override
	public void serialize(Object value, JsonGenerator jgen,
			SerializerProvider provider) throws IOException,
			JsonProcessingException {
		String json;
		if (value instanceof String) {
			json = (String)value;
		}else{
			json = mapper.writeValueAsString(value);
		}
		json = mapper.writeValueAsString(value);
		jgen.writeFieldName(json);
	}

}
