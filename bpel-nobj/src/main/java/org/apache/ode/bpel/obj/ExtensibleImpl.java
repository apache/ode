package org.apache.ode.bpel.obj;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class, property = "@id")
public class ExtensibleImpl  implements Extensible<Object>, Serializable{
	public static final long serialVersionUID = -1L;
	
	/** The wrapper wraps fields. Fields can be deleted, added or updated */
	transient protected Map<String, Object> fieldContainer;
	/** Version of this class*/
	private static final String CLASS_VERSION = "classVersion";

	protected ExtensibleImpl() {
		fieldContainer = new LinkedHashMap<String, Object>();
	}
	protected ExtensibleImpl(Map<String, Object> container) {
		fieldContainer = container;
	}
	
//	@JsonAnyGetter
	public Map<String, Object> getFieldContainer() {
		return fieldContainer;
	}
	public void setFieldContainer(Map<String, Object> fieldContainer){
		this.fieldContainer = fieldContainer;
	}

//	@JsonAnySetter
	public void addField(String name, Object value) {
		fieldContainer.put(name, value);
	}

	public Object getField(String name) {
		return fieldContainer.get(name);
	}

	@Override
	public Object delField(String fieldName) {
		return fieldContainer.remove(fieldName);
	}
	
	@JsonIgnore
	public int getClassVersion() {
		Object o = fieldContainer.get(CLASS_VERSION);
		return o == null ? 0 : (Integer)o;
	}

	public void setClassVersion(int version) {
		fieldContainer.put(CLASS_VERSION, version);
	}
	
	private void writeObject(ObjectOutputStream oos) throws IOException{
		oos.defaultWriteObject();
		oos.writeInt(fieldContainer.size());
		oos.writeObject(fieldContainer.getClass().getName());
		for (String key : fieldContainer.keySet()){
			oos.writeObject(key);
			oos.writeObject(fieldContainer.get(key));
		}
	}
	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException{
		ois.defaultReadObject();
		int size = ois.readInt();
		String cls = (String)ois.readObject();
		try {
			fieldContainer = (Map<String, Object>)(Class.forName(cls).newInstance());
		} catch (Exception e) {
			//should never get here
			e.printStackTrace();
		}
		for (int i = 0; i < size; i++){
			String key = (String)ois.readObject();
			Object value = ois.readObject();
			fieldContainer.put(key, value);
		}
	}
}
