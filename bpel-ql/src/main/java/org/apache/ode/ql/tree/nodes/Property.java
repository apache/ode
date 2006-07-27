package org.apache.ode.ql.tree.nodes;

import javax.xml.namespace.QName;

public class Property implements Identifier {
    private final QName name;

    
    /**
     * @param name
     */
    public Property(final QName name) {
        super();
        this.name = name;
    }

    public String getNamespace() {
        return name.getNamespaceURI();
    }
    /**
     * @return the name
     */
    public String getName() {
        return name.getLocalPart();
    }
    
}
