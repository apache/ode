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

package org.apache.ode.bpel.common;

import java.io.Serializable;
import java.util.ArrayList;

import org.apache.ode.utils.CollectionUtils;

/**
 * An instance of this class represents a correlation key that has a 'join' use case in the process definition.
 * For instance, if a correlation set, 'orderId' is used as initiate="no" in 3 occurrences and initiate="join"
 * in 1 occurrence, any correlation keys that are derived from the correlation set, 'orderId' is an
 * optional correlation key.
 *
 * @author sean
 *
 */
public class OptionalCorrelationKey extends CorrelationKey implements Serializable {
    private static final long serialVersionUID = 1L;

    public OptionalCorrelationKey(String csetName, String[] keyValues) {
        super(csetName, keyValues);
    }

    public OptionalCorrelationKey(String canonicalForm) {
        super(canonicalForm);
    }

    /**
     * @see Object#toString
     */
    public String toString() {
        StringBuffer buf = new StringBuffer("{OptionalCorrelationKey ");
        buf.append("setName=");
        buf.append(getCorrelationSetName());
        buf.append(", values=");
        buf.append(CollectionUtils.makeCollection(ArrayList.class, getValues()));
        buf.append('}');

        return buf.toString();
    }
}