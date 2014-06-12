package org.apache.ode.bpel.obj;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

/**
 * base class for compiled BPEL objects. It gives some common fields.
 * 
 * @author fangzhen
 * 
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
public class OBase {
	/** The wrapper wraps fields. Fields can be deleted, added or updated */
	protected Map<String, Object> fieldContainer;	
	public static String VERSION = "version";
	public static String ID = "id";
	protected OBase() {
		fieldContainer = new LinkedHashMap<>();
	}

	protected OBase(Map<String, Object> map) {
		fieldContainer = map;
	}

	
	@JsonAnyGetter
	public Map<String, Object> getFieldContainer() {
		return fieldContainer;
	}
	@JsonAnySetter
	public void addField(String name, Object value) {
		fieldContainer.put(name, value);
	}

	@SuppressWarnings("unchecked")
	public <T> T getField(String name){
		return (T)fieldContainer.get(name);
	}
	
	@JsonIgnore
	public int getVersion() {
		return (int)fieldContainer.get(VERSION);
	}
	public void setVersion(int version) {
		fieldContainer.put(VERSION, version);
	}

	@JsonIgnore
	public int getId() {
		return (int)fieldContainer.get(ID);
	}

	public void setId(int id) {
		fieldContainer.put(ID, id);
	}
}
