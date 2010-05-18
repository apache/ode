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

import org.w3c.dom.Element;

/**
 * Assignment copy entry. Each copy entry consists of a "left hand side"
 * (L-value) and a "right hand side (R-value). The value on the right hand side
 * is copied to the location referenced in the left hand side.
 */
public class Copy extends BpelObject {

    public Copy(Element el) {
        super(el);
    }

    /**
     * Get the L-value.
     * 
     * @return the L-value.
     */
    public To getTo() {
        return getFirstChild(To.class);
    }

    /**
     * Get the R-value.
     * 
     * @return the R-value.
     */
    public From getFrom() {
        return getFirstChild(From.class);
    }

    public boolean isKeepSrcElement() {
        return getAttribute("keepSrcElementName", "no").equals("yes");
    }

    public boolean isIgnoreMissingFromData() {
        return getAttribute("ignoreMissingFromData", "no").equals("yes");
    }

    public boolean isIgnoreUninitializedFromVariable() {
        return getAttribute("ignoreUninitializedFromVariable", "no").equals("yes");
    }
    
    public boolean isInsertMissingToData() {
        return getAttribute("insertMissingToData", "no").equals("yes");
    }
}
