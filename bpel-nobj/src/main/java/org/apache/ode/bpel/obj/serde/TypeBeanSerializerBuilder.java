package org.apache.ode.bpel.obj.serde;


import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializer;
import com.fasterxml.jackson.databind.ser.BeanSerializerBuilder;

public class TypeBeanSerializerBuilder extends BeanSerializerBuilder{
	 private final static BeanPropertyWriter[] NO_PROPERTIES = new BeanPropertyWriter[0];
	protected TypeBeanSerializerBuilder(BeanSerializerBuilder src) {
		super(src);
	}

	public TypeBeanSerializerBuilder(BeanDescription beanDesc) {
		super(beanDesc);
	}
	@Override
	public TypeBeanSerializer createDummy() {
        return TypeBeanSerializer.createDummy(_beanDesc.getType());
    }
	
	/**
     * Method called to create {@link BeanSerializer} instance with
     * all accumulated information. Will construct a serializer if we
     * have enough information, or return null if not.
     */
	@Override
    public JsonSerializer<?> build()
    {
        BeanPropertyWriter[] properties;
        // No properties, any getter or object id writer?
        // No real serializer; caller gets to handle
        if (_properties == null || _properties.isEmpty()) {
            if (_anyGetter == null && _objectIdWriter == null) {
                return null;
            }
            properties = NO_PROPERTIES;
        } else {
            properties = _properties.toArray(new BeanPropertyWriter[_properties.size()]);
        }
        return new TypeBeanSerializer(_beanDesc.getType(), this,
                properties, _filteredProperties);
    }
}
