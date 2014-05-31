package org.apache.ode.bpel.obj;

public class OBase {
	private int version;
	private int id;
	public void setVersion(int version){
		this.version = version;
	}
	public int getVersion() {
		return version;
	}
	public int getId() {
		return id;
	}
	public void setId(int _id) {
		this.id = _id;
	}
}
