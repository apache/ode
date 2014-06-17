package org.apache.ode.bpel.obj.serde;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.obj.OProcessWrapper;
import org.apache.ode.bpel.obj.serde.jacksonhack.TypeBeanSerializerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class JsonOmDeserializer implements OmDeserializer {
	protected static final Log __log = LogFactory.getLog(JsonOmDeserializer.class);
	
	private OProcessWrapper wrapper;
	private InputStream is;

	protected JsonFactory factory;
	private ObjectMapper mapper;
	private Map<Class<?>, JsonDeserializer<?>> deserializers;
	
	public JsonOmDeserializer() {
		deserializers = new HashMap<>();
	}
	public JsonOmDeserializer(InputStream is){
		this(is, new JsonFactory());
	}
	protected JsonOmDeserializer(InputStream is, JsonFactory factory){
		this();
		this.is = is;
		this.factory = factory;
		wrapper = new OProcessWrapper();
	}
	
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public OProcessWrapper deserialize()
			throws IOException, SerializaionRtException {
		mapper = new ObjectMapper(factory);
		mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
		mapper.setSerializerFactory(TypeBeanSerializerFactory.instance);
		SimpleModule simpleModule = new SimpleModule("SimpleModule");
		for (Class<?> d : deserializers.keySet()) {
			simpleModule.addDeserializer((Class) d,
					(JsonDeserializer) deserializers.get(d));
		}
		mapper.registerModule(simpleModule);

		wrapper = mapper.readValue(is, OProcessWrapper.class);
		return wrapper;
	}


	public void addCustomDeserializer(Class<?> c,
			JsonDeserializer<?> sd) {
		if (deserializers.containsKey(c)) {
			__log.warn("Deserizer for type " + c
					+ " has been added. Removed previous one");
		}
		deserializers.put(c, sd);
	}
	public JsonDeserializer<?> removeCustomDeserializer(Class<?> c){
		return deserializers.remove(c);
	}
	public InputStream getIs() {
		return is;
	}
	public void setIs(InputStream is) {
		this.is = is;
	}
}
