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
package org.apache.ode.bpel.o;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.io.ObjectInputStream.GetField;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

/**
 * Information about the source that was used to create a compiled object.
 */
public class DebugInfo implements Serializable {
    static final long serialVersionUID = -1L  ;

    /** Source file / resource name. */
    public String sourceURI;

    /** Source line number (start). */
    public int startLine;

    /** Source line number (end). */
    public int endLine;

    public String description;

    public HashMap<QName, Object> extensibilityElements = new HashMap<QName, Object>();

    public DebugInfo(String sourceURI, int startLine, int endLine, Map<QName, Object> extElmt) {
        this.sourceURI = sourceURI;
        this.startLine = startLine;
        this.endLine = endLine;
        if (extElmt != null && extElmt.size() > 0) {
            this.extensibilityElements = new HashMap<QName, Object>(extElmt);
        }
    }

    public DebugInfo(String sourceURI, int line, Map<QName, Object> extElmt) {
        this(sourceURI, line, line, extElmt);
    }

    /**
     * Do not load the description and extensibilityElements fields,
     * as they may be big and are not really required at run-time.
     * 
     * @param ois
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
    	GetField getField = ois.readFields();
    	sourceURI = (String) getField.get("sourceURI", null);
    	startLine = (Integer) getField.get("startLine", 0);
    	endLine = (Integer) getField.get("endLine", 0);
    	extensibilityElements = (HashMap<QName, Object>) getField.get("extensibilityElements", null);
    }

}
