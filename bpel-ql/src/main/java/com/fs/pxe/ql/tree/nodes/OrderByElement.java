package com.fs.pxe.ql.tree.nodes;

public class OrderByElement<ID extends Identifier> implements Node {
    protected final ID identifier;
    protected final OrderByType type;
    
    /**
     * @param identifier
     */
    public OrderByElement(final ID identifier) {
        this(identifier, OrderByType.ASC);
    }
    /**
     * @param identifier
     * @param type
     */
    public OrderByElement(final ID identifier, final OrderByType type) {
        super();
        this.identifier = identifier;
        this.type = type;
    }
    /**
     * @return the identifier
     */
    public ID getIdentifier() {
        return identifier;
    }
    /**
     * @return the type
     */
    public OrderByType getType() {
        return type;
    }
    
    
}
