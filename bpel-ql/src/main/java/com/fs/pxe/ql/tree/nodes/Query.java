package com.fs.pxe.ql.tree.nodes;

import java.util.Collection;

public class Query implements Node {
    private final Collection<Object> childs;
    private final OrderBy order;
    private Limit limit;
    
    
    /**
     * @param childs
     * @param order
     * @param limit
     */
    public Query(final Collection<Object> childs, final OrderBy order, Limit limit) {
      super();
      this.childs = childs;
      this.order = order;
      this.limit = limit;
    }


    /**
     * @param childs
     * @param order
     */
    public Query(final Collection<Object> childs, final OrderBy order) {
        super();
        this.childs = childs;
        this.order = order;
    }


    /**
     * @param childs
     */
    public Query(final Collection<Object> childs) {
        this(childs, null);
    }


    /**
     * @return the childs
     */
    public Collection<Object> getChilds() {
        return childs;
    }


    /**
     * @return the order
     */
    public OrderBy getOrder() {
        return order;
    }


    /**
     * @return the limit
     */
    public Limit getLimit() {
        return limit;
    }


    /**
     * @param limit the limit to set
     */
    public void setLimit(Limit limit) {
        this.limit = limit;
    }
}
