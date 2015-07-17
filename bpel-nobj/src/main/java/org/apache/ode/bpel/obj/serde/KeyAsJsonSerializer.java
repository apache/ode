package org.apache.ode.bpel.obj.serde;

import java.io.IOException;
import java.net.URI;

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
	
	public static String URIPrefix = "_-_-java.net.URI_TYPE_HACKER";
	@Override
	public void serialize(Object value, JsonGenerator jgen,
			SerializerProvider provider) throws IOException,
			JsonProcessingException {
		String json;
		if (value instanceof URI){
			json =  URIPrefix + value;
		}else{
			json = mapper.writeValueAsString(value);
		}
		jgen.writeFieldName(json);
	}

}
