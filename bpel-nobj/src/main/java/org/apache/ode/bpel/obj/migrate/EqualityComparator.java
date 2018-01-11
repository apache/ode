package org.apache.ode.bpel.obj.migrate;

/**
 * Used by {@link DeepEqualityHelper} for custom comparator. 
 */
public interface EqualityComparator {
	/**
	 * return true if specified obj1 and obj2 are equal, false otherwise.
	 */
	Boolean objectsEqual(Object obj1, Object obj2);
	
	/**
	 * decide if the comparator can handle the specified object.
	 * Usually called before {@link EqualityComparator#objectsEqual(Object, Object)} are invoked.
	 */
	Boolean canHandle(Object obj);
	
	void setDeepEquality(DeepEqualityHelper deepEquality);
}
