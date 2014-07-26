package org.apache.ode.bpel.obj.serde;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.apache.ode.bpel.obj.OProcessWrapper;

public class JavaSerOmSerializer implements OmSerializer {
	private OutputStream out;
	private OProcessWrapper wrapper;
	public JavaSerOmSerializer(OutputStream out, OProcessWrapper wrapper) {
		this.wrapper = wrapper;
		this.out = out;
	}

	@Override
	public void serialize() throws SerializaionRtException, IOException {
		ObjectOutputStream oos = new ObjectOutputStream(out);
		oos.writeObject(wrapper);
	}

}
