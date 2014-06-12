package org.apache.ode.bpel.obj.serde;

import java.io.InputStream;

import com.fasterxml.jackson.dataformat.smile.SmileFactory;

public class SmileOmDeserializer extends JsonOmDeserializer {
	public SmileOmDeserializer(){
		super();
		factory = new SmileFactory();
	}
	
	public SmileOmDeserializer(InputStream is){
		super(is, new SmileFactory());
	}
}
