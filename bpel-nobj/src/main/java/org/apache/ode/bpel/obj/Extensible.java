package org.apache.ode.bpel.obj;

public interface Extensible {
	/**
	 * Add or update a field to the extensible object.
	 * 
	 * @param fieldName
	 * @param value
	 */
	public <T> void addField(String fieldName, T value);

	/**
	 * Delete a field with given name. Return true if the field are found and
	 * deleted successfully.
	 * 
	 * @param fieldName
	 * @return
	 */
	public boolean delField(String fieldName);
	
	/**
	 * Return field value of given field name
	 * @param fieldName
	 * @return
	 */
	public <T> T getField(String fieldName);
}
