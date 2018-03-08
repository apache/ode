/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.ode.bpel.extension.bpel4restlight;

import java.io.IOException;

import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.eapi.ExtensionContext;
import org.apache.ode.bpel.o.OScope;
import org.apache.ode.bpel.o.OScope.Variable;
import org.apache.ode.bpel.o.OVarType;
import org.apache.ode.bpel.o.OXsdTypeVarType;
import org.apache.ode.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * This class provides some utility methods for the BPEL REST extension activities.
 * 
 * @author Michael Hahn (mhahn.dev@gmail.com)
 * 
 */
public class Bpel4RestLightUtil {

    private static final String VARIABLE_VALUE_REFERENCE = "$bpelvar[";

    /**
     * This method extracts the request message payload from the provided extension activity. This
     * request payload is either provided through a specified BPEL variable ('request' attribute) or
     * statically defined within the process model as the child node of the extension activity.
     * 
     * @param context The extension context required to resolve variable values
     * @param element The extension activity DOM element containing the request payload
     * @return The request message payload
     * 
     * @throws FaultException
     */
    public static String extractRequestPayload(ExtensionContext context, Element element)
            throws FaultException {
        String requestPayload = null;

        String requestPayloadVariableName =
                getMethodAttributeValue(context, element, MethodAttribute.REQUEST_PAYLOAD_VARIABLE);

        // Check if a reference to a variable is specified
        if (requestPayloadVariableName != null && !requestPayloadVariableName.isEmpty()) {
            // Get the request variable value
            Node requestVariableNode = context.readVariable(requestPayloadVariableName);

            // Check if the specified variable provides data
            if (requestVariableNode != null) {
                requestPayload = variableData2String(requestVariableNode);
            }
        }

        // If no variable was specified or the variable doesn't provide data, we check
        // if a static request payload is specified as the child element of the
        // extension activity
        if (requestPayload == null) {
            Node request = DOMUtils.findChildByType(element, Node.ELEMENT_NODE);

            if (request != null) {
                requestPayload = DOMUtils.domToString(request);
            }
        }

        if (requestPayload == null) {
            throw new FaultException(Bpel4RestLightExtensionBundle.FAULT_QNAME,
                    "REST extension activity does not specify any request payload.");
        }

        return requestPayload;
    }

    /**
     * This method writes the response payload to a specified BPEL variable.
     * 
     * @param context The extension context required to resolve a variable and write a new value to
     *        it
     * @param responsePayload The payload of the response which will be written to the specified
     *        variable
     * @param processVariableName The name of the variable to write to
     * @throws FaultException
     */
    public static void writeResponsePayload(ExtensionContext context, Object responsePayload,
            String processVariableName) throws FaultException {
        if (responsePayload != null && !responsePayload.toString().isEmpty()) {
            OScope.Variable bpelVariable = context.getVisibleVariables().get(processVariableName);

            // Create a new instance of the variables' type, to see if we need a wrapper
            Document doc = DOMUtils.newDocument();
            Node val = bpelVariable.type.newInstance(doc);

            // Check if we need a temporary simple type wrapper
            if (val.getNodeType() == Node.TEXT_NODE) {
                // Create a wrapper element and add the response payload as text node
                Element tempwrapper = doc.createElementNS(null, "temporary-simple-type-wrapper");
                doc.appendChild(tempwrapper);
                tempwrapper.appendChild(val);

                // Set the response payload
                val.setTextContent(responsePayload.toString());

                // Return the wrapper element
                val = tempwrapper;
            } else {
                // Convert the structured XML response payload to DOM
                try {
                    val = DOMUtils.stringToDOM(responsePayload.toString());
                } catch (SAXException e) {
                    throw new FaultException(Bpel4RestLightExtensionBundle.FAULT_QNAME,
                            "BPEL4REST: Writing the response payload to BPEL variable '"
                                    + processVariableName + "' caused an exception: "
                                    + e.getMessage(),
                            e);
                } catch (IOException e) {
                    throw new FaultException(Bpel4RestLightExtensionBundle.FAULT_QNAME,
                            "BPEL4REST: Writing the response payload to BPEL variable '"
                                    + processVariableName + "' caused an exception: "
                                    + e.getMessage(),
                            e);
                }
            }

            // Write the variable value
            context.writeVariable(bpelVariable, val);
        }
    }

