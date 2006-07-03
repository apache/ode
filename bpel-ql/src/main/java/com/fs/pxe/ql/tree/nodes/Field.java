package com.fs.pxe.ql.tree.nodes;

public class Field implements Identifier {
    private final String name;

    
    /**
     * @param name
     */
    public Field(final String name) {
        super();
        this.name = name;
    }


    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
    
    
}
