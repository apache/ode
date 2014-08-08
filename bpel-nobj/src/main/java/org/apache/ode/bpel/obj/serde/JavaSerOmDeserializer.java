package org.apache.ode.bpel.obj.serde;

import java.io.InputStream;
import java.io.ObjectInputStream;

import org.apache.ode.bpel.obj.OProcess;
import org.apache.ode.bpel.obj.migrate.ObjectTraverser;
import org.apache.ode.bpel.obj.migrate.OmUpgradeVisitor;

public class JavaSerOmDeserializer implements OmDeserializer {
	private InputStream is;
	public JavaSerOmDeserializer(InputStream is) {
		this.is = is;
	}

	@Override
	public OProcess deserialize() throws SerializaionRtException {
		ObjectInputStream ois;
		try {
			ois = new ObjectInputStream(is);
			OProcess process;
			process = (OProcess)ois.readObject();
			return process;
		} catch (Exception e) {
			SerializaionRtException se = new SerializaionRtException("error when deserializing process");
			se.initCause(e);
			throw se;
		}
	}

}
