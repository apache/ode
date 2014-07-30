package org.apache.ode.bpel.obj.serde;

import java.io.OutputStream;

import org.apache.ode.bpel.obj.OProcess;

import com.fasterxml.jackson.dataformat.smile.SmileFactory;

public class SmileOmSerializer extends JsonOmSerializer{
	public SmileOmSerializer(){
		super();
		factory = new SmileFactory();
	}
	public SmileOmSerializer(OutputStream out, OProcess process) {
		super(out, process, new SmileFactory());
	}
}
