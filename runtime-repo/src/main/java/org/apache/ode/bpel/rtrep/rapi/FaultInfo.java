package org.apache.ode.bpel.rtrep.rapi;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

public interface FaultInfo {

	QName getFaultName();

	String getExplanation();

	int getFaultLineNo();

	int getActivityId();

	Element getFaultMessage();

}
