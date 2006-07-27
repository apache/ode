package org.apache.ode.ql.tree.nodes;

import java.util.Collection;

public class In implements LogicNode {
    private final Identifier identifier;
    private final Collection<Value> values;
    
    
    /**
     * @param identifier
     * @param values
     */
    public In(final Identifier identifier, final Collection<Value> values) {
        super();
        this.identifier = identifier;
        this.values = values;
    }
    /**
     * @return the identifier
     */
    public Identifier getIdentifier() {
        return identifier;
    }
    /**
     * @return the values
     */
    public Collection<Value> getValues() {
        return values;
    }
    
    
    
}
