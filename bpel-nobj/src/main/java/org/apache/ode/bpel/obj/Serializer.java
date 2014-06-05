package org.apache.ode.bpel.obj;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.smile.SmileFactory;

/**
 * Serializer using jackson. Basically, it (de)serializes OProcessWrapper.
 * 
 * The methods and fields are mainly two categories, serialize and deserialize.
 * 
 * @author fangzhen
 * 
 */
public class Serializer {
	public static final short FORMAT_SERIALIZED_JSON = 0x10;
	public static final short FORMAT_SERIALIZED_SMILE = 0x11;
	public static final short FORMAT_SERIALIZED_DEFAULT = FORMAT_SERIALIZED_JSON;

	protected static final Log __log = LogFactory.getLog(Serializer.class);

	// fields for deserializer
	private JsonParser jsonParser;
	private ObjectMapper mapper;
	private Map<Class<?>, JsonDeserializer<?>> deserializers;
	// end field for deserializer

	// fields for serializer
	private Map<Class<?>, JsonSerializer<?>> serializers;

	// end field for serializer

	public Serializer() {
		serializers = new HashMap<>();
		serializers.put(byte[].class, new ByteArraySerializer());
		serializers.put(OBaseExtensible.class,
				new OBaseExtensible.OBaseExtensibleSerializer());

		deserializers = new HashMap<>();
	}

	public void serialize(OProcessWrapper wrapper, String bpelPath)
			throws SerializaionException, IOException {
		serialize(wrapper, bpelPath, FORMAT_SERIALIZED_DEFAULT);
	}

	public void serialize(OProcessWrapper wrapper, String bpelPath, short format)
			throws SerializaionException, IOException {
		String sPath;
		if (format == FORMAT_SERIALIZED_JSON) {
			sPath = bpelPath.substring(0, bpelPath.lastIndexOf(".")) + ".jcbp";
		} else if (format == FORMAT_SERIALIZED_SMILE) {
			sPath = bpelPath.substring(0, bpelPath.lastIndexOf(".")) + ".scbp";
		} else {
			throw new SerializaionException(
					"unsupported serialization format, supported are json and smile");
		}
		OutputStream os;
		os = new BufferedOutputStream(new FileOutputStream(sPath));
		serialize(wrapper, os, format);
	}

	public void serialize(OProcessWrapper wrapper, OutputStream os, short format)
			throws SerializaionException, IOException {
		ObjectMapper mapper;
		if (format == FORMAT_SERIALIZED_JSON) {
			mapper = new ObjectMapper();
		} else if (format == FORMAT_SERIALIZED_SMILE) {
			mapper = new ObjectMapper(new SmileFactory());
		} else {
			throw new SerializaionException(
					"unsupported serialization format, supported are json and smile");

		}
		SimpleModule simpleModule = new SimpleModule("SimpleModule");
		for (Class<?> ss : serializers.keySet()) {
			simpleModule.addSerializer((Class) ss,
					(JsonSerializer) serializers.get(ss));
		}
		mapper.registerModule(simpleModule);
		mapper.writeValue(os, wrapper);
		os.flush();
	}

	public OProcessWrapper deserialize(String cbpPath) throws IOException,
			SerializaionException {
		String ext = cbpPath.substring(cbpPath.lastIndexOf("."),
				cbpPath.length());
		short format;
		if (ext.equals("jcbp")) {
			format = FORMAT_SERIALIZED_JSON;
		} else if (ext.equals("scbp")) {
			format = FORMAT_SERIALIZED_SMILE;
		} else {
			throw new SerializaionException(
					"Unrecognized file extension. jcbp =>json serialization; scbp=>smile serialization");
		}
		InputStream is = new BufferedInputStream(new FileInputStream(cbpPath));
		return deserialize(is, format);
	}

