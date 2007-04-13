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
package org.apache.ode.bpel.elang.xpath10.compiler;

import java.net.URI;

import javax.xml.transform.SourceLocator;

import org.apache.ode.bpel.compiler.api.SourceLocation;

public class SourceLocatorWrapper implements SourceLocation {

    private SourceLocator _sloc;

    public SourceLocatorWrapper(SourceLocator sloc) {
        _sloc = sloc;
    }
    
    public int getColumnNo() {
        return _sloc.getColumnNumber();
    }

    public int getLineNo() {
        return _sloc.getLineNumber();
    }

    public String getPath() {
        return "";
    }

    public URI getURI() {
        try {
            return new URI(_sloc.getSystemId());
        } catch (Exception e) {
            return null;
        }
    }

}
