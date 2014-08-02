package org.apache.ode.bpel.compiler_2_0;

import org.apache.ode.bpel.obj.serde.OmSerdeFactory;

public class SmileSerializationTest extends JavaSerializationTest{
	public SmileSerializationTest(){
		this.format = OmSerdeFactory.SerializeFormat.FORMAT_SERIALIZED_SMILE;
		this.pathSuffix = "smile";

	}
}
