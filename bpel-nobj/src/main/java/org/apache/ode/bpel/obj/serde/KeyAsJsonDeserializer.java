package org.apache.ode.bpel.obj.serde;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;

public class KeyAsJsonDeserializer extends KeyDeserializer{
	static ObjectMapper mapper = new ObjectMapper();
	static {
		mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);	
	}
	@Override
	public Object deserializeKey(String key, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		if (key.startsWith(KeyAsJsonSerializer.URIPrefix)){
			key = key.substring(KeyAsJsonSerializer.URIPrefix.length());
			try {
				return new URI(key);
			} catch (URISyntaxException e) {
				// should never get here.
				e.printStackTrace();
				return null;
			}
		}
		return mapper.readValue(key, Object.class);
	}
	

}
