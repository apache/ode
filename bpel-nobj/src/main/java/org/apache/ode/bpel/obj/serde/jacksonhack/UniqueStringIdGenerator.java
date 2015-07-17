package org.apache.ode.bpel.obj.serde.jacksonhack;

import java.util.UUID;

import com.fasterxml.jackson.annotation.ObjectIdGenerator;

/**
 * Copied from jackson {@link com.fasterxml.jackson.annotation.ObjectIdGenerators.UUIDGenerator}
 *  and changed id type from UUID to String
 */
public class UniqueStringIdGenerator extends ObjectIdGenerator<String>{
    private static final long serialVersionUID = 1L;
    protected final Class<?> _scope;
    public UniqueStringIdGenerator() { this(Object.class); }
    private UniqueStringIdGenerator(Class<?> scope) {
    	_scope = scope;
    }

    /**
     * Can just return base instance since this is essentially scopeless
     */
    @Override
    public ObjectIdGenerator<String> forScope(Class<?> scope) {
        return this;
    }
    
    /**
     * Can just return base instance since this is essentially scopeless
     */
    @Override
    public ObjectIdGenerator<String> newForSerialization(Object context) {
        return this;
    }

    @Override
    public String generateId(Object forPojo) {
        return UUID.randomUUID().toString();
    }

    @Override
    public IdKey key(Object key) {
        return new IdKey(getClass(), null, key);
    }

    /**
     * Since UUIDs are always unique, let's fully ignore scope definition
     */
    @Override
    public boolean canUseFor(ObjectIdGenerator<?> gen) {
        return (gen.getClass() == getClass());
    }
	@Override
	public Class<?> getScope() {
		return _scope;
	}
}
