package com.fs.pxe.ql.tree.nodes;

import java.util.Collection;

public class OrderBy implements Node {
    private final Collection<OrderByElement> orders;

    
    /**
     * @param orders
     */
    public OrderBy(final Collection<OrderByElement> orders) {
        super();
        this.orders = orders;
    }


    /**
     * @return the orders
     */
    public Collection<OrderByElement> getOrders() {
        return orders;
    }
    
    
}
