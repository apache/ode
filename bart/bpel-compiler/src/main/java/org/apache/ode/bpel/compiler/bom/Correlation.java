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
 * Representaiton of a BPEL <code>&lt;correlation&gt;</code> modifier. A
 * correlation is a tuple consisting of a communication element (such as an
 * invoke, receive, or onMessage) a correlation set, and an initiate flag
 */
public class Correlation extends BpelObject {

    public Correlation(Element el) {
        super(el);
    }

    public enum Initiate {
        YES, NO, JOIN, UNSET;

     private static final Map<String, Initiate> __map = new HashMap<String, Initiate>();
     static {
         __map.put("yes", YES);
         __map.put("no", NO);
         __map.put("join", JOIN);
         __map.put("rendezvous", JOIN); // BPEL 1.1
         __map.put("", UNSET);
     }
    }

    public enum CorrelationPattern {
        IN, OUT, INOUT, UNSET;
        
        private static final Map<String, CorrelationPattern> __map = new HashMap<String, CorrelationPattern>();
        static {
            __map.put("in", IN);
            __map.put("response", IN);
            __map.put("out", OUT);
            __map.put("request", OUT); 
            __map.put("in-out", INOUT);
            __map.put("out-in", INOUT);
            __map.put("request-response", INOUT);
            __map.put("", UNSET);
        }
    }

    

    /**
     * Get the name of the referenced correlation set.
     * 
     * @return correlation set
     */
    public String getCorrelationSet() {
        return getAttribute("set", null);
    }

    /**
     * Get the value of the initiate flag.
     * 
     * @return one of <code>{@link Correlation}.INITATE_XXX</code> constants
     */
    public Initiate getInitiate() {
        return getAttribute("initiate", Initiate.__map, Initiate.UNSET);
    }

    /**
     * Get the correlation pattern.
     * 
     * @return the correlation pattern, one of:
     *         <ul>
     *         <li>{@link #CORRPATTERN_IN}</li>
     *         <li>{@link #CORRPATTERN_OUT}</li>
     *         <li>{@link #CORRPATTERN_INOUT}</li>
     *         </ul>
     */
    public CorrelationPattern getPattern() {
        return getAttribute("pattern", CorrelationPattern.__map, CorrelationPattern.UNSET);
        
    }

}
