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

package org.apache.ode.axis2.httpbinding;

import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.ode.utils.wsdl.WsdlUtils;

import javax.wsdl.Binding;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.extensions.http.HTTPBinding;
import javax.wsdl.extensions.http.HTTPOperation;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:midon@intalio.com">Alexis Midon</a>
 */
public class HttpBindingValidator {

    private static final Messages httpMsgs = Messages.getMessages(Messages.class);
    private static final org.apache.ode.utils.wsdl.Messages wsdlMsgs = Messages.getMessages(org.apache.ode.utils.wsdl.Messages.class);

    protected Binding binding;
    private String verb;

    public HttpBindingValidator(Binding binding) {
        this.binding = binding;
        if (!WsdlUtils.useHTTPBinding(binding))
            throw new IllegalArgumentException(httpMsgs.msgHttpBindingNotUsed(binding));
        HTTPBinding httpBinding = (HTTPBinding) WsdlUtils.getBindingExtension(binding);
        verb = httpBinding.getVerb();
    }

    public void validate() throws IllegalArgumentException {
        validatePort();
    }

    protected void validatePort() {

        if (!"get".equalsIgnoreCase(verb) && !"post".equalsIgnoreCase(verb)) {
            throw new IllegalArgumentException(httpMsgs.msgUnsupportedHttpMethod(binding, verb));
        }

        // Validate the given HttpBinding
        for (int i = 0; i < binding.getBindingOperations().size(); i++) {
            BindingOperation bindingOperation = (BindingOperation) binding.getBindingOperations().get(i);
            validateOperation(bindingOperation);
        }
    }

    protected void validateOperation(BindingOperation bindingOperation) {
        BindingOutput output = bindingOperation.getBindingOutput();
        String outputContentType = WsdlUtils.getMimeContentType(output.getExtensibilityElements());
        if (!outputContentType.endsWith("text/xml")) {
            throw new IllegalArgumentException(httpMsgs.msgUnsupportedContentType(binding, bindingOperation));
        }

        Map outputParts = bindingOperation.getOperation().getOutput().getMessage().getParts();
        if (outputParts.size() > 1) {
            throw new IllegalArgumentException(httpMsgs.msgOnePartOnlyForOutput(binding, bindingOperation));
        }

        BindingInput input = bindingOperation.getBindingInput();
        String inputContentType = WsdlUtils.getMimeContentType(input.getExtensibilityElements());

        // multipartRelated not supported
        if (WsdlUtils.useMimeMultipartRelated(input)) {
            throw new IllegalArgumentException(httpMsgs.msgMimeMultipartRelatedUnsupported(binding, bindingOperation));
        }

        // only 2 content-types supported
        if (inputContentType != null) {
            if (!inputContentType.endsWith("text/xml") && !PostMethod.FORM_URL_ENCODED_CONTENT_TYPE.equalsIgnoreCase(inputContentType)) {
                throw new IllegalArgumentException(httpMsgs.msgUnsupportedContentType(binding, bindingOperation));
            }
            Map inputParts = bindingOperation.getOperation().getInput().getMessage().getParts();
            if (inputParts.size() > 1) {
                if (!PostMethod.FORM_URL_ENCODED_CONTENT_TYPE.equalsIgnoreCase(inputContentType)) {
                    throw new IllegalArgumentException(httpMsgs.msgInvalidContentType(binding, bindingOperation));
                }
            }
        }

        // only GET can use urlreplacement
        if (!"get".equalsIgnoreCase(verb)) {
            if (WsdlUtils.useUrlReplacement(input)) {
                throw new IllegalArgumentException(httpMsgs.msgUrlReplacementWithGetOnly(binding));
            }
        }

        // other specific validations
        if ("get".equalsIgnoreCase(verb)) {
            validateGet(bindingOperation);
        }
    }

    protected void validateGet(BindingOperation bindingOperation) {
        HTTPOperation httpOperation = (HTTPOperation) WsdlUtils.getOperationExtension(bindingOperation);
        BindingInput input = bindingOperation.getBindingInput();
        Map inputParts = bindingOperation.getOperation().getInput().getMessage().getParts();

        if (!WsdlUtils.useUrlEncoded(input) && !WsdlUtils.useUrlReplacement(input)) {
            throw new IllegalArgumentException(httpMsgs.msgGetOnlySupportsUrlEncodedAndUrlreplacement(binding, bindingOperation));
        }

        // validate the url pattern
        if (WsdlUtils.useUrlReplacement(input)) {
            String locationUri = httpOperation.getLocationURI();
            Set partNames = inputParts.keySet();
            // Must be *exactly* one search pattern for each message part.
            for (Iterator it = partNames.iterator(); it.hasNext();) {
                String name = (String) it.next();
                Pattern p = Pattern.compile(".*(\\(" + name + "\\)).*");
                Matcher m = p.matcher(locationUri);
                // might be perfectible
                if (!m.matches() || locationUri.split("(\\(" + name + "\\))", -1).length != 2) {
                    throw new IllegalArgumentException(httpMsgs.msgInvalidURIPattern(binding, bindingOperation, locationUri));
                }
            }
        }

        // another test would be to check that all parts use a simple type
    }

}
