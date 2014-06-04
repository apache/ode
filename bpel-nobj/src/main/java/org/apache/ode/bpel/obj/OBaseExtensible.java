package org.apache.ode.bpel.obj;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class OBaseExtensible implements Extensible{
	/** the wrapper wraps the whole process and other data */
	private Map<String, Object> map;
	protected OBaseExtensible(){
		map = new LinkedHashMap<>();
	}
	@Override
	public <T> void addField(String fieldName, T value) {
		map.put(fieldName, value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getField(String fieldName) {
		return (T)map.get(fieldName);
	}
	
	/**
	 * return map representation of the OBaseExtensible
	 * @return
	 */
	protected Map<String, Object> getMap(){
		return map;
	}
	public String toString(){
		return map.toString();
	}
	public static class OBaseExtensibleSerializer extends JsonSerializer<OBaseExtensible>{

		@Override
		public void serialize(OBaseExtensible value, JsonGenerator jgen,
				SerializerProvider provider) throws IOException,
				JsonGenerationException {
			jgen.writeObject(value.getMap());
		}
		
	}
}
