package org.apache.ode.bpel.obj.serde;

import java.io.OutputStream;

import org.apache.ode.bpel.obj.OProcessWrapper;

import com.fasterxml.jackson.dataformat.smile.SmileFactory;

public class SmileOmSerializer extends JsonOmSerializer{
	public SmileOmSerializer(){
		super();
		factory = new SmileFactory();
	}
	public SmileOmSerializer(OutputStream out, OProcessWrapper wrapper) {
		super(out, wrapper, new SmileFactory());
	}
}
