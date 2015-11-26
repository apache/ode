package org.apache.ode.bpel.obj.migrate;

import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

/**
 * DeepEqual of two DomElement.
 * @see DeepEqualityHelper
 */
public class DomElementComparator implements EqualityComparator{
    private static final Logger __log = LoggerFactory.getLogger(DomElementComparator.class);
	private DeepEqualityHelper deepEquality;
	@Override
	public Boolean objectsEqual(Object obj1, Object obj2) {
		if (obj1 == obj2) return true;
		if (!(obj2 instanceof Element)){
			if (!deepEquality.logFalseThrough){
				__log.debug("Unequal in Dom Element: Type mismatch. " + deepEquality.getSt()
						+ "Object2 has type " + obj2.getClass());
			}
			return false;
		}
		try{
			String str1 = Element2String((Element)obj1);
			String str2 = Element2String((Element)obj2);
			boolean e =  str1.equals(str2);
		if (!e){
			if (!deepEquality.logFalseThrough){
				__log.debug("Unequal in Dom Element: " + deepEquality.getSt() + 
					"\n" + str1 + "\nand\n " + str2);
			}
		}
		return e;
		}catch(Exception e){
			if (!deepEquality.logFalseThrough){
				__log.debug("Unequal in Dom Element: Exception when comparing. " + 
						deepEquality.getSt() + e);
			}
			return false;
		}
	}

	public String Element2String(Element node){
		Document document = node.getOwnerDocument();
		DOMImplementationLS domImplLS = (DOMImplementationLS) document
		    .getImplementation();
		LSSerializer serializer = domImplLS.createLSSerializer();
		String str = serializer.writeToString(node);
		serializer .getDomConfig().setParameter("xml-declaration", false);
		return str;
	}
	/**
	 * Another option
	 */
	public String Element2String2(Element obj1) throws TransformerException{
		TransformerFactory transFactory = TransformerFactory.newInstance();
		Transformer transformer = transFactory.newTransformer();
		StringWriter buffer = new StringWriter();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		transformer.transform(new DOMSource(obj1),
		      new StreamResult(buffer));
		String str = buffer.toString();	
		return str;
	}
	@Override
	public Boolean canHanle(Object obj) {
		return obj instanceof Element;
	}

	public DeepEqualityHelper getDeepEquality() {
		return deepEquality;
	}
	
	@Override
	public void setDeepEquality(DeepEqualityHelper deepEquality) {
		this.deepEquality = deepEquality;
	}

}
