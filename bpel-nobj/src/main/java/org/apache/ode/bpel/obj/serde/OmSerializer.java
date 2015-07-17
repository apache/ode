package org.apache.ode.bpel.obj.serde;

import org.apache.ode.bpel.obj.OProcess;


public interface OmSerializer {
	/**
	 * Serialize the {@link OProcess} instance
	 * @throws SerializaionRtException
	 * @see OmSerdeFactory
	 */
	public void serialize() throws SerializaionRtException;
}
