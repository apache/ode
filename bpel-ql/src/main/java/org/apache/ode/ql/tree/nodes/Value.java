package org.apache.ode.ql.tree.nodes;

public class Value<V> implements Node {
    private V value;


    /**
     * @param value
     */
    public Value(final V value) {
        super();
        this.value = value;
    }


    /**
     * 
     */
    public Value() {
        super();
    }


    /**
     * @return the value
     */
    public V getValue() {
        return value;
    }


    /**
     * @param value the value to set
     */
    public void setValue(V value) {
        this.value = value;
    }
    

    
}
