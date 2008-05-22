/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.ode.ql.tree.nodes;

import java.util.Collection;

public class Query implements Node {
    private static final long serialVersionUID = -1889820969391077174L;
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
