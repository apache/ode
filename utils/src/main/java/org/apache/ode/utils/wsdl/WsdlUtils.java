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

import org.apache.ode.utils.stl.CollectionsX;

import javax.wsdl.Binding;
import javax.wsdl.Port;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingInput;
import javax.wsdl.Service;
import javax.wsdl.Definition;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.mime.MIMEContent;
import javax.wsdl.extensions.mime.MIMEMultipartRelated;
import javax.wsdl.extensions.http.HTTPBinding;
import javax.wsdl.extensions.http.HTTPOperation;
import javax.wsdl.extensions.http.HTTPUrlEncoded;
import javax.wsdl.extensions.http.HTTPUrlReplacement;
import javax.wsdl.extensions.http.HTTPAddress;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
     * Test if the given binding uses a Http binding.
     *
     * @param binding
     * @return true if {@link HTTPBinding} is assignable from the binding
     * @see #getBindingExtension(javax.wsdl.Binding)
     */
    public static boolean useHTTPBinding(Binding binding) {
        ExtensibilityElement element = getBindingExtension(binding);
        return HTTPBinding.class.isAssignableFrom(element.getClass());
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
     * throw an {@link IllegalArgumentException} if no or multiple bindings found.
     *
     * @param port
     * @return an instance of {@link SOAPBinding} or {@link HTTPBinding}
     */
    public static ExtensibilityElement getBindingExtension(Binding binding) {
        Collection bindings = new ArrayList();
        CollectionsX.filter(bindings, binding.getExtensibilityElements(), HTTPBinding.class);
        CollectionsX.filter(bindings, binding.getExtensibilityElements(), SOAPBinding.class);
        if (bindings.size() == 0) {
            // exception if no bindings found
            throw new IllegalArgumentException(msgs.msgNoBinding(binding.getQName()));
        } else if (bindings.size() > 1) {
            // exception if multiple bindings found
            throw new IllegalArgumentException(msgs.msgMultipleBindings(binding.getQName()));
        } else {
            // retrieve the single element
            ExtensibilityElement result = (ExtensibilityElement) bindings.iterator().next();
            return result;
        }
    }

    public static ExtensibilityElement getBindingExtension(Port port) {
        Binding binding = port.getBinding();
        if (binding == null) {
            throw new IllegalArgumentException(msgs.msgBindingNotFound(port.getName()));
        }

        return getBindingExtension(binding);
    }

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

    public static boolean useUrlEncoded(BindingInput bindingInput) {
        Collection<HTTPUrlEncoded> coll = CollectionsX.filter(bindingInput.getExtensibilityElements(), HTTPUrlEncoded.class);
        return !coll.isEmpty();
    }

    public static boolean useUrlReplacement(BindingInput bindingInput) {
        Collection<HTTPUrlReplacement> coll = CollectionsX.filter(bindingInput.getExtensibilityElements(), HTTPUrlReplacement.class);
        return !coll.isEmpty();
    }

    public static boolean useMimeMultipartRelated(BindingInput bindingInput) {
        Collection<MIMEMultipartRelated> coll = CollectionsX.filter(bindingInput.getExtensibilityElements(), MIMEMultipartRelated.class);
        return !coll.isEmpty();
    }

    public static String getMimeContentType(List extensibilityElements) {
        Collection<MIMEContent> coll = CollectionsX.filter(extensibilityElements, MIMEContent.class);
        if (coll.size() == 0) {
            return null;
        } else if (coll.size() > 1) {
            // exception if multiple contents found
            throw new IllegalArgumentException(msgs.msgMultipleMimeContent());
        } else {
            // retrieve the single element
            MIMEContent mimeContent = coll.iterator().next();
            return mimeContent.getType();
        }
    }

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


}
