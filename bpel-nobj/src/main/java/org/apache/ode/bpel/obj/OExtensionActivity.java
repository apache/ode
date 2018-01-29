/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.ode.bpel.obj;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Compiled representation of the BPEL <code>&lt;extensionActivity&gt;</code> activity.
 * <p>
 * Adapted initial version for compatibility with new ODE object model (bpel-nobj).
 * 
 * @author Tammo van Lessen (University of Stuttgart), Michael Hahn (mhahn.dev@gmail.com)
 */
public class OExtensionActivity extends OActivity implements Serializable {

    static final long serialVersionUID = -1L;

    private static final String EXTENSIONNAME = "extensionName";
    private static final String NESTEDELEMENT = "nestedElement";
    private static final String CHILDREN = "children";

    @JsonCreator
    public OExtensionActivity() {}

    public OExtensionActivity(OProcess owner, OActivity parent) {
        super(owner, parent);
        setChildren(new ArrayList<OActivity>());
    }

    @JsonIgnore
    public QName getExtensionName() {
        Object o = fieldContainer.get(EXTENSIONNAME);
        return o == null ? null : (QName) o;
    }

    @JsonIgnore
    public String getNestedElement() {
        Object o = fieldContainer.get(NESTEDELEMENT);
        return o == null ? null : (String) o;
    }

    @SuppressWarnings("unchecked")
    @JsonIgnore
    public List<OActivity> getChildren() {
        Object o = fieldContainer.get(CHILDREN);
        return o == null ? null : (List<OActivity>) o;
    }

    public void setExtensionName(QName extensionName) {
        fieldContainer.put(EXTENSIONNAME, extensionName);
    }

    public void setNestedElement(String nestedElement) {
        fieldContainer.put(NESTEDELEMENT, nestedElement);
    }

    void setChildren(List<OActivity> children) {
        if (getChildren() == null) {
            fieldContainer.put(CHILDREN, children);
        }
    }

    public String toString() {
        return "{OExtensionActivity; " + getExtensionName() + "}";
    }

    @Override
    public void dehydrate() {
        super.dehydrate();
        setExtensionName(null);
        setNestedElement(null);
        for (OBase obase : getChildren()) {
            obase.dehydrate();
        }
        getChildren().clear();
    }
}
