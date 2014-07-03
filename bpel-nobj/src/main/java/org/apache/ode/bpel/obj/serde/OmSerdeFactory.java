package org.apache.ode.bpel.obj.serde;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.ode.bpel.obj.OProcessWrapper;

public class OmSerdeFactory {
	public static final short FORMAT_UNINITIALIZED = 0x00;
	public static final short FORMAT_SERIALIZED_JSON = 0x10;
	public static final short FORMAT_SERIALIZED_SMILE = 0x11;
	public static final short FORMAT_SERIALIZED_DEFAULT = FORMAT_SERIALIZED_JSON;

	private short format = FORMAT_SERIALIZED_DEFAULT;
	
	
	public OmSerializer createOmSerializer(OutputStream out, OProcessWrapper wrapper){
		OmSerializer serializer = null;
		switch (format) {
		case FORMAT_SERIALIZED_JSON:
			serializer = new JsonOmSerializer(out, wrapper);
			break;
		case FORMAT_SERIALIZED_SMILE:
			serializer = new SmileOmSerializer(out, wrapper);
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
		default:
			throw new SerializaionRtException("Unsupported format");
		}
		return deser;
	}

	public short getFormat() {
		return format;
	}
	public void setFormat(short format) {
		this.format = format;
	}
	
}
