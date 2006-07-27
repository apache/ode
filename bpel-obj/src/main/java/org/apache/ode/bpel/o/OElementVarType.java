/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.o;

import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class OElementVarType extends OVarType {
	private static final long serialVersionUID = 1L;

	public QName elementType;

	public OElementVarType(OProcess owner, QName typeName) {
		super(owner);
		elementType = typeName;
	}

	public Node newInstance(Document doc) {
		Element el = doc.createElementNS(elementType.getNamespaceURI(),
				elementType.getLocalPart());
		return el;
	}
}
