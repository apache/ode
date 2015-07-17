package org.apache.ode.bpel.obj.serde.jacksonhack;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializer;
import com.fasterxml.jackson.databind.ser.BeanSerializerBuilder;
import com.fasterxml.jackson.databind.ser.impl.ObjectIdWriter;
import com.fasterxml.jackson.databind.ser.impl.WritableObjectId;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;

/**
 * This BeanSerializer differs the standard {@link BeanSerializer} when serialize
 * shared bean reference with object id. This class adds type info along with Object id.
 * The BeanSerializer seems not designed for extension, we have some redundant code here
 * copied from {@link BeanSerializer}.
 * @author fangzhen
 *
 */
public class TypeBeanSerializer extends BeanSerializer {

	public TypeBeanSerializer(JavaType type, BeanSerializerBuilder builder,
			BeanPropertyWriter[] properties,
			BeanPropertyWriter[] filteredProperties) {
		super(type, builder, properties, filteredProperties);
	}

	/**
	 * Alternate copy constructor that can be used to construct
	 * standard {@link BeanSerializer} passing an instance of
	 * "compatible enough" source serializer. Simply copied from BeanSerializer.
	 */
	protected TypeBeanSerializer(BeanSerializerBase src) {
		super(src);
	}

	protected TypeBeanSerializer(BeanSerializerBase src,
			ObjectIdWriter objectIdWriter) {
		super(src, objectIdWriter);
	}

	protected TypeBeanSerializer(BeanSerializerBase src,
			ObjectIdWriter objectIdWriter, Object filterId) {
		super(src, objectIdWriter, filterId);
	}

	protected TypeBeanSerializer(BeanSerializerBase src, String[] toIgnore) {
		super(src, toIgnore);
	}

	/**
	 * Convenient methods for new Serializer.
	 */
	@Override
	public BeanSerializerBase withObjectIdWriter(ObjectIdWriter objectIdWriter) {
		return new TypeBeanSerializer(this, objectIdWriter, _propertyFilterId);
	}

	@Override
	protected BeanSerializerBase withFilterId(Object filterId) {
		return new TypeBeanSerializer(this, _objectIdWriter, filterId);
	}

	@Override
	protected BeanSerializerBase withIgnorals(String[] toIgnore) {
		return new TypeBeanSerializer(this, toIgnore);
	}

	public static TypeBeanSerializer createDummy(JavaType forType) {
		return new TypeBeanSerializer(forType, null, NO_PROPS, null);
	}

	/**
	 * The method {@link BeanSerializer#_serializeWithObjectId(Object, JsonGenerator, 
	 * SerializerProvider,	TypeSerializer)} is final in BeanSerializer. As a 
	 * result, we cannot override it. But it actually overrides the method functionally. The differences
	 * is that this method write additional type info along with object id for shared reference. Then the
	 * deserializer can tell if it's a reference or ordinary object. 
	 * @param bean
	 * @param jgen
	 * @param provider
	 * @param typeSer
	 * @throws IOException
	 * @throws JsonGenerationException
	 */
	protected void _serializeWithObjectId2(Object bean,
			JsonGenerator jgen, SerializerProvider provider,
			TypeSerializer typeSer) throws IOException, JsonGenerationException {
		final ObjectIdWriter w = _objectIdWriter;
		WritableObjectId objectId = provider.findObjectId(bean, w.generator);

		if (objectId.id == null && w.alwaysAsId) {
			objectId.generateId(bean);
		}
		//don't know  why the field WrotableObjectId.isWritten is necessary. i didn't test it.
		if (objectId.id != null) {
			//write type info; assume that object id is scalar
			String typeStr = (_typeId == null) ? null : _customTypeId(bean);
			if (typeStr == null) {
				typeSer.writeTypePrefixForScalar(bean, jgen);
			} else {
				typeSer.writeCustomTypePrefixForScalar(bean, jgen, typeStr);
			}

			objectId.writeAsId(jgen, provider, w);
			if (typeStr == null) {
				typeSer.writeTypeSuffixForScalar(bean, jgen);
			} else {
				typeSer.writeCustomTypeSuffixForScalar(bean, jgen, typeStr);
			}
			return;
		}

		objectId.generateId(bean);
		_serializeObjectId(bean, jgen, provider, typeSer, objectId);
	}

	/**
	 * override this method to call our {@link #_serializeWithObjectId2(Object, JsonGenerator, SerializerProvider, TypeSerializer)}
	 *  instead of {@link BeanSerializer#_serializeWithObjectId(Object, JsonGenerator, SerializerProvider, TypeSerializer)}.
	 */
	@Override
	public void serializeWithType(Object bean, JsonGenerator jgen,
			SerializerProvider provider, TypeSerializer typeSer)
			throws IOException, JsonGenerationException {
		if (_objectIdWriter != null) {
			_serializeWithObjectId2(bean, jgen, provider, typeSer);
			return;
		}

		String typeStr = (_typeId == null) ? null : _customTypeId(bean);
		if (typeStr == null) {
			typeSer.writeTypePrefixForObject(bean, jgen);
		} else {
			typeSer.writeCustomTypePrefixForObject(bean, jgen, typeStr);
		}
		if (_propertyFilterId != null) {
			serializeFieldsFiltered(bean, jgen, provider);
		} else {
			serializeFields(bean, jgen, provider);
		}
		if (typeStr == null) {
			typeSer.writeTypeSuffixForObject(bean, jgen);
		} else {
			typeSer.writeCustomTypeSuffixForObject(bean, jgen, typeStr);
		}
	}
	private final String _customTypeId(Object bean) {
		final Object typeId = _typeId.getValue(bean);
		if (typeId == null) {
			return "";
		}
		return (typeId instanceof String) ? (String) typeId : typeId.toString();
	}

}
