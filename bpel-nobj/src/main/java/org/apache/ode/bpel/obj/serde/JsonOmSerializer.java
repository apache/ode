package org.apache.ode.bpel.obj.serde;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.wsdl.OperationType;
import javax.wsdl.Part;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.obj.OProcess;
import org.apache.ode.bpel.obj.serde.jacksonhack.TypeBeanSerializerFactory;
import org.apache.ode.utils.NSContext;
import org.w3c.dom.Element;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import com.fasterxml.jackson.module.jaxb.ser.DomElementJsonSerializer;
import com.ibm.wsdl.MessageImpl;


public class JsonOmSerializer implements OmSerializer {
	protected static final Log __log = LogFactory
			.getLog(JsonOmSerializer.class);

	private OutputStream os;
	private OProcess process;

	private ObjectMapper mapper;
	protected JsonFactory factory;
	private Map<Class<?>, JsonSerializer<?>> serializers;

	public JsonOmSerializer() {
		serializers = new HashMap<Class<?>, JsonSerializer<?>>();
		addCustomSerializer(OperationType.class, new OperationTypeSerializer());
		addCustomSerializer(Element.class, new DomElementSerializerHack());
		addCustomSerializer(NSContext.class, new NSContextSerializer(NSContext.class));
		addCustomSerializer(MessageImpl.class, new MessageSerializer(MessageImpl.class));
	}

	public JsonOmSerializer(OutputStream os, OProcess process){
		this(os, process, new JsonFactory());
	}

	protected JsonOmSerializer(OutputStream os, OProcess process,
			JsonFactory factory) {
		this();
		this.os = os;
		this.process = process;
		this.factory = factory;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void serialize(){
		mapper = new ObjectMapper(factory);
		mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
		mapper.setSerializerFactory(TypeBeanSerializerFactory.instance);
		SimpleModule simpleModule = new SimpleModule("SimpleModule");
		for (Class<?> ss : serializers.keySet()) {
			simpleModule.addSerializer((Class) ss,
					(JsonSerializer) serializers.get(ss));
		}

		simpleModule.addKeySerializer(Object.class, new KeyAsJsonSerializer());
		mapper.registerModule(simpleModule);
		
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		try {
			mapper.writeValue(os, process);
			os.flush();
		} catch (Exception e) {
			SerializaionRtException se = new SerializaionRtException("error when serialize process");
			se.initCause(e);
			throw se;
		}
	}

	public void addCustomSerializer(Class<?> c, JsonSerializer<?> ss) {
		if (serializers.containsKey(c)) {
			__log.warn("Serializer for type " + c
					+ " has been added. Removed previous one");
		}
		serializers.put(c, ss);
	}

	public JsonSerializer<?> removeCustomSerializer(Class<?> c) {
		return serializers.remove(c);
	}

	public OutputStream getOs() {
		return os;
	}

	public void setOs(OutputStream os) {
		this.os = os;
	}

	public static class OperationTypeSerializer extends
			StdScalarSerializer<OperationType> {

		protected OperationTypeSerializer() {
			super(OperationType.class, false);
		}

		@Override
		public void serialize(OperationType value, JsonGenerator jgen,
				SerializerProvider provider) throws IOException,
				JsonGenerationException {
			if (OperationType.ONE_WAY.equals(value)) {
				jgen.writeString("ONE_WAY");
			} else if (OperationType.REQUEST_RESPONSE.equals(value)) {
				jgen.writeString("REQUEST_RESPONSE");
			} else if (OperationType.SOLICIT_RESPONSE.equals(value)) {
				jgen.writeString("SOLICIT_RESPONSE");
			} else if (OperationType.NOTIFICATION.equals(value)) {
				jgen.writeString("NOTIFICATION");
			} else {
				// unknown type
				jgen.writeString(value.toString());
			}
		}

		@Override
		public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
			return createSchemaNode("string", true);
		}

	}
	
	public static class DomElementSerializerHack extends DomElementJsonSerializer{
	    @Override
	    public void serializeWithType(Element value, JsonGenerator jgen, SerializerProvider provider,
	            TypeSerializer typeSer)
				throws IOException, JsonGenerationException {
			String typeId = "org.w3c.dom.Element";
			jgen.writeStartArray();
			jgen.writeString(typeId);
			serialize(value, jgen, provider);
			jgen.writeEndArray();
		}
	}
	
	public static class NSContextSerializer extends StdScalarSerializer<NSContext>{

	
		protected NSContextSerializer(Class<NSContext> t) {
			super(t);
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public void serialize(NSContext value, JsonGenerator jgen,
				SerializerProvider provider) throws IOException,
				JsonGenerationException {
			jgen.writeObject(new HashMap(value.toMap()));
		}
		
	}
	
	public static class MessageSerializer extends StdScalarSerializer<MessageImpl>{

		protected MessageSerializer(Class<MessageImpl> class1) {
			super(class1);
		}

		@Override
		public void serialize(MessageImpl value, JsonGenerator jgen,
				SerializerProvider provider) throws IOException,
				JsonGenerationException {
			jgen.writeObject(value.getDocumentationElement());
			jgen.writeObject(value.getExtensibilityElements());
			jgen.writeObject(value.getExtensionAttributes());

			jgen.writeObject(value.getParts());
			jgen.writeObject(value.getNativeAttributeNames());
			jgen.writeObject(value.isUndefined());
			jgen.writeObject(value.getQName());
			try {
				Field f = value.getClass().getDeclaredField("additionOrderOfParts");
				f.setAccessible(true);
				Vector<Part> parts = (Vector)f.get(value);
				jgen.writeObject(parts);
			} catch (Exception e) {
				//nothing to do.
				__log.debug("Exception when serialize MessageImpl:" + e);
			}
		}
		
	}

}
