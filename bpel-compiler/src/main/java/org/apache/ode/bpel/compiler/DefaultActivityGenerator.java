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
package org.apache.ode.bpel.compiler;

import org.apache.ode.bom.api.Activity;
import org.apache.ode.bom.api.BpelObject;
import org.apache.ode.bpel.o.OActivity;
import org.apache.ode.bpel.o.FailureHandling;
import org.apache.ode.bpel.capi.CompilationException;
import org.apache.ode.bpel.capi.CompilerContext;
import org.apache.ode.utils.msg.MessageBundle;
import org.apache.ode.utils.DOMUtils;

import javax.xml.namespace.QName;
import org.w3c.dom.Element;

/**
 * Base implementation of the {@link ActivityGenerator} interface.
 */
abstract class DefaultActivityGenerator implements ActivityGenerator {
    protected CompilerContext _context;
    private static final CommonCompilationMessages __cmsgs = MessageBundle.getMessages(CommonCompilationMessages.class);

    public void setContext(CompilerContext context) {
        _context = context;
    }

    static void defaultExtensibilityElements(OActivity output, BpelObject src) {
        if (src != null) {
            failureHandlinExtensibilityElement(output, src);
        }
    }

    static private void failureHandlinExtensibilityElement(OActivity output, BpelObject src) {
        // Failure handling extensibility element.
        Element failure = (Element) src.getExtensibilityElements().get(FailureHandling.FAILURE_EXT_ELEMENT);
        if (failure != null) {
            FailureHandling failureHandling = new FailureHandling();
            output.setFailureHandling(failureHandling);
            String textValue;
            Element element = DOMUtils.findChildByName(failure, new QName(FailureHandling.EXTENSION_NS_URI, "retryFor"));
            if (element != null) {
                textValue = DOMUtils.getTextContent(element);
                if (textValue != null) {
                    try {
                        failureHandling.retryFor = Integer.parseInt(textValue);
                        if (failureHandling.retryFor < 0)
                            throw new CompilationException(__cmsgs.errInvalidRetryForValue(textValue));
                    } catch (NumberFormatException except) {
                        throw new CompilationException(__cmsgs.errInvalidRetryForValue(textValue));
                    }
                }
            }
            element = DOMUtils.findChildByName(failure, new QName(FailureHandling.EXTENSION_NS_URI, "retryDelay"));
            if (element != null) {
                textValue = DOMUtils.getTextContent(element);
                if (textValue != null) {
                    try {
                        failureHandling.retryDelay = Integer.parseInt(textValue);
                        if (failureHandling.retryDelay < 0)
                            throw new CompilationException(__cmsgs.errInvalidRetryDelayValue(textValue));
                    } catch (NumberFormatException except) {
                        throw new CompilationException(__cmsgs.errInvalidRetryDelayValue(textValue));
                    }
                }
            }
            element = DOMUtils.findChildByName(failure, new QName(FailureHandling.EXTENSION_NS_URI, "faultOnFailure"));
            if (element != null) {
                textValue = DOMUtils.getTextContent(element);
                if (textValue != null)
                    failureHandling.faultOnFailure = Boolean.valueOf(textValue).booleanValue();
            }
        }
    }

}
