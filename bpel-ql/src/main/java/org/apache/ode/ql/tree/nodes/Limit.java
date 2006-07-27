package org.apache.ode.ql.tree.nodes;

public class Limit implements Node {
    private int number;

    /**
     * @param number
     */
    public Limit(int number) {
        super();
        this.number = number;
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
    
    
}
