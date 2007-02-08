package org.apache.ode.axis2;

import javax.wsdl.Definition;
import javax.wsdl.Operation;
import javax.wsdl.PortType;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.apache.axiom.om.impl.llom.factory.OMXMLBuilderFactory;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.axiom.soap.impl.dom.soap11.SOAP11Factory;
import org.apache.axis2.AxisFault;
import org.apache.ode.axis2.util.SoapMessageConverter;
import org.apache.ode.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class SoapMessageConverterTestFromRaja1 extends TestCase {
    String wsdl1tns;
    Definition wsdl1;
    String portName = "RepoAccessor";
    SoapMessageConverter portmapper;
    
    PortType portType;
    Document req1;
    private Operation op1;
    
    public SoapMessageConverterTestFromRaja1() throws Exception {
    }
    
    
    public void setUp() throws Exception {
        req1 = DOMUtils.parse(getClass().getResourceAsStream("/raja1req.soap"));  
        WSDLReader reader = WSDLFactory.newInstance().newWSDLReader();
        wsdl1 = reader.readWSDL(getClass().getResource("/raja1.wsdl").toExternalForm());
        wsdl1tns = wsdl1.getTargetNamespace();
        portType = wsdl1.getPortType(new QName(wsdl1tns,"SimpleServicesPortType"));
        op1 = portType.getOperation("addNumbers", null, null);
        portmapper = new SoapMessageConverter(new SOAP11Factory(),
                wsdl1, new QName(wsdl1tns,"SimpleServices"), 
                "SimpleServicesHttpPort", true);
    }
    
    public void tearDown() {
        
    }
    
    public void testParseSOAPRequest() throws Exception {
        XMLStreamReader sr = XMLInputFactory.newInstance().createXMLStreamReader(getClass().getResourceAsStream("/raja1req.soap"));
        StAXSOAPModelBuilder builder = OMXMLBuilderFactory.createStAXSOAPModelBuilder(new SOAP11Factory(),sr);

        SOAPEnvelope env = builder.getSOAPEnvelope();
        Element msg = DOMUtils.stringToDOM("<message/>");
        portmapper.parseSoapRequest(msg,env,op1);
        System.out.println(DOMUtils.domToString(msg));
        
    }

}
