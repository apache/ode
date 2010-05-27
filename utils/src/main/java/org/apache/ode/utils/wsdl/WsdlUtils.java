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

package org.apache.ode.utils.wsdl;

import org.apache.ode.utils.Namespaces;
import org.apache.ode.utils.stl.CollectionsX;
import org.w3c.dom.Element;

import javax.wsdl.Binding;
import javax.wsdl.BindingFault;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Fault;
import javax.wsdl.Operation;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.UnknownExtensibilityElement;
import javax.wsdl.extensions.http.HTTPAddress;
import javax.wsdl.extensions.http.HTTPBinding;
import javax.wsdl.extensions.http.HTTPOperation;
import javax.wsdl.extensions.http.HTTPUrlEncoded;
import javax.wsdl.extensions.http.HTTPUrlReplacement;
import javax.wsdl.extensions.mime.MIMEContent;
import javax.wsdl.extensions.mime.MIMEMultipartRelated;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Iterator;

/**
 * @author <a href="mailto:midon@intalio.com">Alexis Midon</a>
 */
public class WsdlUtils {

    private static final Messages msgs = Messages.getMessages(Messages.class);

    /**
     * Test if the given binding uses a Soap binding.
     *
     * @param binding
     * @return true if {@link SOAPBinding} is assignable from the binding
     * @see #getBindingExtension(javax.wsdl.Binding)
     */
    public static boolean useSOAPBinding(Binding binding) {
        ExtensibilityElement element = getBindingExtension(binding);
        return SOAPBinding.class.isAssignableFrom(element.getClass());
    }


    /**
     * Test if the given binding uses HTTP binding.
     *
     * @param binding
     * @return true if {@link HTTPBinding} is assignable from the binding
     * @see #getBindingExtension(javax.wsdl.Binding)
     */
    public static boolean useHTTPBinding(Binding binding) {
        ExtensibilityElement element = getBindingExtension(binding);
        // with a fully wsdl-compliant document, this element cannot be null.
        // but ODE extends the HTTP binding and supports the HTTP verb at the operation level.
        // A port using this extension may have no HTTPBinding at the port level.
        if (element == null) {
            // in this case, we check the binding information of one operation
            final BindingOperation anOperation = (BindingOperation) binding.getBindingOperations().get(0);
            final ExtensibilityElement opExt = getOperationExtension(anOperation);
            return HTTPOperation.class.isAssignableFrom(opExt.getClass());
        } else {
            return HTTPBinding.class.isAssignableFrom(element.getClass());
        }
    }


    /**
     * @see #useSOAPBinding(javax.wsdl.Binding)
     */
    public static boolean useSOAPBinding(Port port) {
        return useSOAPBinding(port.getBinding());
    }

    /**
     * @see #useHTTPBinding(javax.wsdl.Binding)
     */
    public static boolean useHTTPBinding(Port port) {
        return useHTTPBinding(port.getBinding());
    }

    /**
     * @see #useSOAPBinding(javax.wsdl.Binding)
     */
    public static boolean useSOAPBinding(Definition def, QName serviceName, String portName) {
        Service serviceDef = def.getService(serviceName);
        if (serviceDef == null)
            throw new IllegalArgumentException(msgs.msgServiceDefinitionNotFound(serviceName));
        Port port = serviceDef.getPort(portName);
        if (port == null)
            throw new IllegalArgumentException(msgs.msgPortDefinitionNotFound(serviceName, portName));
        return useSOAPBinding(port);
    }

    /**
     * @see #useHTTPBinding(javax.wsdl.Binding)
     */
    public static boolean useHTTPBinding(Definition def, QName serviceName, String portName) {
        Service serviceDef = def.getService(serviceName);
        if (serviceDef == null)
            throw new IllegalArgumentException(msgs.msgServiceDefinitionNotFound(serviceName));
        Port port = serviceDef.getPort(portName);
        if (port == null)
            throw new IllegalArgumentException(msgs.msgPortDefinitionNotFound(serviceName, portName));
        return useHTTPBinding(port);
    }

    /**
     * Look up the ExtensibilityElement defining the binding for the given Port or
     * throw an {@link IllegalArgumentException} if multiple bindings found.
     *
     * @param binding
     * @return an instance of {@link SOAPBinding} or {@link HTTPBinding} or null
     * @throws IllegalArgumentException if multiple bindings found.
     */
    public static ExtensibilityElement getBindingExtension(Binding binding) {
        Collection bindings = new ArrayList();
        CollectionsX.filter(bindings, binding.getExtensibilityElements(), HTTPBinding.class);
        CollectionsX.filter(bindings, binding.getExtensibilityElements(), SOAPBinding.class);
        if (bindings.size() == 0) {
            return null;
        } else if (bindings.size() > 1) {
            // exception if multiple bindings found
            throw new IllegalArgumentException(msgs.msgMultipleBindings(binding.getQName()));
        } else {
            // retrieve the single element
            ExtensibilityElement result = (ExtensibilityElement) bindings.iterator().next();
            return result;
        }
    }

