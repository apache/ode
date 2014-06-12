package org.apache.ode.bpel.obj.serde;

import java.io.IOException;

import org.apache.ode.bpel.obj.OProcessWrapper;

public interface OmDeserializer {
	public OProcessWrapper deserialize() throws IOException, SerializaionRtException;
}
