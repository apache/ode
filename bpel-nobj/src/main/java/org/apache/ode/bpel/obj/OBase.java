package org.apache.ode.bpel.obj;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.ode.bpel.o.DebugInfo;

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
 
	/** Our identifier, in terms of our parent. */
	public static String ID = "id";
	/** Version of this class*/
	public static String VERSION = "version";
	/** Owner OProcess */
	public static String OWNER = "owner";
	public static String DEBUG_INFO = "debugInfo";
	
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
	/**
	 * Convenient method to add or set field dynamically.
	 * @param name
	 * @param value
	 */
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
	
	@JsonIgnore
	public OProcess getOwner(){
		return (OProcess)fieldContainer.get(OWNER);
	}
	public void setOwner(OProcess process){
		fieldContainer.put(OWNER, process);
	}
	
	@JsonIgnore
	public DebugInfo getDebugInfo(){
		return (DebugInfo)fieldContainer.get(DEBUG_INFO);
	}
	public void setDebugInfo(DebugInfo debugInfo){
		fieldContainer.put(DEBUG_INFO, debugInfo);
	}
}
