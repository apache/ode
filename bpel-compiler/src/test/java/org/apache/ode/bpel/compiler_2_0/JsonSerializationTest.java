package org.apache.ode.bpel.compiler_2_0;

import org.apache.ode.bpel.obj.serde.OmSerdeFactory;

public class JsonSerializationTest extends JavaSerializationTest{
	public JsonSerializationTest(){
		this.format = OmSerdeFactory.SerializeFormat.FORMAT_SERIALIZED_JSON;
		this.pathSuffix = "json";
	}
}
