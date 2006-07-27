/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.soap.mapping;

import org.apache.ode.utils.msg.MessageBundle;
import org.apache.ode.utils.stl.CollectionsX;

import java.util.*;

import javax.wsdl.*;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPFault;
import javax.wsdl.extensions.soap.SOAPHeader;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.xml.namespace.QName;


/**
 * This class provides a 'model' of a WSDL document that we can use 
 * for processing SOAP messages. It's intent is to encapsulate all
 * aspects of WSDL processing for SOAP.
 */
public class SoapOperationBindingModel extends AbstractSoapBinding {
  private static final Messages __msgs = MessageBundle.getMessages(Messages.class);

  /** Binding style. */
	private boolean _rpcStyle;

  /** Part map (request): partMap->SOAPBody|SOAPHeader */
	private Map<String, ExtensibilityElement> _requestPartMap =
    new HashMap<String, ExtensibilityElement>();

  /** Part map (response): partMap->SOAPBody|SOAPHeader */
  private Map<String, ExtensibilityElement> _responsePartMap =
    new HashMap<String, ExtensibilityElement>();

  private BindingOperation _bindingOperation;
  private Operation _operation;
  private SOAPOperation _soapOperation;
  private QName _qname;
  private SOAPReader _requestReader;
  private SOAPWriter _requestWriter;
  private SOAPReader _responseReader;
  private SOAPWriter _responseWriter;
  private Map<String, SOAPWriter> _faultWriters = new HashMap<String, SOAPWriter>();
  private Map<String, SOAPReader> _faultReaders = new HashMap<String, SOAPReader>();
  private Map<QName, String> _knownFaults = new HashMap<QName, String>();


  /**
	 * Constructor.
   * @param bindingOperation
   */
  @SuppressWarnings("unchecked")
	public SoapOperationBindingModel(SoapBindingModel soapBindingModel, BindingOperation bindingOperation)
    throws SoapBindingException
  {
    _bindingOperation = bindingOperation;
    _operation = _bindingOperation.getOperation();
    _rpcStyle = soapBindingModel.isRpcStyle();
    Collection<SOAPOperation> soapOperations = CollectionsX.filter(_bindingOperation.getExtensibilityElements(), SOAPOperation.class);
    if (soapOperations.isEmpty()) {
      throw new SoapBindingException("Missing <soap:operation> extension for " + bindingOperation,
              "binding-operation", _operation.getName(), __msgs.msgNoSoapBindingForOperation());
    }
    else if (soapOperations.size() > 1) {
      throw new SoapBindingException("Multiple <soap:operation> extensions for " + bindingOperation,
              "binding-operation", _operation.getName(), __msgs.msgMultipleSoapBindingsForOperation());
    }
    _soapOperation = soapOperations.iterator().next();
    if (parseRpcStyle(_soapOperation.getStyle()) ^ _rpcStyle) {
      throw new BasicProfileBindingViolation("R2705", "binding-operation", _operation.getName(), __msgs.msgBP_R2705());
    }

    if (_operation.getInput() == null || _operation.getInput().getMessage() == null)
      throw new SoapBindingException("Missing input message for " + bindingOperation,
              "operation" , _operation.getName(), __msgs.msgNoInputMessage());

    if (_bindingOperation.getBindingInput() == null)
      throw new SoapBindingException("Missing input binding for " + bindingOperation,
              "binding-operation", _operation.getName(), __msgs.msgNoInputBinding());

    buildPartMap(_requestPartMap, _operation.getInput().getMessage(),
            Collections.checkedCollection(_bindingOperation.getBindingInput().getExtensibilityElements(),ExtensibilityElement.class));
    _requestReader = new SOAPReader(this, true);
    _requestWriter = new SOAPWriter(this, true);

    if (!isOneWay()) {
      if (_operation.getOutput() == null || _operation.getOutput().getMessage() == null)
        throw new SoapBindingException("Missing output message for " + bindingOperation,
                "operation" , _operation.getName(), __msgs.msgNoOutputMessage());
      
      if (_bindingOperation.getBindingOutput() == null)
        throw new SoapBindingException("Missing output binding for "+ bindingOperation,
                "binding-operation", _operation.getName(), __msgs.msgNoOutputBinding());

      buildPartMap(_responsePartMap, _operation.getOutput().getMessage(),
              Collections.checkedCollection(_bindingOperation.getBindingOutput().getExtensibilityElements(), ExtensibilityElement.class));
      buildFaultMap();
      _responseReader = new SOAPReader(this, false);
      _responseWriter = new SOAPWriter(this, false);
    }
	}


