package de.unistuttgart.iaas.bpel.util;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.runtime.common.extension.ExtensionContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Copyright 2011
 * 
 * @author Uwe Breitenbuecher
 * 
 *         This class provides some methods for BPEL-Variable-Injection
 */
public class BPELVariableInjectionUtil {
	
	/**
	 * This method serializes a Node into a String
	 * 
	 * @param node
	 * @return String representation of the node
	 */
	public static String nodeToString(Node node) {
		try {
			
			if (node != null && node.getLocalName().equals("temporary-simple-type-wrapper")) {
				// this is a temporary hack for string variables and the likes,
				// as you may see ODE wrappes simpletypes in wrapper-elements,
				// but this isn't great here
				return node.getTextContent();
			}
			
			// Create transformer
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			
			// Transform Node into a String representation by regarding some
			// formatting rules
			StringWriter stringWriter = new StringWriter();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");
			transformer.transform(new DOMSource(node), new StreamResult(stringWriter));
			
			// Return build string
			return stringWriter.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// If any error occurs, return empty string
		return "";
	}
	
	/**
	 * This method executes the BPEL-Variable-Injection. It replaces referenced
	 * BPEL-Variables with corresponding content
	 * 
	 * @param context ExtensionContext of process
	 * @param element DOM-Representation of the BPEL-Code in which the
	 *            Variable-Injection has to be done
	 * @return modified BPEL-Code as DOM-Representation
	 */
	public static Element replaceExtensionVariables(ExtensionContext context, Element element) {
		
		try {
			String BPELCodeAsString;
			
			// Transform BPEL-Code (DOM-Representation) into a String
			BPELCodeAsString = nodeToString(element);
			
			// Find and replace referenced BPEL-Variables
			int startIndex = BPELCodeAsString.indexOf("$bpelvar[");
			if (startIndex != -1) {
				while (startIndex != -1) {
					int endIndex = startIndex;
					while (BPELCodeAsString.charAt(endIndex) != ']') {
						endIndex++;
					}
					
					// Extract name of referenced variable
					String variableName = BPELCodeAsString.substring(startIndex + 9, endIndex);
					
					// Extract content of referenced variable
					Node variableContent = context.readVariable(variableName);
					
					System.out.println("Replacing variable " + variableName + "(" + variableContent.getNamespaceURI() + " " + variableContent.getLocalName() + ") with content: \n");
					System.out.println("NodeValue(): " + variableContent.getNodeValue() + "\n");
					System.out.println("TextContent(): " + variableContent.getTextContent());
					System.out.println("The full bpel script (before change) as string: \n" + BPELCodeAsString + "\n");
					
					// Replace variable-reference with corresponding content
					BPELCodeAsString = BPELCodeAsString.replace("$bpelvar[" + variableName + "]", nodeToString(variableContent));
					
					System.out.println("The full bpel script as string: \n" + BPELCodeAsString + "\n");
					startIndex = BPELCodeAsString.indexOf("$bpelvar[");
				}
				
				// Transform modified code (String) into DOM-Representation
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				factory.setNamespaceAware(true);
				DocumentBuilder builder = factory.newDocumentBuilder();
				
				InputSource inputSource = new InputSource();
				inputSource.setCharacterStream(new StringReader(BPELCodeAsString));
				Document newDocument = builder.parse(inputSource);
				
				// Return first child (because Document root is not needed)
				return (Element) newDocument.getFirstChild();
				
			} else {
				
				// If no referenced variables are found, return original code
				return element;
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FaultException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
}
