package org.apache.ode.bpel.rapi;

import javax.xml.namespace.QName;

public interface Variable extends ScopedObject {

	String getName();

    String getExternalId();

    QName getElementType();
	
}
