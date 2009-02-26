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
package org.apache.ode.bpel.compiler.bom;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;

/**
 * Base class for nodes (such as {@link Activity} and {@link Process}
 * that can suppress join failure.
 */
public class JoinFailureSuppressor extends BpelObject {

    public JoinFailureSuppressor(Element el) {
        super(el);
    }



    public static final Map<String, SuppressJoinFailure>__suppressJoinFailure = new HashMap<String, SuppressJoinFailure>();
    static {
        __suppressJoinFailure.put("yes", SuppressJoinFailure.YES);
        __suppressJoinFailure.put("no", SuppressJoinFailure.NO);
        __suppressJoinFailure.put("",SuppressJoinFailure.NOTSET);
    }
    
    public enum SuppressJoinFailure {
        /**
         * Model element does not specify a <code>suppressJoinFailure</code>
         * override.
         */
        NOTSET,

        /**
         * Model element overrides <code>suppressJoinFailure</code> to
         * <code>no</code>.
         */
        NO,

        /**
         * Model element overrides <code>suppressJoinFailure</code> to
         * <code>no</code>.
         */
        YES
    }
    
    

    /**
     * Get the suppress join failure flag.
     * 
     * @return suppress join failure flag code
     */
    public SuppressJoinFailure getSuppressJoinFailure() {
        return getAttribute("suppressJoinFailure",__suppressJoinFailure, SuppressJoinFailure.NOTSET);
    }




}
