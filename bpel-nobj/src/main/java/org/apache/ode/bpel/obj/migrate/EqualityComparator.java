package org.apache.ode.bpel.obj.migrate;

public interface EqualityComparator {

	Boolean objectsEqual(Object obj1, Object obj2);
	Boolean canHanle(Object obj);
}
