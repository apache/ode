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
package org.apache.ode.bpel.rtrep.v2;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Base class for variable types. 
 */
public abstract class OVarType extends OBase {

    static final long serialVersionUID = 3785545734056078041L;

    public static int SCHEMA_TYPE = 0;
    public static int NUMBER_TYPE = 1;
    public static int STRING_TYPE = 2;
    public static int BOOLEAN_TYPE = 3;

    public int underlyingType = SCHEMA_TYPE;
    
    public OVarType(OProcess owner) {
        super(owner);
    }
    
    /**
     * Create a new instance of this variable.
     * @return a "skeleton" representation of this variable
     */
    public abstract Node newInstance(Document doc);  
  
    
}
