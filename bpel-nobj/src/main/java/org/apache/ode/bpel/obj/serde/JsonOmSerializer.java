package org.apache.ode.bpel.obj.serde;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.obj.OProcessWrapper;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class JsonOmSerializer implements OmSerializer {
	protected static final Log __log = LogFactory.getLog(JsonOmSerializer.class);
	
	private OutputStream os;
	private OProcessWrapper wrapper;

	private ObjectMapper mapper;
	protected JsonFactory factory;
	private Map<Class<?>, JsonSerializer<?>> serializers;

	public JsonOmSerializer() {
		serializers = new HashMap<>();
	}

	public JsonOmSerializer(OutputStream os, OProcessWrapper wrapper) {
		this(os, wrapper, new JsonFactory());
	}

	protected JsonOmSerializer(OutputStream os, OProcessWrapper wrapper,
			JsonFactory factory) {
		this();
		this.os = os;
		this.wrapper = wrapper;
		this.factory = factory;
		mapper = new ObjectMapper(factory);
		mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void serialize() throws SerializaionRtException, IOException {
		SimpleModule simpleModule = new SimpleModule("SimpleModule");
		for (Class<?> ss : serializers.keySet()) {
			simpleModule.addSerializer((Class) ss,
					(JsonSerializer) serializers.get(ss));
		}
		mapper.registerModule(simpleModule);
		mapper.writeValue(os, wrapper);
		os.flush();
	}

	public void addCustomSerializer(Class<?> c, JsonSerializer<?> ss) {
		if (serializers.containsKey(c)) {
			__log.warn("Serizer for type " + c
					+ " has been added. Removed previous one");
		}
		serializers.put(c, ss);
	}
	public JsonSerializer<?> removeCustomSerializer(Class<?> c){
		return serializers.remove(c);
	}
	public OutputStream getOs() {
		return os;
	}

	public void setOs(OutputStream os) {
		this.os = os;
	}

	public OProcessWrapper getWrapper() {
		return wrapper;
	}

	public void setWrapper(OProcessWrapper wrapper) {
		this.wrapper = wrapper;
	}
}