  /**
   * Find the fault that corresponds to a set of detail
   * elements.
   * @param detailNames set of detail QNAMEs
   * @return name of matching fault, or null if not found
   */
  public String findMatchingFault(Set<QName> detailNames) {
    for (Iterator<QName> i = detailNames.iterator(); i.hasNext(); ) {
      QName detailName = i.next();
      String faultName = _knownFaults.get(detailName);
      if (faultName != null) {
        return faultName;
      }
    }
    return null;
  }

  ExtensibilityElement getRequestPartBinding(String partName) {
    return _requestPartMap.get(partName);
  }

  ExtensibilityElement getResponsePartBinding(String partName) {
    return _responsePartMap.get(partName);
  }


	/**
	 * This method returns a boolean true or false depending on whether or not this is an RPC style document   
	 * @return true or false depending on whether or not the SOAP Style for this method is RPC style.
	 */
	public boolean isRPCStyle() {
		return _rpcStyle;
	}


	/**
	 * This method returns a boolean true or false depending on whether or not this is an Document style document   
	 * @return true or false depending on whether or not the SOAP Style for this method is Document style.
	 */
	public boolean isDocumentStyle() {
		return !_rpcStyle;
	}

  private void buildFaultMap() throws SoapBindingException {
    for (Iterator i = _bindingOperation.getBindingFaults().entrySet().iterator(); i.hasNext(); ) {
      Map.Entry me = (Map.Entry) i.next();
      BindingFault bindingFault = (BindingFault) me.getValue();
      Collection soapBindingFaults = CollectionsX.filter(bindingFault.getExtensibilityElements(), SOAPFault.class);
      SOAPFault soapBindingFault = (SOAPFault) (soapBindingFaults.isEmpty() ? null : soapBindingFaults.iterator().next());
      if (soapBindingFault == null) {
        // No SOAP binding for the fault, may cause a little bit of a surprise
        // to the user.
        continue;
      }

      String faultName = soapBindingFault.getName();
      if (faultName == null || "".equals(faultName))
        throw new BasicProfileBindingViolation("R2721", "binding-operation",
                _bindingOperation.getName(), __msgs.msgBP_R2721());

      Fault wsdlFault = _operation.getFault(faultName);
      if (wsdlFault == null)
        throw new BasicProfileBindingViolation("R2754", "binding-operation",
                _bindingOperation.getName(),__msgs.msgBP_R2754());

      if (wsdlFault.getMessage().getParts().size() != 1)
        throw new SoapBindingException("Fault message must contain exactly one part!",
                "operation", _operation.getName(),
                __msgs.msgFaultMustHaveExactlyOnePart(faultName));

      Part faultPart = (Part) wsdlFault.getMessage().getParts().values().iterator().next();
      if (faultPart.getTypeName() != null || faultPart.getElementName() == null)
        throw new BasicProfileBindingViolation("R2205", "operation", _operation.getName(),
                __msgs.msgBP_R2205(faultPart.getName()));

      if (soapBindingFault.getNamespaceURI() != null)
        throw _rpcStyle
                ? new BasicProfileBindingViolation("R2726", "binding-operation", _bindingOperation.getName(),
                        __msgs.msgBP_R2726(faultPart.getName()))
                : new BasicProfileBindingViolation("R2716", "binding-operation", _bindingOperation.getName(),
                        __msgs.msgBP_R2716(faultPart.getName()));

      if (soapBindingFault.getUse() != null && !soapBindingFault.getUse().equals("literal"))
        throw new BasicProfileBindingViolation("R2706","binding-operation", _bindingOperation.getName(),
                __msgs.msgBP_R2706(faultPart.getName()));

      _faultWriters.put(faultName, new SOAPWriter(soapBindingFault, faultPart));
      _faultReaders.put(faultName, new SOAPReader(this, soapBindingFault));
      _knownFaults.put(faultPart.getElementName(), wsdlFault.getName());
    }

  }

