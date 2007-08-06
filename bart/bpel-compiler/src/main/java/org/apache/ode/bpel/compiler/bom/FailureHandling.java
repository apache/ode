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
 * BOM representation of the failure handling settings extensibility element.
 */
public class FailureHandling extends BpelObject {

    public FailureHandling(Element el) {
        super(el);
    }

    public int getRetryFor() {
        RetryFor element = getFirstChild(RetryFor.class);
        return element == null ? 0 : element.getValue();
    }

    public int getRetryDelay() {
        RetryDelay element = getFirstChild(RetryDelay.class);
        return element == null ? 0 : element.getValue();
    }

    public boolean getFaultOnFailure() {
        FaultOnFailure element = getFirstChild(FaultOnFailure.class);
        return element == null ? false : element.getValue();
    }

    static class RetryFor extends BpelObject {
        public RetryFor(Element el) {
            super(el);
        }

        public int getValue() {
            String textValue = getTextValue();
            return textValue == null ? 0 : Integer.parseInt(textValue);
        }
    }

    static class RetryDelay extends BpelObject {
        public RetryDelay(Element el) {
            super(el);
        }

        public int getValue() {
            String textValue = getTextValue();
            return textValue == null ? 0 : Integer.parseInt(textValue);
        }
    }

    static class FaultOnFailure extends BpelObject {
        public FaultOnFailure(Element el) {
            super(el);
        }

        public boolean getValue() {
            String textValue = getTextValue();
            return textValue == null ? false : (textValue.equals("true") || textValue.equals("yes"));
        }
    }
}
