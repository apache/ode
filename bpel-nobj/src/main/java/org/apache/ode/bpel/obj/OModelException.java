package org.apache.ode.bpel.obj;

import java.io.Serializable;

public class OModelException extends Exception  implements Serializable{
	private static final long serialVersionUID = -3148845461985443106L;
	public OModelException(String message){
		super(message);
	}
}
