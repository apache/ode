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
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.ode.axis2.Properties;
import org.apache.ode.axis2.util.URLEncodedTransformer;
import org.apache.ode.axis2.util.UrlReplacementTransformer;
import org.apache.ode.bpel.iapi.PartnerRoleMessageExchange;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.Namespaces;
import org.apache.ode.utils.wsdl.Messages;
import org.apache.ode.utils.wsdl.WsdlUtils;
import org.apache.ode.il.epr.MutableEndpoint;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.wsdl.Binding;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Part;
import javax.wsdl.BindingOutput;
import javax.wsdl.extensions.http.HTTPOperation;
import javax.wsdl.extensions.mime.MIMEContent;
import javax.wsdl.extensions.UnknownExtensibilityElement;
import javax.xml.namespace.QName;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Collection;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class HttpMethodConverter {

    private static final String CONTENT_TYPE_TEXT_XML = "text/xml";
    private static final Log log = LogFactory.getLog(HttpMethodConverter.class);

    protected static final Messages msgs = Messages.getMessages(Messages.class);
    protected Binding binding;

    public HttpMethodConverter(Binding binding) {
        this.binding = binding;
    }

    public HttpMethod createHttpRequest(PartnerRoleMessageExchange odeMex, HttpParams params) throws UnsupportedEncodingException {
        Operation operation = odeMex.getOperation();
        BindingOperation bindingOperation = binding.getBindingOperation(operation.getName(), operation.getInput().getName(), operation.getOutput().getName());

        // message to be sent
        Element message = odeMex.getRequest().getMessage();
        Message msgDef = operation.getInput().getMessage();

        // base url
        String url = ((MutableEndpoint) odeMex.getEndpointReference()).getUrl();

        // extract part values into a map and check that all parts are assigned a value
        Map<String, Element> partElements = extractPartElements(msgDef, message);

        // http method type
        // the operation may override the verb, this is an extension for RESTful BPEL
        String verb = WsdlUtils.resolveVerb(binding, bindingOperation);

        // build the http method itself
        HttpMethod method = prepareHttpMethod(bindingOperation, verb, partElements, url, params);
        return method;
    }


    /**
     * create and initialize the http method.
     * Http Headers that may been passed in the params are not set in this method.
     * Headers will be automatically set by HttpClient.
     * See usages of HostParams.DEFAULT_HEADERS
     * See org.apache.commons.httpclient.HttpMethodDirector#executeMethod(org.apache.commons.httpclient.HttpMethod)
     */
    protected HttpMethod prepareHttpMethod(BindingOperation opBinding, String verb, Map<String, Element> partValues,
                                           final String rootUri, HttpParams params) throws UnsupportedEncodingException {
        if (log.isDebugEnabled()) log.debug("Preparing http request...");
        // convenience variables...
        BindingInput bindingInput = opBinding.getBindingInput();
        HTTPOperation httpOperation = (HTTPOperation) WsdlUtils.getOperationExtension(opBinding);
        MIMEContent content = WsdlUtils.getMimeContent(bindingInput.getExtensibilityElements());
        String contentType = content == null ? "" : content.getType();
        boolean useUrlEncoded = WsdlUtils.useUrlEncoded(bindingInput) || PostMethod.FORM_URL_ENCODED_CONTENT_TYPE.equalsIgnoreCase(contentType);
        boolean useUrlReplacement = WsdlUtils.useUrlReplacement(bindingInput);

        // the http method to be built and returned
        HttpMethod method = null;

        // the 4 elements the http method may be made of
        String relativeUri = httpOperation.getLocationURI();
        String queryPath = null;
        RequestEntity requestEntity;
        String encodedParams = null;

        // ODE supports uri template in both port and operation location.
        // so assemble the final url *before* replacement
        String completeUri = rootUri;
        if (StringUtils.isNotEmpty(relativeUri)) {
            completeUri = completeUri + (completeUri.endsWith("/") || relativeUri.startsWith("/") ? "" : "/") + relativeUri;
        }

        if (useUrlReplacement) {
            // insert part values in the url
            completeUri = new UrlReplacementTransformer().transform(completeUri, partValues);
        } else if (useUrlEncoded) {
            // encode part values
            encodedParams = new URLEncodedTransformer().transform(partValues);
        }

        // http-client api is not really neat
        // something similar to the following would save some if/else manipulations.
        // But we have to deal with it as-is.
        //
        //  method = new Method(verb);
        //  method.setRequestEnity(..)
        //  etc...
        if ("GET".equalsIgnoreCase(verb) || "DELETE".equalsIgnoreCase(verb)) {
            if ("GET".equalsIgnoreCase(verb)) {
                method = new GetMethod();
            } else if ("DELETE".equalsIgnoreCase(verb)) {
                method = new DeleteMethod();
            }
            if (useUrlEncoded) {
                queryPath = encodedParams;
            }

            // Let http-client manage the redirection
            // see org.apache.commons.httpclient.params.HttpClientParams.MAX_REDIRECTS
            // default is 100
            method.setFollowRedirects(true);
        } else if ("POST".equalsIgnoreCase(verb) || "PUT".equalsIgnoreCase(verb)) {

            if ("POST".equalsIgnoreCase(verb)) {
                method = new PostMethod();
            } else if ("PUT".equalsIgnoreCase(verb)) {
                method = new PutMethod();
            }

            // some body-building...
            if (useUrlEncoded) {
                requestEntity = new StringRequestEntity(encodedParams, PostMethod.FORM_URL_ENCODED_CONTENT_TYPE, method.getParams().getContentCharset());
            } else if (contentType.endsWith(CONTENT_TYPE_TEXT_XML)) {
                // get the part to be put in the body
                Part part = opBinding.getOperation().getInput().getMessage().getPart(content.getPart());
                Element partValue = partValues.get(part.getName());
                // if the part has an element name, we must take the first element
                if (part.getElementName() != null) {
                    partValue = DOMUtils.getFirstChildElement(partValue);
                }
                String xmlString = DOMUtils.domToString(partValue);
                requestEntity = new ByteArrayRequestEntity(xmlString.getBytes(), contentType);
            } else {
                // should not happen because of HttpBindingValidator, but never say never
                throw new IllegalArgumentException("Unsupported content-type!");
            }

            // cast safely, PUT and POST are subclasses of EntityEnclosingMethod
            final EntityEnclosingMethod enclosingMethod = (EntityEnclosingMethod) method;
            enclosingMethod.setRequestEntity(requestEntity);
            enclosingMethod.setContentChunked(params.getBooleanParameter(Properties.PROP_HTTP_REQUEST_CHUNK, false));

        } else {
            // should not happen because of HttpBindingValidator, but never say never
            throw new IllegalArgumentException("Unsupported HTTP method: " + verb);
        }

        // link params together
        method.getParams().setDefaults(params);
        method.setPath(completeUri); // assumes that the path is properly encoded (URL safe).
        method.setQueryString(queryPath);

        // headers
        setHttpRequestHeaders(method, partValues, opBinding.getOperation().getInput().getMessage(), opBinding.getBindingInput());
        return method;
    }

    /**
     * Go through the list of {@linkplain Namespaces.ODE_HTTP_EXTENSION_NS}{@code :header} elements included in the input binding. For each of them, set the HTTP Request Header with the static value defined by the attribute {@linkplain Namespaces.ODE_HTTP_EXTENSION_NS}{@code :value},
     *  or the part value mentionned in the attribute {@linkplain Namespaces.ODE_HTTP_EXTENSION_NS}{@code :part}.
     */
    public void setHttpRequestHeaders(HttpMethod method, Map<String, Element> partValues, Message inputMessage, BindingInput bindingInput) {
        Collection<UnknownExtensibilityElement> headerBindings = WsdlUtils.getHttpHeaders(bindingInput.getExtensibilityElements());
        for (Iterator<UnknownExtensibilityElement> iterator = headerBindings.iterator(); iterator.hasNext();) {
            Element binding = iterator.next().getElement();
            String headerName = binding.getAttribute("name");
            String partName = binding.getAttribute("part");
            String value = binding.getAttribute("value");

            String headerValue;
            if (StringUtils.isNotEmpty(partName)) {
                // get the part to be put in the body
                Part part = inputMessage.getPart(partName);
                Element partValue = partValues.get(part.getName());
                // if the part has an element name, we must take the first element
                if (part.getElementName() != null) partValue = DOMUtils.getFirstChildElement(partValue);
                headerValue = DOMUtils.domToString(partValue);
            } else if (StringUtils.isNotEmpty(value)) {
                headerValue = value;
            } else {
                String errMsg = "Invalid binding: missing attribute! Expecting " + new QName(Namespaces.ODE_HTTP_EXTENSION_NS, "part") + " or " + new QName(Namespaces.ODE_HTTP_EXTENSION_NS, "value");
                if (log.isErrorEnabled()) {
                    log.error(errMsg);
                }
                throw new RuntimeException(errMsg);
            }
            method.setRequestHeader(headerName, HttpClientHelper.replaceCRLFwithLWS(headerValue));
        }
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


    /**
     * Create the element to be associated with this part into the {@link org.apache.ode.bpel.iapi.Message}.
     * <br/>An element named with the part name will be returned. the content of this element depends on the part.
     * <p/>If the part has a non-null element name, a new element will be created and named accordingly then the text value is inserted in this new element.
     * <br/>else the given text content is simply set on the part element.
     *
     * @param part
     * @param textContent
     * @return an element named with the part name will be returned
     */
    public Element createPartElement(Part part, String textContent) {
        Document doc = DOMUtils.newDocument();
        Element partElement = doc.createElementNS(null, part.getName());
        if (part.getElementName() != null) {
            Element element = doc.createElementNS(part.getElementName().getNamespaceURI(), part.getElementName().getLocalPart());
            element.setTextContent(textContent);
            partElement.appendChild(element);
        } else {
            partElement.setTextContent(textContent);
        }
        return partElement;
    }


    /**
     * Create the element to be associated with this part into the {@link org.apache.ode.bpel.iapi.Message}.
     * <p/>If the part has a non-null element name, the bodyElement is simply appended.
     * Else if the bodyElement has a text content, the value is set to the message.
     * Else append all nodes of bodyElement to the returned element. Attributes are ignored.
     * <p/>
     * The name of the returned element is the part name.
     *
     * @param part
     * @param receivedElement
     * @return the element to insert "as is" to ODE message
     */
    public Element createPartElement(Part part, Element receivedElement) {
        Document doc = DOMUtils.newDocument();
        Element partElement = doc.createElementNS(null, part.getName());
        if (part.getElementName() != null) {
            partElement.appendChild(doc.importNode(receivedElement, true));
        } else {
            if (DOMUtils.isEmptyElement(receivedElement)) {
                // Append an empty text node.
                // Warning! setting an empty string with setTextContent has not effect. See javadoc.
                partElement.appendChild(doc.createTextNode(""));
            } else {
                // No need to make the distinction between simple and complex types, importNode will handle it
                // !!! Attributes are ignored
                for (int m = 0; m < receivedElement.getChildNodes().getLength(); m++) {
                    Node child = receivedElement.getChildNodes().item(m);
                    partElement.appendChild(doc.importNode(child, true));
                }
            }
        }
        return partElement;
    }

    /**
     * Process the HTTP Response Headers.
     * <p/>
     * First go through the list of {@linkplain Namespaces.ODE_HTTP_EXTENSION_NS}{@code :header} elements included in the output binding. For each of them, set the header value as the value of the message part.
     * <br/>Then add all HTTP headers as header part in the message. The name of the header would be the part name.
     *
     * @param odeMessage
     * @param method
     * @param messageDef
     * @param bindingOutput
     */
    public void extractHttpResponseHeaders(org.apache.ode.bpel.iapi.Message odeMessage, HttpMethod method, Message messageDef, BindingOutput bindingOutput) {
        Collection<UnknownExtensibilityElement> headerBindings = WsdlUtils.getHttpHeaders(bindingOutput.getExtensibilityElements());

        // iterate through the list of header bindings
        // and set the message parts accordingly
        for (Iterator<UnknownExtensibilityElement> iterator = headerBindings.iterator(); iterator.hasNext();) {
            Element binding = iterator.next().getElement();
            String partName = binding.getAttribute("part");
            String headerName = binding.getAttribute("name");

            Part part = messageDef.getPart(partName);
            if (StringUtils.isNotEmpty(partName)) {
                odeMessage.setPart(partName, createPartElement(part, method.getRequestHeader(headerName).getValue()));
            } else {
                String errMsg = "Invalid binding: missing required attribute! Part name: " + new QName(Namespaces.ODE_HTTP_EXTENSION_NS, "part");
                if (log.isErrorEnabled()) log.error(errMsg);
                throw new RuntimeException(errMsg);
            }
        }

        // also add all HTTP headers into the messade as header parts
        Header[] reqHeaders = method.getResponseHeaders();
        for (int i = 0; i < reqHeaders.length; i++) {
            Header h = reqHeaders[i];
            odeMessage.setHeaderPart(h.getName(), h.getValue());
        }
    }


}

