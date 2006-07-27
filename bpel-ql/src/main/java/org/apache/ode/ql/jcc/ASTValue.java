package org.apache.ode.ql.jcc;

public class ASTValue extends SimpleNode {
    protected String value;

    public ASTValue(int id) {
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
        //TODO
        this.value = value.substring(1, value.length()-1);
    }
    
}
