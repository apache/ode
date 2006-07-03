package com.fs.pxe.ql.jcc;

public class ASTOrderBy extends SimpleNode {
    protected String name;

    public ASTOrderBy(int id) {
        super(id);
    }
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    
}
