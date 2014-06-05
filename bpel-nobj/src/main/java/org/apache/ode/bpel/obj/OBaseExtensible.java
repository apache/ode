package org.apache.ode.bpel.obj;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * An implementation of Extensible interface using map as a container of fields.
 * Other OModel classes should inherit this class.
 * 
 * In fact, in it's subclass, we don't have to put all fields into the
 * map. There might be other fields along with the map. And override
 * getMapRepr() to wraps all fields including the map as a new map and return
 * it. Meanwhile, the add/del/getField() methods are targeted at the map for
 * extension.
 * 
 * @author fangzhen
 * 
 */
public class OBaseExtensible implements Extensible {
	/** The wrapper wraps fields. Fields can be deleted, added or updated */
	protected Map<String, Object> fieldContainer;

	protected OBaseExtensible() {
		fieldContainer = new LinkedHashMap<>();
	}

	protected OBaseExtensible(Map<String, Object> map) {
		fieldContainer = map;
	}

	@Override
	public <T> void addField(String fieldName, T value) {
		fieldContainer.put(fieldName, value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getField(String fieldName) {
		return (T) fieldContainer.get(fieldName);
	}

	@Override
	public boolean delField(String fieldName) {
		return fieldContainer.remove(fieldName) != null;
	}

	/**
	 * Return map representation of the OBaseExtensible. If the fieldContainer
	 * maintains all the fields, return it simply. But the map returned should
	 * not be modified, since we cannot guarantee the change be applied back to
	 * the OBaseExtensible. use add/delField instead
	 * 
	 * @return
	 */
	protected Map<String, Object> getMapRepr() {
		return fieldContainer;
	}

	public String toString() {
		return fieldContainer.toString();
	}

	public static class OBaseExtensibleSerializer extends
			JsonSerializer<OBaseExtensible> {

		@Override
		public void serialize(OBaseExtensible value, JsonGenerator jgen,
				SerializerProvider provider) throws IOException,
				JsonGenerationException {
			jgen.writeObject(value.getMapRepr());
		}

	}

}
