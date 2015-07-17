package org.apache.ode.bpel.obj.migrate;

/**
 * Object visitor used when traverse an object.
 * @author fangzhen
 *
 */
public interface ObjectVisitor {
	/**
	 * Operation if obj has been visited before 
	 * @return we may need to return new object corresponding to the visiting object.
	 */
	Object visited(Object obj);

	/**
	 * Operation when obj is first visited or its former wasn't recorded.
	 * @return we may need to return new object corresponding to the visiting object.
	 */
	Object visit(Object obj);

	void setTraverse(ObjectTraverser traverseObject);
	void addCustomVisitor(Class cls, ObjectVisitor visitor);
	ObjectTraverser getTraverse();
}
