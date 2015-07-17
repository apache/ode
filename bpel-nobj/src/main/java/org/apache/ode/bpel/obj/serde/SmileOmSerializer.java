package org.apache.ode.bpel.obj.serde;

import java.io.OutputStream;

import org.apache.ode.bpel.obj.OProcess;

import com.fasterxml.jackson.dataformat.smile.SmileFactory;

/**
 * OModel Serializer that corresponding to {@link OmSerdeFactory.SerializeFormat#FORMAT_SERIALIZED_SMILE}
 * @see JsonOmDeserializer
 */
public class SmileOmSerializer extends JsonOmSerializer{
	public SmileOmSerializer(){
		super();
		factory = new SmileFactory();
	}
	public SmileOmSerializer(OutputStream out, OProcess process) {
		super(out, process, new SmileFactory());
	}
}
