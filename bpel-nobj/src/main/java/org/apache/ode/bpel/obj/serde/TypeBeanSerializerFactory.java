package org.apache.ode.bpel.obj.serde;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.cfg.SerializerFactoryConfig;
import com.fasterxml.jackson.databind.ser.BeanSerializerFactory;

public class TypeBeanSerializerFactory extends BeanSerializerFactory{

	private static final long serialVersionUID = 8155473476177879404L;
	public final static TypeBeanSerializerFactory instance = new TypeBeanSerializerFactory(null);

	protected TypeBeanSerializerFactory(SerializerFactoryConfig config) {
		super(config);
	}
	
	@Override
	protected TypeBeanSerializerBuilder constructBeanSerializerBuilder(BeanDescription beanDesc) {
        return new TypeBeanSerializerBuilder(beanDesc);
    }
}
