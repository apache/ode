package org.apache.ode.bpel.obj.serde;

import java.io.InputStream;
import java.io.ObjectInputStream;

import org.apache.ode.bpel.obj.OProcess;

/**
 * OModel deserializer that use java serializtion mechanism.
 * Corresponding to format {@link OmSerdeFactory.SerializeFormat#FORMAT_SERIALIZED_JAVA}
 * @see JavaSerOmSerializer
 */
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
