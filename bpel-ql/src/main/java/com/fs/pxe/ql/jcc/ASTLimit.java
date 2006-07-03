package com.fs.pxe.ql.jcc;

public class ASTLimit extends SimpleNode {
    private int number;
    
    public ASTLimit(int id) {
        super(id);
    }

    /**
     * @return the number
     */
    public int getNumber() {
        return number;
    }

    /**
     * @param number the number to set
     */
    public void setNumber(int number) {
        this.number = number;
    }
    
    /**
     * @param number the number to set
     */
    public void setNumber(String number) {
        this.number = Integer.parseInt(number);
    }
    
}
