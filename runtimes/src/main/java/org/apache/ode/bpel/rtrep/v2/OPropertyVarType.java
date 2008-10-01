package org.apache.ode.bpel.rtrep.v2;

import org.w3c.dom.Node;
import org.w3c.dom.Document;

public class OPropertyVarType extends OVarType {
    
    public OPropertyVarType(OProcess owner) {
        super(owner);
    }

    public Node newInstance(Document doc) {
        return doc.createTextNode("");
    }
}
