/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.o;

import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * XSD-typed variable type.
 */
public class OXsdTypeVarType extends OVarType {
	private static final long serialVersionUID = 1L;

	public QName xsdType;

	public boolean simple;

	public OXsdTypeVarType(OProcess owner) {
		super(owner);
	}

	public Node newInstance(Document doc) {
		if (simple)
			return doc.createTextNode("");
		else
			return doc.createElement("xsd-complex-type-wrapper");
	}
}
