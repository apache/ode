package de.unistuttgart.iaas.xml;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

/**
 * 
 * Copyright 2011 IAAS University of Stuttgart <br>
 * <br>
 * 
 * @author uwe.breitenbuecher@iaas.uni-stuttgart.de
 * 
 */
public class XPathEvaluator {
	
	public static XPath xpath = XPathFactory.newInstance().newXPath();
	
	
	@SuppressWarnings("unchecked")
	public static <t> t evaluate(String expression, Object source, QName returnType) {
		
		Object resultAsObject = null;
		try {
			resultAsObject = xpath.evaluate(expression, source, returnType);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return (t) resultAsObject;
	}
	
}
