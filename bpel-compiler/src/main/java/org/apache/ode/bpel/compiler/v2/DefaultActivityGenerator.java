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
package org.apache.ode.bpel.compiler.v2;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.ode.bpel.compiler.CommonCompilationMessages;
import org.apache.ode.bpel.compiler.v2.CompilerContext;
import org.apache.ode.bpel.compiler.api.CompilationException;
import org.apache.ode.bpel.compiler.bom.BpelObject;
import org.apache.ode.bpel.compiler.bom.ContextPropagation;
import org.apache.ode.bpel.compiler.bom.FailureHandling;
import org.apache.ode.bpel.rtrep.v2.OActivity;
import org.apache.ode.bpel.rtrep.v2.OContextPropagation;
import org.apache.ode.bpel.rtrep.v2.OFailureHandling;
import org.apache.ode.bpel.rtrep.common.extension.ExtensibilityQNames;
import org.apache.ode.utils.msg.MessageBundle;
import org.w3c.dom.Element;

/**
 * Base implementation of the {@link ActivityGenerator} interface.
 */
abstract class DefaultActivityGenerator implements ActivityGenerator {
    private static final CommonCompilationMessages _cmsgsGeneral =
        MessageBundle.getMessages(CommonCompilationMessages.class);

    protected CompilerContext _context;

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
        Element element = src.getExtensibilityElement(ExtensibilityQNames.FAILURE_HANDLING);
        if (element == null)
            return;
        FailureHandling extElement = new FailureHandling(element);
        OFailureHandling obj = new OFailureHandling();
        obj.retryFor = extElement.getRetryFor();
        obj.retryDelay = extElement.getRetryDelay();
        obj.faultOnFailure = extElement.getFaultOnFailure();
        output.setFailureHandling(obj);
    }

    protected Set<OContextPropagation> doContextPropagation(BpelObject src) {
        Set<OContextPropagation> props = new LinkedHashSet<OContextPropagation>();
        // Context propagation extensibility element.
        for (Element element : src.getExtensibilityElements(ExtensibilityQNames.CONTEXT_PROPAGATE)) {
            ContextPropagation extElement = new ContextPropagation(element);
            OContextPropagation obj = new OContextPropagation();
            obj.contexts = Arrays.asList(extElement.getContext().split("\\s+"));
            if (obj.contexts == null) {
                throw new CompilationException(_cmsgsGeneral.errMissingContextAttribute());
            }
            String fromVariableName = extElement.getFromVariable();
            String fromPartnerLinkName = extElement.getFromPartnerLink();
            if (fromPartnerLinkName != null && fromVariableName == null) {
                obj.fromPartnerLink = _context.resolvePartnerLink(fromPartnerLinkName);    
            } else if (fromPartnerLinkName == null && fromVariableName != null) {
                obj.fromVariable = _context.resolveVariable(fromVariableName);
            } else {
                throw new CompilationException(_cmsgsGeneral.errMissingVariableOrPartnerLinkAttribute());
            }
            props.add(obj);
        }
        
        return props.isEmpty() ? null : props;
    }

}
