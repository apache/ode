/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.o;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Message variable type.
 */
public class OMessageVarType extends OVarType {
  public QName messageType;
  public final Map<String, Part> parts = new HashMap<String,Part>();

  /** For doc-lit-like message types , the element type of the only part. */
  public final OElementVarType docLitType;

  public OMessageVarType(OProcess owner, QName messageType, Collection<Part> parts) {
    super(owner);
    this.messageType = messageType;
    for (Iterator<Part> i = parts.iterator(); i.hasNext();) {
      Part part = i.next();
      this.parts.put(part.name,part);
    }

    if ((parts.size() == 1 && parts.iterator().next().type instanceof OElementVarType))
      docLitType = (OElementVarType) parts.iterator().next().type;
    else
      docLitType = null;

  }

  boolean isDocLit() { return docLitType != null; }


	public Node newInstance(Document doc) {
		Element el = doc.createElementNS(null, "message");
		for(OMessageVarType.Part part : parts.values()){
			Element partElement = doc.createElementNS(null, part.name);
			partElement.appendChild(part.type.newInstance(doc));
      el.appendChild(partElement);
    }
		return el;
	}

  public static class Part extends OBase {
    public String name;
    public OVarType type;

    public Part(OProcess owner, String partName, OVarType partType) {
      super(owner);
      this.name = partName;
      this.type = partType;
    }

  }



}
