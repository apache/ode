/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
/*
 * This class implements a model of a SOAPMessage for processing via the 
 * SOAP Adapter (SOAPMessageMapper). This class should encapsulate all 
 * the functions require by the SOAPMessageMapper for processing a SOAPMessage
 * and expose all the properties it needs.  
 *  
 * @author <a href="mailto:kevin@kbedell.com">Kevin Bedell</a>
 * @version 1.0, Apr 30, 2004
 * 
 */
package com.fs.pxe.soap.mapping;

import com.fs.utils.DOMUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Loose wrapper around {@link Document} that provides access to common SOAP elements, and provides
 * rudimentary logic regarding the nature of a SOAP message (like is it a fault?).
 */
public class SoapMessage {
  private Document _soapMsg;

	private Element _soapEnv;
  private Element _soapHeader;
  private Element _soapBody;

  /** Is this a fault message? */
  private boolean _isFault;

  /**
   * The payload, normally the first body element, but in the case of faults this is
   * the first child of the detail element.
   */
  private Element _payload;

  /** Map of fault details. */
  private Map<QName, Element> _faultDetails;

  /** Value of the faultstring element. */
  private String _faultString;

  /** Value of the faultcode element. */
  private String _faultCode;

  private QName _payloadQName;

  /**
	 * Constructor.
	 * @param soapMsg The SOAP XML document
	 */
	public SoapMessage(Document soapMsg)
		throws SoapFormatException {

    if (soapMsg == null)
      throw new IllegalArgumentException("Null SOAP document!");
    _soapMsg = soapMsg;
		_soapEnv = soapMsg.getDocumentElement();
    if (_soapEnv == null)
      throw new IllegalArgumentException("Incomplete document (no root element).");
    if (!qname(_soapEnv).equals(Soap11Constants.QNAME_ENVELOPE))
      throw new SoapFormatException("Invalid root element: " + qname(_soapEnv));

    for (Node i = _soapEnv.getFirstChild(); i != null; i = i.getNextSibling()) {
      switch (i.getNodeType()) {
        case Node.ELEMENT_NODE:
          Element el = (Element) i;
          QName elName = qname(el);
          if (elName.equals(Soap11Constants.QNAME_HEADER)) {
            if (_soapHeader == null)
              _soapHeader = el;
            else
              throw new SoapFormatException("Duplicate <soap:header> element!");
          } else if (elName.equals(Soap11Constants.QNAME_BODY)) {
            if (_soapBody == null)
              _soapBody = el;
            else
              throw new SoapFormatException("Duplicate <soap:body> element!");
          }
      }
    }

    if (_soapBody == null)
      throw new SoapFormatException("Missing <soap:body> element!");

    Element bodyelement = DOMUtils.getFirstChildElement(_soapBody);

    if (bodyelement != null) {
      QName bodyElQName  = new QName(bodyelement.getNamespaceURI(), bodyelement.getLocalName());

      if (bodyElQName.equals(Soap11Constants.QNAME_FAULT)) {
        _isFault = true;
        _faultDetails = new HashMap<QName, Element>();
        
        for (Node i = bodyelement.getFirstChild(); i != null; i = i.getNextSibling()) {
          switch (i.getNodeType()) {
            case Node.ELEMENT_NODE:
              Element el = (Element) i;
              if (el.getLocalName().equals("faultcode")) {
                _faultCode = DOMUtils.getChildCharacterData(el);
              } else if (el.getLocalName().equals("faultstring")) {
                _faultString = DOMUtils.getChildCharacterData(el);
              } else if (el.getLocalName().equals("detail")) {
                _faultDetails = new HashMap<QName, Element>();

                for (Node j = el.getFirstChild(); j != null; j= j.getNextSibling()) {
                  if (j.getNodeType() != Node.ELEMENT_NODE)
                    continue;
                  Element faultEl = (Element) j;
                  QName faultQName = new QName(faultEl.getNamespaceURI(),  faultEl.getLocalName());
                  _faultDetails.put(faultQName, faultEl);
                }
              }
              // We happily ignore anything else.
          }
        }
      } else /** Not a fault. */ {
        _payload = bodyelement;
        _payloadQName = bodyElQName;
      }

    }

	}

  public SoapMessage(InputStream payloadInputStream) throws SoapFormatException, IOException, SAXException
  {
    this(DOMUtils.parse(payloadInputStream));
  }

  public SoapMessage(String faultCode, String faultString, String actor)  throws SoapFormatException {
    this(genFaultDoc(faultCode,faultString, actor));
  }


  /**
   * Returns the QName of the operation being invoked on the SOAPMessage.
   * @return QName The QName of the operation being invoked
   */
  public QName getPayloadQName() {
    return _payloadQName;
  }

	/**
	 * This method returns the soap:body element.
	 */
	public Element getSoapBody() {
		return _soapBody;
	}

	/**
	 * This method returns the SOAPEnvelope carried by the SOAPMessage
	 * @return  The SOAPEnvelope carried by the SOAPMessage
	 */
	public Element getSoapEnv() {
		return _soapEnv;
	}

  public Element getSoapHeader() {
    return _soapHeader;
  }

  public Element getPayload() {
    return _payload;
  }

  public boolean isFault() {
    return _isFault;
  }

  public String faultCode() {
    return _faultCode;
  }

  public Map<QName, Element> getFaultDetails() {
    return _faultDetails;
  }

  public Element getFaultDetail(QName elQName) {
    return _faultDetails.get(elQName);
  }

  private static QName qname(Element e) {
    return new QName(e.getNamespaceURI(), e.getLocalName());
  }


  public void writeTo(OutputStream bos) throws IOException {
    // TODO: Use proper stream serialization!
    String str = DOMUtils.domToString(_soapMsg);
    bos.write(str.getBytes());
  }

  private static Document genFaultDoc(String faultCode, String faultString, String actor) {
    Document msg = DOMUtils.newDocument();
    Element soapEnv = msg.createElementNS(Soap11Constants.QNAME_ENVELOPE.getNamespaceURI(), Soap11Constants.QNAME_ENVELOPE.getLocalPart());
    soapEnv.setPrefix("soapenv");
    msg.appendChild(soapEnv);
    Element soapBody = msg.createElementNS(Soap11Constants.QNAME_BODY.getNamespaceURI(), Soap11Constants.QNAME_BODY.getLocalPart());
    soapBody.setPrefix("soapenv");
    soapEnv.appendChild(soapBody);
    Element soapFault = msg.createElementNS(Soap11Constants.QNAME_FAULT.getNamespaceURI(), Soap11Constants.QNAME_FAULT.getLocalPart());
    soapFault.setPrefix("soapenv");
    soapBody.appendChild(soapFault);
    Element faultCodeEl = msg.createElementNS(null, "faultcode");
    soapFault.appendChild(faultCodeEl);
    faultCodeEl.appendChild(msg.createTextNode("soapenv:"+faultCode));
    Element faultStringEl = msg.createElementNS(null, "faultstring");
    soapFault.appendChild(faultStringEl);
    if (faultString != null)
      faultStringEl.appendChild(msg.createTextNode(faultString));
    return msg;
  }

  public String getFaultString() {
    return _faultString;
  }

  public boolean isValidResponse() {
    return true; // todo: better checking of validitiy
  }

}
