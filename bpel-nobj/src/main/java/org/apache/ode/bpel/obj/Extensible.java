package org.apache.ode.bpel.obj;

/**
 * defines interfaces for add/del/get fields.
 *
 */
public interface Extensible<T> {
	/**
	 * Add or update a field to the extensible object.
	 * 
	 * @param fieldName
	 * @param value
	 */
	public void addField(String fieldName, T value);

	/**
	 * Delete and return a field with given name.
	 * 
	 * @param fieldName
	 * @return
	 */
	public T delField(String fieldName);
	
	/**
	 * Return field value of given field name
	 * @param fieldName
	 * @return
	 */
	public T getField(String fieldName);
}
