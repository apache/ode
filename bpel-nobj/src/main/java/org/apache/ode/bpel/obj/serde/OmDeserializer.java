package org.apache.ode.bpel.obj.serde;

import org.apache.ode.bpel.obj.OProcess;

public interface OmDeserializer {
	/**
	 * Deserialize to process instance. Instances should be 
	 * created and built by {@link OmSerdeFactory}
	 * @return Deserialized {@link OProcess}.
	 * @throws SerializaionRtException
	 */
	public OProcess deserialize() throws SerializaionRtException;
}