	public OProcessWrapper deserialize(InputStream is, short format)
			throws IOException, SerializaionException {
		JsonFactory factory;
		if (format == FORMAT_SERIALIZED_JSON) {
			factory = new JsonFactory();
		} else if (format == FORMAT_SERIALIZED_SMILE) {
			factory = new SmileFactory();
		} else {
			throw new SerializaionException(
					"unsupported serialization format, supported are json and smile");
		}
		jsonParser = factory.createParser(is);
		mapper = new ObjectMapper(factory);
		SimpleModule simpleModule = new SimpleModule("SimpleModule");
		for (Class<?> d : deserializers.keySet()) {
			simpleModule.addDeserializer((Class) d,
					(JsonDeserializer) deserializers.get(d));
		}
		mapper.registerModule(simpleModule);

		OProcessWrapper wrapper = new OProcessWrapper();
		try {
			readHeader(wrapper, is);
		} catch (OModelException e) {
			e.printStackTrace();
		}
		@SuppressWarnings("unchecked")
		Map<String, Object> oProcess = mapper.readValue(jsonParser, Map.class);
		wrapper.addField(OProcessWrapper.PROCESS, oProcess);
		
		jsonParser.close();
		return wrapper;
	}

	private void readHeader(OProcessWrapper wrapper, InputStream is)
			throws IOException, OModelException {
		jsonParser.nextToken();
		while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
			String fieldName = jsonParser.getCurrentName();
			jsonParser.nextToken();
			switch (fieldName) {
			case OProcessWrapper.MAGIC_NUMBER:
				byte[] magic = new byte[OProcessWrapper.CURRENT_MAGIC_NUMBER.length];
				magic = jsonParser.getBinaryValue();
				wrapper.addField(OProcessWrapper.MAGIC_NUMBER, magic);
				// if (!Arrays.equals(magic,
				// OProcessWrapper.MAGIC_NUMBER_OFH_20140529)){
				// throw new
				// SerializaionException("Unrecognized file format (bad magic number)");
				// }
				break;
			case OProcessWrapper.FORMAT:
				short rformat = jsonParser.getShortValue();
				wrapper.addField(OProcessWrapper.FORMAT, rformat);
				break;
			case OProcessWrapper.COMPILE_TIME:
				long compileTime = jsonParser.getLongValue();
				wrapper.addField(OProcessWrapper.COMPILE_TIME, compileTime);
				break;
			case OProcessWrapper.GUID:
				String guid = jsonParser.getText();
				wrapper.addField(OProcessWrapper.GUID, guid);
				break;
			case OProcessWrapper.TYPE:
				String type = jsonParser.getText();
				wrapper.addField(OProcessWrapper.TYPE, type);
				break;
			case OProcessWrapper.OTHER_HEADERS:
				@SuppressWarnings("unchecked")
				Map<String, Object> headers = mapper.readValue(jsonParser,
						Map.class);
				if (headers == null) {
					headers = new LinkedHashMap<>();
				}
				wrapper.addField(OProcessWrapper.OTHER_HEADERS, headers);
				break;

			case OProcessWrapper.PROCESS:
				// headers has been processed
				wrapper.checkValid();
				return;
			}
		}
	}

	public void addCustomSerializer(Class<Object> c, JsonSerializer<Object> ss) {
		if (serializers.containsKey(c)) {
			__log.warn("Serizer for type " + c
					+ " has been added. Removed previous one");
		}
		serializers.put(c, ss);
	}

	public void addCustomDeserializer(Class<Object> c,
			JsonDeserializer<Object> sd) {
		if (deserializers.containsKey(c)) {
			__log.warn("Deserizer for type " + c
					+ " has been added. Removed previous one");
		}
		deserializers.put(c, sd);
	}

	static class ByteArraySerializer extends JsonSerializer<byte[]> {
		@Override
		public void serialize(byte[] value, JsonGenerator jgen,
				SerializerProvider provider) throws IOException,
				JsonProcessingException {
			jgen.writeBinary(value);
		}
	}

}
