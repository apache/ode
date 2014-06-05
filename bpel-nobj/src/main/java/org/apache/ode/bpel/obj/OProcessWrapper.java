package org.apache.ode.bpel.obj;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Serializer using jackson
 * 
 * @author fangzhen
 */
public class OProcessWrapper extends OBaseExtensible{
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
	
	public OProcessWrapper(){
		super(new LinkedHashMap<String, Object>());
	}
	public OProcessWrapper(long compileTime){
		fieldContainer = new LinkedHashMap<String, Object>();
		fieldContainer.put(OProcessWrapper.MAGIC_NUMBER, OProcessWrapper.CURRENT_MAGIC_NUMBER);
		fieldContainer.put(OProcessWrapper.FORMAT, Serializer.FORMAT_SERIALIZED_DEFAULT);
		fieldContainer.put(OProcessWrapper.COMPILE_TIME, new Long(compileTime));
		fieldContainer.put(OProcessWrapper.OTHER_HEADERS, new LinkedHashMap<>()); //place holder
	}
	
	public void setOProcess(OProcess process){
//		map.put(GUID, process.guid);
//		map.put(TYPE, new QName(process.targetNamespace,
//				process.processName));
		fieldContainer.put(OProcessWrapper.PROCESS, process.getMapRepr());
	}

	public void checkValid() throws OModelException{
		// TODO Auto-generated method stub
		
	}


}
