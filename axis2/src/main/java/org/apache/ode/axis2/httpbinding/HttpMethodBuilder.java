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

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.iapi.PartnerRoleMessageExchange;
import org.apache.ode.bpel.epr.MutableEndpoint;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.wsdl.*;
import org.apache.ode.utils.wsdl.Messages;
import org.apache.ode.axis2.util.UrlReplacementTransformer;
import org.apache.ode.axis2.util.URLEncodedTransformer;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.wsdl.Operation;
import javax.wsdl.BindingOperation;
import javax.wsdl.Message;
import javax.wsdl.Part;
import javax.wsdl.BindingInput;
import javax.wsdl.Binding;
import javax.wsdl.extensions.http.HTTPOperation;
import javax.wsdl.extensions.http.HTTPBinding;
import javax.xml.namespace.QName;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import com.ibm.wsdl.PartImpl;

/**
 * @author <a href="mailto:midon@intalio.com">Alexis Midon</a>
 */
public class HttpMethodBuilder {

    private static final String CONTENT_TYPE_TEXT_XML = "text/xml";
    private static final Log log = LogFactory.getLog(HttpMethodBuilder.class);

    protected static final org.apache.ode.utils.wsdl.Messages msgs = Messages.getMessages(Messages.class);
    protected Binding binding;
    protected String verb;

    public HttpMethodBuilder(Binding binding) {
        this.binding = binding;
        HTTPBinding httpBinding = (HTTPBinding) WsdlUtils.getBindingExtension(binding);
        this.verb = httpBinding.getVerb();
    }


    public HttpMethod buildHttpMethod(PartnerRoleMessageExchange odeMex) throws UnsupportedEncodingException {
        Operation operation = odeMex.getOperation();
        BindingOperation bindingOperation = binding.getBindingOperation(operation.getName(), operation.getInput().getName(), operation.getOutput().getName());
        // message to be sent
        Element message = odeMex.getRequest().getMessage();
        Message msgDef = operation.getInput().getMessage();

        // base url
        String url = ((MutableEndpoint) odeMex.getEndpointReference()).getUrl();

        // extract part values into a map and check that all parts are assigned a value
        Map<String, Element> partElements = extractPartElements(msgDef, message);

        // build the http method
        HttpMethod method = prepareHttpMethod(bindingOperation, partElements, url);
        return method;
    }

    protected Map<String, Element> extractPartElements(Message msgDef, Element message) {
        Map<String, Element> partValues = new HashMap<String, Element>();
        for (Iterator iterator = msgDef.getParts().values().iterator(); iterator.hasNext();) {
            Part part = (Part) iterator.next();
            Element partEl = DOMUtils.findChildByName(message, new QName(null, part.getName()));
            if (partEl == null)
                throw new IllegalArgumentException(msgs.msgOdeMessageMissingRequiredPart(part.getName()));
            partValues.put(part.getName(), partEl);
        }
        return partValues;
    }

    protected HttpMethod prepareHttpMethod(BindingOperation bindingOperation, Map<String, Element> partValues, String rootUri) throws UnsupportedEncodingException {
        if (log.isDebugEnabled()) log.debug("Preparing http request...");
        HttpMethod method;
        BindingInput bindingInput = bindingOperation.getBindingInput();
        HTTPOperation httpOperation = (HTTPOperation) WsdlUtils.getOperationExtension(bindingOperation);
        String contentType = WsdlUtils.getMimeContentType(bindingInput.getExtensibilityElements());
        boolean useUrlEncoded = WsdlUtils.useUrlEncoded(bindingInput) || PostMethod.FORM_URL_ENCODED_CONTENT_TYPE.equalsIgnoreCase(contentType);
        boolean useUrlReplacement = WsdlUtils.useUrlReplacement(bindingInput);
        String relativeUri = httpOperation.getLocationURI();
        if ("get".equalsIgnoreCase(verb)) {
            String queryPath = null;
            if (useUrlReplacement) {
                // insert part values in the url
                UrlReplacementTransformer transformer = new UrlReplacementTransformer(partValues.keySet());
                relativeUri = transformer.transform(relativeUri, partValues);
            } else if (useUrlEncoded) {
                // encode part values
                URLEncodedTransformer transformer = new URLEncodedTransformer();
                queryPath = transformer.transform(partValues);
            }
            String uri = rootUri + (relativeUri.startsWith("/") ? "" : "/") + relativeUri;
            method = new GetMethod(uri);
            method.setQueryString(queryPath);
            // Let http-client manage the redirection for GET
            // see org.apache.commons.httpclient.params.HttpClientParams.MAX_REDIRECTS
            // default is 100
            method.setFollowRedirects(true);
        } else if ("post".equalsIgnoreCase(verb)) {
            RequestEntity requestEntity;
            if (useUrlEncoded) {
                URLEncodedTransformer transformer = new URLEncodedTransformer();
                String encodedParams = transformer.transform(partValues);
                requestEntity = new StringRequestEntity(encodedParams, PostMethod.FORM_URL_ENCODED_CONTENT_TYPE, "UTF-8");
            } else if (contentType.endsWith(CONTENT_TYPE_TEXT_XML)) {
                // assumption is made that there is a single part
                // validation steps in the constructor must warranty that
                Part part = (Part) bindingOperation.getOperation().getInput().getMessage().getParts().values().iterator().next();
                Element partValue = partValues.get(part.getName());
                // if the part has an element name, we must take the first element
                if(part.getElementName()!=null){
                    partValue = DOMUtils.getFirstChildElement(partValue);
                }
                String xmlString = DOMUtils.domToString(partValue);
                requestEntity = new ByteArrayRequestEntity(xmlString.getBytes(), contentType);
            } else {
                // should not happen because of HttpBindingValidator, but never say never
                throw new IllegalArgumentException("Unsupported content-type!");
            }
            String uri = rootUri + (relativeUri.startsWith("/") ? "" : "/") + relativeUri;
            PostMethod post = new PostMethod(uri);
            post.setRequestEntity(requestEntity);
            method = post;
        } else {
            // should not happen because of HttpBindingValidator, but never say never
            throw new IllegalArgumentException("Unsupported HTTP method: " + verb);
        }
        return method;
    }
}
