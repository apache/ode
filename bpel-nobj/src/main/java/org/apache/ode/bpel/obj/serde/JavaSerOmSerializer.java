package org.apache.ode.bpel.obj.serde;

import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.apache.ode.bpel.obj.OProcess;

/**
 * OModel Serializer that use java serializtion mechanism. 
 * Corresponding to format {@link OmSerdeFactory.SerializeFormat#FORMAT_SERIALIZED_JAVA}
 * @see JavaSerOmDeserializer
 */
public class JavaSerOmSerializer implements OmSerializer {
	private OutputStream out;
	OProcess process;
	public JavaSerOmSerializer(OutputStream out, OProcess process) {
		this.process = process;
		this.out = out;
	}

	@Override
	public void serialize() throws SerializaionRtException {
		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(out);
			oos.writeObject(process);
		} catch (Exception e) {
			SerializaionRtException se = new SerializaionRtException("error when serialize process");
			se.initCause(e);
			throw se;
		}
	}

}
