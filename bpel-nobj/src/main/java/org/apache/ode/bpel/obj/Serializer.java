package org.apache.ode.bpel.obj;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.dataformat.smile.SmileFactory;

/**
 * Serializer using jackson
 * 
 * @author fangzhen
 * 
 */
public class Serializer {
	public static final short FORMAT_SERIALIZED_JSON = 0x10;
	public static final short FORMAT_SERIALIZED_SMILE = 0x11;
	public static final short FORMAT_SERIALIZED_DEFAULT = FORMAT_SERIALIZED_JSON;

	protected static final Log __log = LogFactory.getLog(Serializer.class);
	private JsonParser jsonParser;
	private ObjectMapper mapper;
	private List<StdSerializer<?>> serializers;

	public Serializer() {
		serializers = new ArrayList<StdSerializer<?>>();
		serializers.add(new ByteArraySerializer(byte[].class));
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
		Map<String, Object> map = wrapper.getMap();
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
		for (StdSerializer<?> ss : serializers) {
			simpleModule.addSerializer(ss);
		}
		mapper.registerModule(simpleModule);
		mapper.writeValue(os, map);
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
		
		OProcessWrapper wrapper = new OProcessWrapper();
		if (!readHeader(wrapper, is)){
			//TODO
		}
		Map<String, Object> map = wrapper.getMap();
		OProcess oProcess = mapper.readValue(jsonParser, OProcess.class);
		map.put(OProcessWrapper.PROCESS, oProcess);
		
		jsonParser.close();
		return wrapper;
	}

	private boolean readHeader(OProcessWrapper wrapper, InputStream is) throws IOException {
		jsonParser.nextToken();
		Map<String, Object> map = wrapper.getMap();
		while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
			String fieldName = jsonParser.getCurrentName();
			jsonParser.nextToken();
			switch (fieldName) {
			case OProcessWrapper.MAGIC_NUMBER:
				byte[] magic = new byte[OProcessWrapper.CURRENT_MAGIC_NUMBER.length];
				magic = jsonParser.getBinaryValue();
				map.put(OProcessWrapper.MAGIC_NUMBER, magic);
//				if (!Arrays.equals(magic, OProcessWrapper.MAGIC_NUMBER_OFH_20140529)){
//					throw new SerializaionException("Unrecognized file format (bad magic number)");
//				}
				break;
			case OProcessWrapper.FORMAT:
				short rformat = jsonParser.getShortValue();
				map.put(OProcessWrapper.FORMAT, rformat);
				break;
			case OProcessWrapper.COMPILE_TIME:
				long compileTime = jsonParser.getLongValue();
				map.put(OProcessWrapper.COMPILE_TIME, compileTime);
				break;
			case OProcessWrapper.GUID:
				String guid = jsonParser.getText();
				map.put(OProcessWrapper.GUID, guid);
				break;
			case OProcessWrapper.TYPE:
				String type = jsonParser.getText();
				map.put(OProcessWrapper.TYPE, type);
				break;
			case OProcessWrapper.OTHER_HEADERS:
				@SuppressWarnings("unchecked")
				Map<String, Object> headers = mapper.readValue(jsonParser, Map.class);
				if (headers == null){
					headers = new LinkedHashMap<>();
				}
				map.put(OProcessWrapper.OTHER_HEADERS, headers);
				break;

			case OProcessWrapper.PROCESS:
				//headers has been processed
				return wrapper.checkValid();
			}
		}
		return false;
	}

	public void addCustomSerializer(StdSerializer<?> ss) {
		Class<?> handledType = ss.handledType();
		for (StdSerializer<?> s : serializers) {
			if (handledType == s.handledType()) {
				serializers.remove(s);
				__log.warn("serizer for type " + handledType
						+ " has been added. Removed previous one");
			}
		}
		serializers.add(ss);
	}

	static class ByteArraySerializer extends StdSerializer<byte[]> {
		protected ByteArraySerializer(Class<byte[]> t) {
			super(t);
		}

		@Override
		public void serialize(byte[] value, JsonGenerator jgen,
				SerializerProvider provider) throws IOException,
				JsonProcessingException {
			jgen.writeBinary(value);
		}
	}

}
