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

public class OrderByElement<ID extends Identifier> implements Node {
    private static final long serialVersionUID = -3927580891987350265L;
      
    protected final ID identifier;
    protected final OrderByType type;
    
    /**
     * @param identifier
     */
    public OrderByElement(final ID identifier) {
        this(identifier, OrderByType.ASC);
    }
    /**
     * @param identifier
     * @param type
     */
    public OrderByElement(final ID identifier, final OrderByType type) {
        super();
        this.identifier = identifier;
        this.type = type;
    }
    /**
     * @return the identifier
     */
    public ID getIdentifier() {
        return identifier;
    }
    /**
     * @return the type
     */
    public OrderByType getType() {
        return type;
    }
    
    
}
