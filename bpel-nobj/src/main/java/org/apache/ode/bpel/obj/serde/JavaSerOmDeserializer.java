package org.apache.ode.bpel.obj.serde;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

import javax.xml.namespace.QName;

import org.apache.ode.bpel.obj.OProcess;
import org.apache.ode.bpel.obj.OProcessWrapper;

public class JavaSerOmDeserializer implements OmDeserializer {
	private InputStream is;
	OProcessWrapper wrapper;
	public JavaSerOmDeserializer(InputStream is) {
		this.is = is;
	}
	
	@Override
	public QName getType(){
		return wrapper.getType();
	}
	@Override
	public String getGuid(){
		return wrapper.getGuid();
	}
	@Override
	public OProcess deserialize() throws IOException,
			SerializaionRtException {
		ObjectInputStream ois = new ObjectInputStream(is);
		OProcessWrapper wrapper = null;
		try {
			wrapper = (OProcessWrapper)ois.readObject();
		} catch (ClassNotFoundException e) {
			SerializaionRtException se = new SerializaionRtException("error when deserializing process");
			se.initCause(e);
			throw se;
		}
		this.wrapper = wrapper;
		return wrapper.getProcess();
	}

}
