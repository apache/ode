package org.apache.ode.bpel.obj;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedHashMap;

import javax.xml.namespace.QName;

import org.apache.ode.bpel.obj.OProcess;

import sun.java2d.pipe.hw.ExtendedBufferCapabilities;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * Serializer using jackson
 * 
 * @author fangzhen
 */
public class OProcessWrapper implements Extensible{

	// constants
	public static final byte[] MAGIC_NUMBER_OFH_20140529 = new byte[] { 0x55,
			'5', 'S', 0x00, 'O', 'F', 'H', 0x20, 0x14, 0x05, 0x29 };
	public static final byte[] CURRENT_MAGIC_NUMBER = MAGIC_NUMBER_OFH_20140529;
	// key constants
	public static final String MAGIC_NUMBER = "MAGIC";
	public static final String FORMAT = "FORMAT";
	public static final String COMPILE_TIME = "COMPILE_TIME";
	public static final String GUID = "GUID";
	public static final String TYPE = "TYPE";
	public static final String PROCESS = "PROCESS";
	public static final String OTHER_HEADERS = "OTHER_HEADERS";
	/** the wrapper wraps the whole process and other data */
	private LinkedHashMap<String, Object> map;
	
	public OProcessWrapper(){
		map = new LinkedHashMap<>();
	}
	public OProcessWrapper(long compileTime){
		map = new LinkedHashMap<String, Object>();
		map.put(OProcessWrapper.MAGIC_NUMBER, OProcessWrapper.CURRENT_MAGIC_NUMBER);
		map.put(OProcessWrapper.FORMAT, Serializer.FORMAT_SERIALIZED_DEFAULT);
		map.put(OProcessWrapper.COMPILE_TIME, new Long(compileTime));
		map.put(OProcessWrapper.OTHER_HEADERS, new LinkedHashMap<>()); //place holder
	}
	
	public void setOProcess(OProcess process){
//		map.put(GUID, process.guid);
//		map.put(TYPE, new QName(process.targetNamespace,
//				process.processName));
		map.put(OProcessWrapper.PROCESS, process);
	}
	public LinkedHashMap<String, Object> getMap() {
		return map;
	}
	public void setMap(LinkedHashMap<String, Object> map) {
		this.map = map;
	}
	public boolean checkValid() {
		// TODO Auto-generated method stub
		return true;
	}


}
