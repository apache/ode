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
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.lang.StringUtils;
import org.apache.ode.utils.wsdl.WsdlUtils;

import javax.wsdl.Binding;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.extensions.http.HTTPOperation;
import javax.wsdl.extensions.mime.MIMEContent;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * @author <a href="mailto:midon@intalio.com">Alexis Midon</a>
 */
public class HttpBindingValidator {

    private static final Messages httpMsgs = Messages.getMessages(Messages.class);
    private static final org.apache.ode.utils.wsdl.Messages wsdlMsgs = Messages.getMessages(org.apache.ode.utils.wsdl.Messages.class);

    protected Binding binding;

    public HttpBindingValidator(Binding binding) {
        this.binding = binding;
        if (!WsdlUtils.useHTTPBinding(binding))
            throw new IllegalArgumentException(httpMsgs.msgHttpBindingNotUsed(binding));
    }

    public void validate() throws IllegalArgumentException {
        validatePort();
    }

    protected void validatePort() {
        // Validate the given HttpBinding
        for (int i = 0; i < binding.getBindingOperations().size(); i++) {
            BindingOperation bindingOperation = (BindingOperation) binding.getBindingOperations().get(i);
            validateOperation(bindingOperation);
        }
    }

    protected void validateOperation(BindingOperation bindingOperation) {
        String verb = WsdlUtils.resolveVerb(binding, bindingOperation);
        if (verb == null) {
            throw new IllegalArgumentException(httpMsgs.msgMissingVerb(binding, bindingOperation));
        }
        if (!"GET".equalsIgnoreCase(verb)
                && !"DELETE".equalsIgnoreCase(verb)
                && !"PUT".equalsIgnoreCase(verb)
                && !"POST".equalsIgnoreCase(verb)) {
            throw new IllegalArgumentException(httpMsgs.msgUnsupportedHttpMethod(binding, verb));
        }


        BindingOutput output = bindingOperation.getBindingOutput();
        MIMEContent outputContent = WsdlUtils.getMimeContent(output.getExtensibilityElements());
        if (outputContent != null) {
            if (StringUtils.isEmpty(outputContent.getType())) {
                throw new IllegalArgumentException(httpMsgs.msgEmptyContentType(binding, bindingOperation));
            }
        }

        BindingInput input = bindingOperation.getBindingInput();

        // multipartRelated not supported
        if (WsdlUtils.useMimeMultipartRelated(input)) {
            throw new IllegalArgumentException(httpMsgs.msgMimeMultipartRelatedUnsupported(binding, bindingOperation));
        }

        // only 2 content-types supported
        MIMEContent inputContent = WsdlUtils.getMimeContent(input.getExtensibilityElements());
        if (inputContent != null) {
            String inputContentType = inputContent.getType();
            if (StringUtils.isEmpty(inputContentType)) {
                throw new IllegalArgumentException(httpMsgs.msgEmptyContentType(binding, bindingOperation));
            }
        }

        if (WsdlUtils.useUrlReplacement(input)) {
            validateUrlReplacement(bindingOperation);
        }

        // other specific validations
        if ("GET".equalsIgnoreCase(verb) || "DELETE".equalsIgnoreCase(verb)) {
            validateGetOrDelete(bindingOperation);
        }
    }

    protected void validateGetOrDelete(BindingOperation bindingOperation) {
        BindingInput input = bindingOperation.getBindingInput();

        if (!WsdlUtils.useUrlEncoded(input) && !WsdlUtils.useUrlReplacement(input)) {
            throw new IllegalArgumentException(httpMsgs.msgOnlySupportsUrlEncodedAndUrlreplacement(binding, bindingOperation));
        }

        // another test would be to check that all parts use a simple type
    }

    protected void validateUrlReplacement(BindingOperation bindingOperation) {
        HTTPOperation httpOperation = (HTTPOperation) WsdlUtils.getOperationExtension(bindingOperation);
        BindingInput input = bindingOperation.getBindingInput();
        Map inputParts = bindingOperation.getOperation().getInput().getMessage().getParts();

        // validate the url pattern
        if (WsdlUtils.useUrlReplacement(input)) {
            String locationUri = httpOperation.getLocationURI();
            Set partNames = inputParts.keySet();
            // Must be *exactly* one search pattern for each message part.
            for (Iterator it = partNames.iterator(); it.hasNext();) {
                String name = (String) it.next();
                Pattern p = Pattern.compile(".*(\\(" + name + "\\)).*");
                Matcher m = p.matcher(locationUri);
                // WSLD spec requires that all message parts must be exactly once in the url pattern.
                // However ODE relaxes this.
                // The only test is to make sure a part is not mentioned more than once
                if (m.matches() && locationUri.split("(\\(" + name + "\\))", -1).length != 2) {
                    throw new IllegalArgumentException(httpMsgs.msgInvalidURIPattern(binding, bindingOperation, locationUri));
                }
            }
        }

        // another test would be to check that all parts use a simple type
    }

}
