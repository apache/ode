package org.apache.ode.bpel.o;

import org.apache.ode.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * @author Matthieu Riou <mriou at apache dot org>
 */
public class OConstantVarType extends OVarType {
    private String value;
    private transient Node nodeValue;

    public OConstantVarType(OProcess owner, Node value) {
        super(owner);
        this.value = DOMUtils.domToString(value);
    }

    public Node newInstance(Document doc) {
        return getValue();
    }

    public Node getValue() {
        if (nodeValue == null)
            try {
                nodeValue = DOMUtils.stringToDOM(value);
            } catch (Exception e) {
                // Highly unexpected
                throw new RuntimeException(e);
            }
        return nodeValue;
    }
}
