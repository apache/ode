package org.apache.ode.bpel.obj;

public interface Extensible {
	public <T> void addField(String fieldName, T value);
	public <T> T getField(String fieldName);
}