    /**
     * @see #getBindingExtension(javax.wsdl.Binding)
     */
    public static ExtensibilityElement getBindingExtension(Port port) {
        Binding binding = port.getBinding();
        if (binding == null) {
            throw new IllegalArgumentException(msgs.msgBindingNotFound(port.getName()));
        }
        return getBindingExtension(binding);
    }

    /**
     * Extract the instance of {@link javax.wsdl.extensions.http.HTTPOperation] or {@link javax.wsdl.extensions.soap.SOAPOperation}
     * from the list of extensibility elements of the given {@link javax.wsdl.BindingOperation}.
     *
     * @param bindingOperation
     * @return an instance of {@link javax.wsdl.extensions.http.HTTPOperation} or {@link javax.wsdl.extensions.soap.SOAPOperation}
     * @throws IllegalArgumentException if not exactly 1 element is found.
     */
    public static ExtensibilityElement getOperationExtension(BindingOperation bindingOperation) {
        Collection operations = new ArrayList();
        CollectionsX.filter(operations, bindingOperation.getExtensibilityElements(), HTTPOperation.class);
        CollectionsX.filter(operations, bindingOperation.getExtensibilityElements(), SOAPOperation.class);

        if (operations.size() == 0) {
            // exception if no bindings found
            throw new IllegalArgumentException(msgs.msgNoBindingForOperation(bindingOperation.getName()));
        } else if (operations.size() > 1) {
            // exception if multiple bindings found
            throw new IllegalArgumentException(msgs.msgMultipleBindingsForOperation(bindingOperation.getName()));
        } else {
            // retrieve the single element
            ExtensibilityElement result = (ExtensibilityElement) operations.iterator().next();
            return result;
        }

    }

    /**
     * @return true if the extensibility elements of the given {@link javax.wsdl.BindingInput} contains an instance of {@link javax.wsdl.extensions.http.HTTPUrlEncoded}
     */
    public static boolean useUrlEncoded(BindingInput bindingInput) {
        Collection<HTTPUrlEncoded> coll = CollectionsX.filter(bindingInput.getExtensibilityElements(), HTTPUrlEncoded.class);
        return !coll.isEmpty();
    }

    /**
     * @return true if the extensibility elements of the given {@link javax.wsdl.BindingInput} contains an instance of {@link javax.wsdl.extensions.http.HTTPUrlReplacement}
     */
    public static boolean useUrlReplacement(BindingInput bindingInput) {
        Collection<HTTPUrlReplacement> coll = CollectionsX.filter(bindingInput.getExtensibilityElements(), HTTPUrlReplacement.class);
        return !coll.isEmpty();
    }

    /**
     * @return true if the extensibility elements of the given {@link javax.wsdl.BindingInput} contains an instance of {@link javax.wsdl.extensions.mime.MIMEMultipartRelated}
     */
    public static boolean useMimeMultipartRelated(BindingInput bindingInput) {
        Collection<MIMEMultipartRelated> coll = CollectionsX.filter(bindingInput.getExtensibilityElements(), MIMEMultipartRelated.class);
        return !coll.isEmpty();
    }

    /**
     * @return the {@linkplain javax.wsdl.extensions.mime.MIMEContent#getType() type} of the instance of {@link javax.wsdl.extensions.mime.MIMEContent}
     *         contained in the extensibility element list. Or null if none.
     * @throws IllegalArgumentException if more than 1 MIMEContent is found.
     */
    public static MIMEContent getMimeContent(List extensibilityElements) {
        Collection<MIMEContent> coll = CollectionsX.filter(extensibilityElements, MIMEContent.class);
        if (coll.size() == 0) {
            return null;
        } else if (coll.size() > 1) {
            // exception if multiple contents found
            throw new IllegalArgumentException(msgs.msgMultipleMimeContent());
        } else {
            // retrieve the single element
            return coll.iterator().next();
        }
    }

    /**
     * Extract the instance of {@link javax.wsdl.extensions.http.HTTPAddress] or {@link javax.wsdl.extensions.soap.SOAPAddress}
     * from the list of extensibility elements of the given {@link javax.wsdl.Port}.
     *
     * @param port
     * @return an instance of {@link javax.wsdl.extensions.http.HTTPAddress} or {@link javax.wsdl.extensions.soap.SOAPAddress}
     * @throws IllegalArgumentException if not exactly 1 element is found.
     */
    public static ExtensibilityElement getAddressExtension(Port port) {
        Collection operations = new ArrayList();
        CollectionsX.filter(operations, port.getExtensibilityElements(), HTTPAddress.class);
        CollectionsX.filter(operations, port.getExtensibilityElements(), SOAPAddress.class);

        if (operations.size() == 0) {
            // exception if no bindings found
            throw new IllegalArgumentException(msgs.msgNoAddressForPort(port.getName()));
        } else if (operations.size() > 1) {
            // exception if multiple bindings found
            throw new IllegalArgumentException(msgs.msgMultipleAddressesForPort(port.getName()));
        } else {
            // retrieve the single element
            ExtensibilityElement result = (ExtensibilityElement) operations.iterator().next();
            return result;
        }
    }

