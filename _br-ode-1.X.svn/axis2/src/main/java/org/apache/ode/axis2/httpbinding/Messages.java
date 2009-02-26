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

import org.apache.ode.utils.msg.MessageBundle;

import javax.xml.namespace.QName;
import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import java.io.File;

/**
 * @author <a href="mailto:midon@intalio.com">Alexis Midon</a>
 */
public class Messages extends MessageBundle {

    public String msgMissingVerb(Binding binding, BindingOperation operation) {
        return format("No verb defined for binding: {0} operation: {1}", binding.getQName(), operation.getName());
    }

    public String msgUnsupportedHttpMethod(Binding binding, String verb) {
        return format("Unsupported HTTP method! binding: {0} method: {1}", binding.getQName(), verb);
    }

    public String msgHttpBindingNotUsed(Binding binding) {
        return format("This binding does not use http:binding: {0}" + binding.getQName());
    }

    public String msgEmptyContentType(Binding binding, BindingOperation operation) {
        return format("MIME Content Type is empty! binding: {0} operation: {1}", binding.getQName(), operation.getName());
    }

    public String msgUnsupportedContentType(Binding binding, BindingOperation operation) {
        return format("Unsupported content-type! binding: {0} operation: {1}", binding.getQName(), operation.getName());
    }

    public String msgInvalidURIPattern() {
        return format("Invalid URI Pattern!");
    }

    public String msgInvalidURIPattern(Binding binding, BindingOperation operation, String locationUri) {
        return format("Invalid URI Pattern : parts must not be mentioned more than once! binding:{0}, operation:{1}, locationUri:{2}", binding.getQName(), operation.getName(), locationUri);
    }
    
    public String msgMimeMultipartRelatedUnsupported(Binding binding, BindingOperation operation) {
        return format("MimeMultipartRelated is not supported! binding:{0}, operation:{1}", binding.getQName(), operation.getName());
    }

    public String msgInvalidContentType(Binding binding, BindingOperation operation) {
        return format("Invalid content-type! binding:{0}, operation:{1}", binding.getQName(), operation.getName());
    }

    public String msgSimpleTypeExpected(String partName){
        return format("Simple type expected for {0}", partName);
    }

    public String msgOnlySupportsUrlEncodedAndUrlreplacement(Binding binding, BindingOperation operation) {
        return format("Only supports urlEncoded or urlReplacement! binding:{0}, operation:{1}", binding.getQName(), operation.getName());
    }

}
