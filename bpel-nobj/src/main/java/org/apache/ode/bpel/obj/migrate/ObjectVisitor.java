package org.apache.ode.bpel.obj.migrate;

public interface ObjectVisitor {

	Object visited(Object obj);

	Object visit(Object obj);

	void setTraverse(TraverseObject traverseObject);

	void addCustomVisitor(Class cls, ObjectVisitor visitor);
}