  @SuppressWarnings("unchecked")
  private void buildPartMap(Map<String, ExtensibilityElement> partMap,
      Message wsdlMessage, Collection<ExtensibilityElement> ioExtElements)
          throws SoapBindingException
  {
    for (Iterator<ExtensibilityElement> i = ioExtElements.iterator(); i.hasNext(); ) {
      ExtensibilityElement obj = i.next();
      if (obj instanceof SOAPBody) {
        SOAPBody soapBody = (SOAPBody) obj;
        if (!soapBody.getUse().equals("literal")) {
          throw new BasicProfileBindingViolation("R2706", "binding-operation",
                  _bindingOperation.getName(),
                  __msgs.msgBP_R2706());
        }
        // Parts to map into body...
        Collection<String> parts = soapBody.getParts() == null
                ? Collections.checkedCollection(wsdlMessage.getParts().keySet(), String.class)
                : Collections.checkedCollection(soapBody.getParts(), String.class);

        if (_rpcStyle) {
          // this may be qname of output message, don't overwrite qname
          if(_qname == null) {
          	_qname = new QName(soapBody.getNamespaceURI(),_operation.getName());
          }
          for (Iterator<String> j = parts.iterator(); j.hasNext(); ) {
            String partName = j.next();
            Part part = wsdlMessage.getPart(partName);
            if (part.getElementName() != null) {
              throw new BasicProfileBindingViolation("R2203", "binding-operation", _bindingOperation.getName(),
                      __msgs.msgBP_R2203(partName));
            }
            partMap.put(partName, soapBody);
          }
        } else /* doc-literal */ {
          if (parts.size() > 1) {
            throw new BasicProfileBindingViolation("R2201", "binding-operation", _bindingOperation.getName(),
                    __msgs.msgBP_R2201());
          }
          else if (parts.size() == 1) {
            String partName = parts.iterator().next();
            Part part = wsdlMessage.getPart(partName);
            if (part.getTypeName() != null) {
              throw new BasicProfileBindingViolation("R2204", "binding-operation", _bindingOperation.getName(),
                      __msgs.msgBP_R2204(partName));
            }
            partMap.put(partName, soapBody);
            // this may be qname of output message, don't overwrite qname
            if(_qname == null) {
            	_qname = part.getElementName();
            }
          }
        }

      } else if (obj instanceof SOAPHeader) {
        SOAPHeader wsdlSoapHeaderExt = (SOAPHeader) obj;
        // NOTE: We ignore headers that do not map to the abstract message!
        if (!wsdlSoapHeaderExt.getMessage().equals(wsdlMessage.getQName()))
          continue;
        String partName = wsdlSoapHeaderExt.getPart();

        if (!wsdlSoapHeaderExt.getUse().equals("literal"))
          throw new BasicProfileBindingViolation("R2706",
                  "binding-operation", _bindingOperation.getName(),
                  __msgs.msgBP_R2706(partName));

        Part part = wsdlMessage.getPart(partName);
        if (part == null)
          throw new SoapBindingException("No part for <soap:header>: " + wsdlSoapHeaderExt,
                  "binding-operation", _bindingOperation.getName(),
                  __msgs.msgPartNotKnown(partName));

        boolean isElement = part.getElementName() != null;
        if (!isElement)
          throw new BasicProfileBindingViolation("R2205",
                  "binding-operation", _operation.getName(),
                  __msgs.msgBP_R2205(partName));

        if (wsdlSoapHeaderExt.getNamespaceURI() != null)
          throw _rpcStyle
                  ? new BasicProfileBindingViolation("R2726",
                          "binding-operation", _operation.getName(),
                          __msgs.msgBP_R2726(partName))
                  : new BasicProfileBindingViolation("R2716",
                          "binding-operation", _operation.getName(),
                          __msgs.msgBP_R2716(partName));
        partMap.put(partName, wsdlSoapHeaderExt);
      }
    }

  }

  public Message getRequestMessage() {
    return _operation.getInput() == null ? null : _operation.getInput().getMessage();
  }

  public Message getResponseMessage() {
    return _operation.getOutput() == null ? null : _operation.getOutput().getMessage();
  }

  public Operation getOperation() {
    return _operation;
  }

  public BindingOperation getBindingOperation() {
    return _bindingOperation;
  }

  public QName getQName() {
    return _qname;
  }

  public String getSOAPAction() {
    return _soapOperation.getSoapActionURI();
  }

  public boolean isOneWay() {
    return _operation.getOutput() == null;
  }

  public SOAPReader getSoapRequestReader() {
    return _requestReader;
  }

  public SOAPReader getSoapResponseReader() {
    return _responseReader;
  }

  public SOAPWriter getSoapRequestWriter() {
    return _requestWriter;
  }

  public SOAPWriter getSoapResponseWriter() {
    return _responseWriter;
  }

  public SOAPWriter getSoapResponseWriter(String faultName) {
    return _faultWriters.get(faultName);
  }

  public SOAPReader getSoapResponseReader(String faultName) {
    return _faultReaders.get(faultName);
  }

}
