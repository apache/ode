package org.apache.ode.ql.jcc;

public class ASTOrderType extends SimpleNode {
    protected String value;

    public ASTOrderType(int id) {
        super(id);
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }
    
    
}
