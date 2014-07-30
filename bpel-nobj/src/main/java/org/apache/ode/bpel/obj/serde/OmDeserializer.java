package org.apache.ode.bpel.obj.serde;

import org.apache.ode.bpel.obj.OProcess;

public interface OmDeserializer {
	public OProcess deserialize() throws SerializaionRtException;
}