    /**
     * ODE extends the wsdl spec by allowing definition of the HTTP verb at the operation level.
     * <br/> If you do so, an {@link UnknownExtensibilityElement} will be added to the list of extensibility elements of the {@link javax.wsdl.BindingOperation}.
     * <br/> This method looks up for such an element and return the value of the verb attribute if the underlying {@link org.w3c.dom.Element} is {@literal <binding xmlns="http://schemas.xmlsoap.org/wsdl/http/"/>}
     * or null.
     *
     * @param bindingOperation
     */
    public static String getOperationVerb(BindingOperation bindingOperation) {
        final Collection<UnknownExtensibilityElement> unknownExtElements = CollectionsX.filter(bindingOperation.getExtensibilityElements(), UnknownExtensibilityElement.class);
        for (UnknownExtensibilityElement extensibilityElement : unknownExtElements) {
            final Element e = extensibilityElement.getElement();
            if (Namespaces.ODE_HTTP_EXTENSION_NS.equalsIgnoreCase(e.getNamespaceURI())
                    && "binding".equals(extensibilityElement.getElement().getLocalName())
                    && e.hasAttribute("verb")) {
                return e.getAttribute("verb");
            }
        }
        return null;
    }

    /**
     * @param fault
     * @return true if the given fault is bound with the {@link org.apache.ode.utils.Namespaces.ODE_HTTP_EXTENSION_NS}:fault element.
     */
    public static boolean isOdeFault(BindingFault fault) {
        final Collection<UnknownExtensibilityElement> unknownExtElements = CollectionsX.filter(fault.getExtensibilityElements(), UnknownExtensibilityElement.class);
        for (UnknownExtensibilityElement extensibilityElement : unknownExtElements) {
            final Element e = extensibilityElement.getElement();
            if (Namespaces.ODE_HTTP_EXTENSION_NS.equalsIgnoreCase(e.getNamespaceURI())
                    && "fault".equals(extensibilityElement.getElement().getLocalName())) {
                // name attribute is optional, but if any it must match the fault name
                if (e.hasAttribute("name")) {
                    return fault.getName().equals(e.getAttribute("name"));
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    public static Collection<UnknownExtensibilityElement> getHttpHeaders(List extensibilityElements) {
        final Collection<UnknownExtensibilityElement> unknownExtElements = CollectionsX.filter(extensibilityElements, UnknownExtensibilityElement.class);
        for (Iterator<UnknownExtensibilityElement> iterator = unknownExtElements.iterator(); iterator.hasNext();) {
            Element e = iterator.next().getElement();
            // keep only the header elements
            if (!Namespaces.ODE_HTTP_EXTENSION_NS.equalsIgnoreCase(e.getNamespaceURI())
                    || !"header".equals(e.getLocalName())) {
                iterator.remove();
            }
        }
        return unknownExtElements;
    }

    /**
     * Return the {@link  javax.wsdl.Fault} that has the given element as message part.
     *
     * @param operation the operation
     * @param elName    the qname to look for
     * @return the first fault for which the element of message part matches the given qname
     */
    @SuppressWarnings("unchecked")
    public static Fault inferFault(Operation operation, QName elName) {
        for (Fault f : (Collection<Fault>) operation.getFaults().values()) {
            if (f.getMessage() == null) continue;
            Collection<Part> parts = f.getMessage().getParts().values();
            if (parts.isEmpty()) continue;
            Part p = parts.iterator().next();
            if (p.getElementName() == null) continue;
            if (p.getElementName().equals(elName)) return f;
        }
        return null;
    }


    /**
     * ODE extends the wsdl spec by allowing definition of the HTTP verb at the operation level.
     * <br/>The current implementation implementations allows you to have a {@literal <binding xmlns="http://schemas.xmlsoap.org/wsdl/http/"/>} element
     * at the port level <strong>and</strong> at the operation level. In such a case the operation's verb overrides the port's verb.
     * <br/> This method applies the later rule.
     * <br/> If defined the operation's verb is returned, else the port's verb.
     *
     * @param binding
     * @param bindingOperation
     * @return If defined the operation's verb is returned, else the port's verb.
     * @see #getOperationVerb(javax.wsdl.BindingOperation)
     */
    public static String resolveVerb(Binding binding, BindingOperation bindingOperation) {
        final HTTPBinding httpBinding = (HTTPBinding) WsdlUtils.getBindingExtension(binding);
        String portVerb = httpBinding != null ? httpBinding.getVerb() : null;
        String operationVerb = WsdlUtils.getOperationVerb(bindingOperation);
        return operationVerb != null ? operationVerb : portVerb;
    }

}
