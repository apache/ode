package org.apache.ode.bpel.obj.serde;

import java.io.IOException;

import javax.xml.namespace.QName;

import org.apache.ode.bpel.obj.OProcess;
import org.apache.ode.bpel.obj.OProcessWrapper;

public interface OmDeserializer {
	public OProcess deserialize() throws IOException, SerializaionRtException;

	String getGuid();
	QName getType();
}
