package org.apache.ode.bpel.obj.serde;

import java.io.InputStream;

import com.fasterxml.jackson.dataformat.smile.SmileFactory;

/** OModel Serializer that corresponding to {@link OmSerdeFactory.SerializeFormat#FORMAT_SERIALIZED_SMILE}
  * @see JsonOmSerializer
*/
public class SmileOmDeserializer extends JsonOmDeserializer {
	public SmileOmDeserializer() {
		super();
		factory = new SmileFactory();
	}

	public SmileOmDeserializer(InputStream is) {
		super(is, new SmileFactory());
	}
}
