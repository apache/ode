package com.fs.pxe.ql.tree.nodes;

public abstract class IdentifierToValueCMP implements LogicNode {
    private final Identifier identifier;
    private final Value value;
    
    
    /**
     * @param identifier
     * @param value
     */
    protected IdentifierToValueCMP(final Identifier identifier, final Value value) {
        super();
        this.identifier = identifier;
        this.value = value;
    }
    /**
     * @return the identifier
     */
    public Identifier getIdentifier() {
        return identifier;
    }
    /**
     * @return the value
     */
    public Value getValue() {
        return value;
    }
    

}
