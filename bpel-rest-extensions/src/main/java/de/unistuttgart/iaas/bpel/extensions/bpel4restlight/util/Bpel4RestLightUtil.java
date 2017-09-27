package de.unistuttgart.iaas.bpel.extensions.bpel4restlight.util;

import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.runtime.common.extension.ExtensionContext;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import de.unistuttgart.iaas.bpel.extensions.bpel4restlight.Bpel4RestLightOperation;
import de.unistuttgart.iaas.bpel.extensions.bpel4restlight.MethodAttribute;
import de.unistuttgart.iaas.xml.DomXmlConverter;

/**
 * Copyright 2011 IAAS University of Stuttgart <br>
 * <br>
 * 
 * @author uwe.breitenbuecher@iaas.uni-stuttgart.de
 * 
 */
public class Bpel4RestLightUtil {
	
	/**
	 * This function extracts the requestPayload specified in the passed
	 * element. This requestPayload is either the content contained in a special
	 * BPEL-Variable which is referenced by name by a special attribute of the
	 * passed element or the content contained in the first child node of the
	 * passed element
	 * 
	 * @param context ExtensionContext
	 * @param element Element from which the requestPayload has to be extracted
	 * @return RequestPayload as String
	 * @throws FaultException
	 */
	public static String extractRequestPayload(ExtensionContext context, Element element) throws FaultException {
		
		String requestPayload = "";
		
		String requestPayloadVariableName = getMethodAttributeValue(element, MethodAttribute.REQUESTPAYLOADVARIABLE);
		
		if (requestPayloadVariableName != null && requestPayloadVariableName != "") {
			Node requestVariableNode = context.readVariable(requestPayloadVariableName);
			if (requestVariableNode.getLocalName().equals("temporary-simple-type-wrapper")) {
				Bpel4RestLightOperation.wrapper = "temporary-simple-type-wrapper";
				requestPayload = DomXmlConverter.nodeToString(requestVariableNode, "temporary-simple-type-wrapper");
			} else {
				requestPayload = DomXmlConverter.nodeToString(requestVariableNode, null);
			}
			System.out.println("The pure request variable as String: \n" + DomXmlConverter.nodeToString(requestVariableNode, null) + "\n");
		}
		
		return requestPayload;
	}
	
	public static String extractAcceptHeader(ExtensionContext context, Element element) throws FaultException {
		return getMethodAttributeValue(element, MethodAttribute.ACCEPTHEADER);
	}
	
	/**
	 * This function extracts special predefined attributes (see
	 * {@link MethodAttribute}) from a passed DOM-Element
	 * 
	 * @param element Element containing the requested Attribute-Value
	 * @param methodAttribute Attribute whose content has to be returned
	 * @return Value / Content of the attribute
	 */
	public static String getMethodAttributeValue(Element element, MethodAttribute methodAttribute) {
		
		String result = "";
		
		switch (methodAttribute) {
		
			case REQUESTURI:
				result = element.getAttribute("uri");
				
				if (result == null || "".equals(result)) {
					result = element.getAttribute("requestUri");
				}
				break;
			case REQUESTPAYLOADVARIABLE:
				result = element.getAttribute("request");
				
				if (result == null || "".equals(result)) {
					result = element.getAttribute("requestPayload");
				}
				break;
			case RESPONSEPAYLOADVARIABLE:
				result = element.getAttribute("response");
				
				if (result == null || "".equals(result)) {
					result = element.getAttribute("responsePayload");
				}
				break;
			case STATUSCODEVARIABLE:
				result = element.getAttribute("statusCode");
				break;
			case ACCEPTHEADER:
				result = element.getAttribute("accept");
				break;
		}
		
		return result;
	}
	
}
