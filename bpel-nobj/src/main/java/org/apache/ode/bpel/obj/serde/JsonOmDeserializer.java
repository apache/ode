package org.apache.ode.bpel.obj.serde;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.wsdl.OperationType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.obj.OProcess;
import org.apache.ode.bpel.obj.OProcessWrapper;
import org.apache.ode.bpel.obj.serde.jacksonhack.TypeBeanSerializerFactory;
import org.apache.ode.utils.NSContext;
import org.w3c.dom.Element;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.module.jaxb.deser.DomElementJsonDeserializer;
public class JsonOmDeserializer implements OmDeserializer {
	protected static final Log __log = LogFactory
			.getLog(JsonOmDeserializer.class);

	private OProcess process;
	private InputStream is;

	protected JsonFactory factory;
	private ObjectMapper mapper;
	private Map<Class<?>, JsonDeserializer<?>> deserializers;

	public JsonOmDeserializer() {
		deserializers = new HashMap<Class<?>, JsonDeserializer<?>>();
		addCustomDeserializer(OperationType.class,
				new OperationTypeDeserializer());
		addCustomDeserializer(Element.class, new DomElementDeserializerHack());
		addCustomDeserializer(NSContext.class, new NSContextDeserializer(NSContext.class));
	}

	public JsonOmDeserializer(InputStream is) {
		this(is, new JsonFactory());
	}

	protected JsonOmDeserializer(InputStream is, JsonFactory factory) {
		this();
		this.is = is;
		this.factory = factory;
		process = new OProcess();
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public OProcess deserialize() throws SerializaionRtException {
		mapper = new ObjectMapper(factory);
		mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
		mapper.setSerializerFactory(TypeBeanSerializerFactory.instance);
		SimpleModule simpleModule = new SimpleModule("SimpleModule");
		for (Class<?> d : deserializers.keySet()) {
			simpleModule.addDeserializer((Class) d,
					(JsonDeserializer) deserializers.get(d));
		}
		simpleModule.setDeserializerModifier(new OModelDeserModifier());
		simpleModule.addKeyDeserializer(Object.class, new KeyAsJsonDeserializer());
		simpleModule.addKeyDeserializer(String.class, new KeyAsJsonDeserializer());
		mapper.registerModule(simpleModule);

		try {
			process = mapper.readValue(is, OProcess.class);
		} catch (Exception e1) {
			SerializaionRtException e = new SerializaionRtException("Error when deseriaze process during deseriazation");
			e.initCause(e1);
			throw e;
		}
		return process;
	}

	public void addCustomDeserializer(Class<?> c, JsonDeserializer<?> sd) {
		if (deserializers.containsKey(c)) {
			__log.warn("Deserizer for type " + c
					+ " has been added. Removed previous one");
		}
		deserializers.put(c, sd);
	}

	public JsonDeserializer<?> removeCustomDeserializer(Class<?> c) {
		return deserializers.remove(c);
	}

	public InputStream getIs() {
		return is;
	}

	public void setIs(InputStream is) {
		this.is = is;
	}

	public static class OModelDeserModifier extends BeanDeserializerModifier {
		@Override
		public JsonDeserializer<?> modifyDeserializer(
				DeserializationConfig config, BeanDescription beanDesc,
				JsonDeserializer<?> deserializer) {
			return deserializer;
		}
	}

	public static class OperationTypeDeserializer extends
			StdScalarDeserializer<OperationType> {
		private static final long serialVersionUID = 2015036061829834379L;

		protected OperationTypeDeserializer() {
			super(OperationType.class);
		}

		@Override
		public OperationType deserialize(JsonParser jp,
				DeserializationContext ctxt) throws IOException,
				JsonProcessingException {

			JsonToken t = jp.getCurrentToken();
			if (t == JsonToken.VALUE_STRING) {
				String text = jp.getText().trim();
				if ("ONE_WAY".equals(text)) {
					return OperationType.ONE_WAY;
				} else if ("REQUEST_RESPONSE".equals(text)) {
					return OperationType.REQUEST_RESPONSE;
				} else if ("SOLICIT_RESPONSE".equals(text)) {
					return OperationType.SOLICIT_RESPONSE;
				} else if ("NOTIFICATION".equals(text)) {
					return OperationType.NOTIFICATION;
				}
			}

			throw ctxt.mappingException("Could not deserialize OperationType");
		}

		@Override
		public boolean isCachable() {
			return true;
		}
	}

	public static class DomElementDeserializerHack extends DomElementJsonDeserializer{
		private static final long serialVersionUID = 2447322357224915181L;

		@Override
	    public Object deserializeWithType(JsonParser jp, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException {
	        boolean hadStartArray = jp.isExpectedStartArrayToken();
	        JsonToken t = jp.nextToken();
	        assert t == JsonToken.VALUE_STRING;
	        String type = jp.getText();
	        assert type.equals("org.w3c.dom.Element");
	        jp.nextToken();
	        Object value;
	        value = deserialize(jp, ctxt);
	        if (hadStartArray && jp.nextToken() != JsonToken.END_ARRAY) {
	            throw ctxt.wrongTokenException(jp, JsonToken.END_ARRAY,
	                    "expected closing END_ARRAY after type information and deserialized value");
	        }
	        return value;    
		}
	}
	
	public static class NSContextDeserializer extends StdScalarDeserializer<NSContext>{
		private static final long serialVersionUID = -4581782525089784968L;

		protected NSContextDeserializer(Class<?> vc) {
			super(vc);
		}

		@Override
		public NSContext deserialize(JsonParser jp, DeserializationContext ctxt)
				throws IOException, JsonProcessingException {
			@SuppressWarnings("unchecked")
			Map<String, String> map = jp.readValueAs(Map.class);
			NSContext ctx = new NSContext();
			ctx.register(map);
			return ctx;
		}
		
	}

}
