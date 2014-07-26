package org.apache.ode.bpel.obj.serde;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.ode.bpel.obj.OProcessWrapper;

public class OmSerdeFactory {
	public static final SerializeFormat FORMAT_SERIALIZED_DEFAULT = SerializeFormat.FORMAT_SERIALIZED_JAVA;

	private SerializeFormat format = FORMAT_SERIALIZED_DEFAULT;
	
	
	public OmSerializer createOmSerializer(OutputStream out, OProcessWrapper wrapper){
		OmSerializer serializer = null;
		switch (format) {
		case FORMAT_SERIALIZED_JSON:
			serializer = new JsonOmSerializer(out, wrapper);
			break;
		case FORMAT_SERIALIZED_SMILE:
			serializer = new SmileOmSerializer(out, wrapper);
			break;
		case FORMAT_SERIALIZED_JAVA:
			serializer = new JavaSerOmSerializer(out, wrapper);
			break;
		default:
			throw new SerializaionRtException("Unsupported format");
		}
		return serializer;
	}

	public OmDeserializer createOmDeserializer(InputStream is){
		OmDeserializer deser = null;
		switch (format) {
		case FORMAT_SERIALIZED_JSON:
			deser = new JsonOmDeserializer(is);
			break;
		case FORMAT_SERIALIZED_SMILE:
			deser = new SmileOmDeserializer(is);
		case FORMAT_SERIALIZED_JAVA:
			deser = new JavaSerOmDeserializer(is);
			break;
		default:
			throw new SerializaionRtException("Unsupported format");
		}
		return deser;
	}

	public SerializeFormat getFormat() {
		return format;
	}
	public void setFormat(SerializeFormat format) {
		this.format = format;
	}
	
	public static enum SerializeFormat{
		FORMAT_UNINITIALIZED,
		FORMAT_SERIALIZED_JSON,
		FORMAT_SERIALIZED_SMILE,
		FORMAT_SERIALIZED_JAVA;
	}
}
