package org.apache.ode.ql.jcc;

public class ASTField extends SimpleNode {
    protected String name;

    public ASTField(int id) {
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
