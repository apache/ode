package org.apache.ode.bpel.obj.serde;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.ode.bpel.obj.OProcess;

public class OmSerdeFactory {
	public static final SerializeFormat FORMAT_SERIALIZED_DEFAULT = SerializeFormat.FORMAT_SERIALIZED_JAVA;

	private SerializeFormat format = FORMAT_SERIALIZED_DEFAULT;
	
	
	public OmSerializer createOmSerializer(OutputStream out, OProcess process){
		OmSerializer serializer;
		switch (format) {
		case FORMAT_SERIALIZED_JSON:
			serializer = new JsonOmSerializer(out, process);
			break;
		case FORMAT_SERIALIZED_SMILE:
			serializer = new SmileOmSerializer(out, process);
			break;
		case FORMAT_SERIALIZED_JAVA:
			serializer = new JavaSerOmSerializer(out, process);
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
		FORMAT_UNINITIALIZED(0x00),
		FORMAT_SERIALIZED_JSON(0x10),
		FORMAT_SERIALIZED_SMILE(0x11),
		FORMAT_SERIALIZED_JAVA(0x20);
		
		private int code;
		private SerializeFormat(int code){
			this.code = code;
		}
		
		public int encode(){
			return code;
		}
		public SerializeFormat decode(int c){
			switch (c) {
			case 0x10:
				return FORMAT_SERIALIZED_JSON;
			case 0x11:
				return FORMAT_SERIALIZED_SMILE;
			case 0x20:
				return FORMAT_SERIALIZED_JAVA;
			default:
				return FORMAT_UNINITIALIZED;
			}
		}
	}
}
