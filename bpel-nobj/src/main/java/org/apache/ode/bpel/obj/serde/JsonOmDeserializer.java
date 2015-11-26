package org.apache.ode.bpel.obj.serde;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.wsdl.OperationType;
import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.ode.bpel.obj.OProcess;
import org.apache.ode.bpel.obj.serde.jacksonhack.TypeBeanSerializerFactory;
import org.apache.ode.utils.NSContext;
import org.w3c.dom.Element;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.introspect.POJOPropertyBuilder;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.module.jaxb.deser.DomElementJsonDeserializer;
import com.ibm.wsdl.AbstractWSDLElement;
import com.ibm.wsdl.MessageImpl;
/**
 * OModel Serializer that deserialize process from json using jackson.
 * Corresponding to format {@link OmSerdeFactory.SerializeFormat#FORMAT_SERIALIZED_JSON}
 * @see JsonOmSerializer
 */
public class JsonOmDeserializer implements OmDeserializer {
	protected static final Logger __log = LoggerFactory.getLogger(JsonOmDeserializer.class);

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
		addCustomDeserializer(MessageImpl.class, new MessageDeserializer(MessageImpl.class));
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
		simpleModule.setDeserializerModifier(new WsdlElementDeserModifier());
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

	/**
	 * @see JsonOmSerializer.OperationTypeSerializer
	 */
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

	/**
	 * Add type info to {@link org.w3c.dom.Element}. 
	 * 
	 * @see DomElementJsonDeserializer
	 */
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
	/**
	 * @see JsonOmSerializer.NSContextSerializer
	 */
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
	
	/**
	 * @see JsonOmSerializer.MessageSerializer
	 */
	public static class MessageDeserializer extends StdScalarDeserializer<MessageImpl>{

		protected MessageDeserializer(Class<?> vc) {
			super(vc);
		}

		@Override
		public MessageImpl deserialize(JsonParser jp, DeserializationContext ctxt)
				throws IOException, JsonProcessingException {
			MessageImpl value = new MessageImpl();
			value.setDocumentationElement(jp.readValueAs(Element.class));
			value.getExtensibilityElements().addAll(jp.readValueAs(Vector.class));
			value.getExtensionAttributes().putAll(jp.readValueAs(HashMap.class));

			value.getParts().putAll(jp.readValueAs(HashMap.class));
			Field f1;
			try {
				f1 = MessageImpl.class.getDeclaredField("nativeAttributeNames");
				f1.setAccessible(true);
				f1.set(value, jp.readValueAs(List.class));
			} catch (Exception e) {
				__log.debug("Exception when serialize MessageImpl:" + e);
			}
			value.setUndefined(jp.readValueAs(Boolean.class));
			value.setQName(jp.readValueAs(QName.class));

			Vector<String> parts = jp.readValueAs(Vector.class);
			try{
				Field f = MessageImpl.class.getDeclaredField("additionOrderOfParts");
				f.setAccessible(true);
				f.set(value, parts);
			}catch(Exception e){
				__log.debug("Exception when serialize MessageImpl:" + e);
			}
			return value;
		}
		
	}

	/**
	 * BeanDeserializerModifier to deserialize {@link AbstractWSDLElement} extensibilityElements
	 * property as field to avoid settlessProperities bug [FastXML/jackson-databind#501].
	 */
	public static class WsdlElementDeserModifier extends BeanDeserializerModifier{
		public static class MyBeanPropertyWriter extends BeanPropertyWriter{
			public MyBeanPropertyWriter(BeanPropertyWriter origi, String newName){
				super(origi, new SerializedString(newName));
			}
		}
		@Override
		public List<BeanPropertyDefinition> updateProperties(DeserializationConfig config,
                BeanDescription beanDesc,
                List<BeanPropertyDefinition> propDefs){
			if (!AbstractWSDLElement.class.isAssignableFrom(beanDesc.getBeanClass())) {
				return propDefs;
			}
			Iterator<BeanPropertyDefinition> itor = propDefs.iterator();
			while(itor.hasNext()){
				BeanPropertyDefinition prop = itor.next();
				if (prop.getName().equalsIgnoreCase("extensibilityElements")){
					try {
						AnnotatedField f = new AnnotatedField(AbstractWSDLElement.class.getDeclaredField("extElements"), null);
						((POJOPropertyBuilder)prop).addField(f, new PropertyName("extElements"), false, true, false);
					} catch (NoSuchFieldException e) {
						SerializaionRtException e1 =  new SerializaionRtException(
								"cann't find field, probably implementation of AbstractWSDLElement has changed");
						e1.initCause(e);
						throw e1;
					} catch (ClassCastException e){
						SerializaionRtException e1 = new SerializaionRtException(
								"Cast to POJOPropertyBuilder failde, probably underlying impl of "
								+ "BeanPropertyDefinition has changed");
						e1.initCause(e);
						throw e1;
					}
					break;
				}
			}
			return propDefs;
		}
	}
}
