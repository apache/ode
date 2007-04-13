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

import java.util.List;
import java.util.Set;

/**
 * BPEL Object Model (BOM) representation of a BPEL process.
 */
public class Process extends Scope {

    public enum Version {
        BPEL11,
        BPEL20_DRAFT,
        BPEL20
    }

    public Process(Element el) {
        super(el);
    }

    /**
     * Get the name of the process.
     * 
     * @return name of the process
     */
    public String getName() {
        return getAttribute("name",null);
    }

    /**
     * Get the BPEL version of this process.
     */
    public Version getBpelVersion() {
        if (is11()) return Version.BPEL11;
        if (is20Draft()) return Version.BPEL20_DRAFT;
        return Version.BPEL20;
    }

    /**
     * Get the root, process-level activity.
     * 
     * @return root process-level activity
     */
    public Activity getRootActivity() {
        return getFirstChild(Activity.class);
    }


    /**
     * Get the URL of the BPEL source document as a String.
     * 
     * @return BPEL source URL.
     */
    public String getSource() {
        return "todo";
    }

    /**
     * Get the process' target namespace.
     * 
     * @return process' target namespace
     */
    public String getTargetNamespace() {
        return getAttribute("targetNamespace", null);
    }

    /**
     * Get the default query language.
     * 
     * @return the default query language.
     */
    public String getQueryLanguage() {
        return getAttribute("queryLanguage", null);
    }

    /**
     * Get the default expression language.
     * 
     * @return default expression language
     */
    public String getExpressionLanguage() {
        return getAttribute("expressionLanguage", null);
    }

    /**
     * Get the <code>&lt;import&gt;</code>(s) of the process.
     * 
     * @return {@link Set} of {@link Import}s
     */
    public List<Import> getImports() {
        return getChildren(Import.class);
    }

}
