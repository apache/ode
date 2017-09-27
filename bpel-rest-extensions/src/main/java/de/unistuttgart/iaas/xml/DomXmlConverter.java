package de.unistuttgart.iaas.xml;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * Copyright 2011 IAAS University of Stuttgart <br>
 * <br>
 * 
 * @author uwe.breitenbuecher@iaas.uni-stuttgart.de
 * 
 */
public class DomXmlConverter {
	
	// Single instance of transformer
	private static Transformer xmlTransformer;
	
	
	/**
	 * Converts a Node to its String-representation
	 * 
	 * @param node Node which has to be converted
	 * @return String representation of the passed node
	 */
	public static String nodeToString(Node node, String wrapperElement) {
		try {
			System.out.println("\n\n\n");
			System.out.println("check if node got a namespace: " + node.getNamespaceURI());
			if (wrapperElement != null) {
				// this hack is need as ODE wrapps simpletypes in such elements
				return node.getTextContent();
			}
			
			Source source = new DOMSource(node);
			
			StringWriter writer = new StringWriter();
			Result result = new StreamResult(writer);
			
			Transformer transformer = DomXmlConverter.getTransformer();
			transformer.transform(source, result);
			
			return writer.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "Parsing error";
	}
	
	/**
	 * Singleton implementation of transformer access
	 * 
	 * @return Transformer
	 * @throws Exception
	 */
	private static synchronized Transformer getTransformer() throws Exception {
		if (DomXmlConverter.xmlTransformer == null) {
			DomXmlConverter.xmlTransformer = TransformerFactory.newInstance().newTransformer();
			DomXmlConverter.xmlTransformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			DomXmlConverter.xmlTransformer.setOutputProperty(OutputKeys.INDENT, "yes");
			DomXmlConverter.xmlTransformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		}
		return DomXmlConverter.xmlTransformer;
	}
	
	/**
	 * This method converts a NodeList into a List of Strings. Each string
	 * represents the TextContent of each Node contained in the NodeList
	 * 
	 * @param nodeList which contains the Nodes
	 * @return List of TextContents of each node
	 */
	public static List<String> convertNodeListToStringList(NodeList nodeList) {
		List<String> resultList = new ArrayList<String>();
		
		for (int i = 0; i < nodeList.getLength(); i++) {
			resultList.add(nodeList.item(i).getTextContent());
		}
		
		return resultList;
	}
	
	/**
	 * This method converts a NodeList into a List of Nodes
	 * 
	 * @param nodeList
	 * @return List of Nodes
	 */
	public static List<Node> convertNodeListToList(NodeList nodeList) {
		List<Node> resultList = new ArrayList<Node>(nodeList.getLength());
		for (int i = 0; i < nodeList.getLength(); i++) {
			resultList.add(nodeList.item(i));
		}
		return resultList;
	}
	
	
	/**
	 * Helper-Class for converting an Object into its DOM-Representation. The
	 * SerializingContainer is a Wrapper to enable the serialization of any
	 * object via JAXB.
	 */
	@XmlRootElement
	private static class SerializingContainer {
		
		Object object;
		
		
		public Object getObject() {
			return this.object;
		}
		
		public void setObject(Object object) {
			this.object = object;
		}
		
	}
	
	
	/**
	 * This methods converts an Object into its DOM-Representation
	 * 
	 * @param object which have to be converted
	 * @return DOM-Representation of the object
	 */
	public static Node convertObjectToDom(Object object) {
		try {
			
			// Create new SerializingContainer and pack the object, which has to
			// be serialized, into it. This has to be done, because JAXB
			// only marshalls objects of classes annotated with the
			// @XmlRootElement-Annotation.
			SerializingContainer container = new SerializingContainer();
			container.setObject(object);
			
			// Create empty Document
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			
			// Create JAXBContext and bind classes
			Class<?>[] classesToBeBound = new Class[] {SerializingContainer.class, container.getObject().getClass()};
			JAXBContext jaxbContext = JAXBContext.newInstance(classesToBeBound, null);
			
			Marshaller marshaller = jaxbContext.createMarshaller();
			
			// Set some properties
			marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
			marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
			
			// Marshall container into document.
			marshaller.marshal(container, document);
			
			// Extract only the contained information in the serialized
			// DOM-Representation of the SerializingContainer
			return document.getFirstChild().getFirstChild();
			
		} catch (Exception e) {
			return null;
		}
	}
	
}