    public static String extractAcceptHeader(ExtensionContext context, Element element)
            throws FaultException {
        return getMethodAttributeValue(context, element, MethodAttribute.ACCEPT_HEADER);
    }

    /**
     * This method extracts special predefined attributes (see {@link MethodAttribute}) from an
     * extension activity. Therefore, references to a variable value via '$bpelVar[varName]' are
     * also automatically resolved.
     * 
     * @param context The extension context required to resolve variable values
     * @param element The extension activity DOM element containing the attribute value
     * @param methodAttribute Attribute whose content has to be returned
     * @return The value of the attribute
     * @throws FaultException
     */
    public static String getMethodAttributeValue(ExtensionContext context, Element element,
            MethodAttribute methodAttribute) throws FaultException {

        String result = "";

        switch (methodAttribute) {

            case REQUEST_URI:
                result = element.getAttribute("uri");

                if (result == null || result.isEmpty()) {
                    result = element.getAttribute("requestUri");
                } else {
                    // Resolve a possible variable value reference
                    result = resolveVariableValueReference(context, result);
                }

                break;
            case REQUEST_PAYLOAD_VARIABLE:
                result = element.getAttribute("request");

                if (result == null || result.isEmpty()) {
                    result = element.getAttribute("requestPayload");
                }
                break;
            case RESPONSE_PAYLOAD_VARIABLE:
                result = element.getAttribute("response");

                if (result == null || result.isEmpty()) {
                    result = element.getAttribute("responsePayload");
                }
                break;
            case STATUS_CODE_VARIABLE:
                result = element.getAttribute("statusCode");
                break;
            case ACCEPT_HEADER:
                result = element.getAttribute("accept");

                // Resolve a possible variable value reference
                if (result != null && !result.isEmpty()) {
                    result = resolveVariableValueReference(context, result);
                }

                break;
        }

        return result;
    }

    /**
     * Resolves references to variable values specified in an extension activity via
     * '$bpelVar[varName]'.
     * 
     * @param context The extension context to lookup and resolve variables and their values.
     * @param variableValueReference A potential variable value reference.
     * 
     * @return If the 'variableValueReference' parameter contains a variable value reference
     *         ($bpelVar[varName]), the actual value of the variable is returned, else the provided
     *         parameter value is returned.
     * @throws FaultException
     */
    public static String resolveVariableValueReference(ExtensionContext context,
            String variableValueReference) throws FaultException {
        String variableValue = variableValueReference;

        // Check if a concrete variable name ("varName") or a reference to the value of
        // a variable
        // is specified ("$bpelVar[varName]")
        if (variableValueReference.startsWith(VARIABLE_VALUE_REFERENCE)) {
            String variableName = variableValueReference.substring(
                    variableValueReference.indexOf("[") + 1, variableValueReference.indexOf("]"));

            Variable variable = context.getVisibleVariables().get(variableName);

            // We only support simple type variables, therefore the value of the variable is
            // directly provided within a <temporary-simple-type-wrapper/> element.
            if (variable != null && isSimpleType(variable.type)) {
                Node variableContent = context.readVariable(variableName);

                if (variableContent.getTextContent() != null) {
                    variableValue = variableContent.getTextContent();
                }
            } else {
                throw new FaultException(Bpel4RestLightExtensionBundle.FAULT_QNAME,
                        "References to the value of a BPEL variable using '$bpelVar[varName]' only support simple type variables.");
            }
        }

        return variableValue;
    }

    public static String variableData2String(Node variableData) {
        String result = null;

        if (variableData != null) {
            if ("temporary-simple-type-wrapper".equals(variableData.getLocalName())) {
                result = variableData.getTextContent();
            } else {
                result = DOMUtils.domToString(variableData);
            }
        }

        return result;
    }

    /**
     * Checks if the type is a simple type or not.
     * 
     * @param type to check
     * 
     * @return True, if the type is simple, False otherwise.
     */
    private static boolean isSimpleType(OVarType type) {
        boolean result = false;

        if (type instanceof OXsdTypeVarType) {
            result = ((OXsdTypeVarType) type).simple;
        }

        return result;
    }
}
