/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.soap.mapping;

import com.fs.utils.msg.MessageBundle;
import com.fs.utils.stl.CollectionsX;

import java.util.*;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Port;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.xml.namespace.QName;

/**
 * BP-I compliant "wrapper" over a WSDL port. This class provides checking of BP-I violations, access
 * to operations, etc..
 */
public class SoapBindingModel extends AbstractSoapBinding {
  private static final Messages __msgs = MessageBundle.getMessages(Messages.class);

  private Port _wsdlPort;
  private SOAPBinding _soapBinding;
  private boolean _rpcStyle;

  /** Map operation-name -> {@link SoapOperationBindingModel} */
  private final Map<String, SoapOperationBindingModel> _operations =
    new HashMap<String, SoapOperationBindingModel>();

  /** Map SOAPAction -> {@link SoapOperationBindingModel} */
  private final Map<String, Set<SoapOperationBindingModel>> _soapActionMap =
    new HashMap<String, Set<SoapOperationBindingModel>>();

  /** Map SOAPBodyElement-QName -> {@link SoapOperationBindingModel} */
  private final Map<QName, Set<SoapOperationBindingModel>> _qnameMap =
    new HashMap<QName, Set<SoapOperationBindingModel>>();

  public SoapBindingModel(Port wsdlPort) throws SoapBindingException {
    if (wsdlPort == null)
      throw new NullPointerException("null wsdlPort");

    _wsdlPort = wsdlPort;
    Binding binding = _wsdlPort.getBinding();

    if (binding == null)
      throw new SoapBindingException(wsdlPort + " is missing <wsdl:binding>",
              "port", wsdlPort.getName(),
              __msgs.msgNoBindingForPort());

    Collection soapBindings = CollectionsX.filter(binding.getExtensibilityElements(), SOAPBinding.class);
    if (soapBindings.isEmpty()) {
      throw new SoapBindingException(wsdlPort + " is missing <soapbind:binding>",
              "port", wsdlPort.getName(),
              __msgs.msgNoSoapBindingForPort());
    }

    else if (soapBindings.size() > 1) {
      throw new SoapBindingException(wsdlPort + " has multiple <soapbind:binding> elements!",
              "port", wsdlPort.getName(),
              __msgs.msgMultipleSoapBindingsForPort());
    }

    _soapBinding = (SOAPBinding) soapBindings.iterator().next();
    _rpcStyle  = parseRpcStyle(_soapBinding.getStyle());

    List operations = binding.getBindingOperations();
    for (Iterator i = operations.iterator(); i.hasNext();) {
      BindingOperation bo = (BindingOperation) i.next();
      SoapOperationBindingModel opBinding = new SoapOperationBindingModel(this, bo);
      if (_operations.containsKey(bo.getName())) {
        throw new BasicProfileBindingViolation("R2304",
                "operation-binding",
                bo.getName(),
                __msgs.msgBP_R2304());
      }
      _operations.put(bo.getName(), opBinding);
      addToSoapActionSet(opBinding);
      addToQNameSet(opBinding);
    }
  }

  public boolean isRpcStyle() {
    return _rpcStyle;
  }

  /**
   * Get an operation model by name of operation.
   * @param opName operation name
   * @return the {@link SoapOperationBindingModel} for the operation name
   */
  public SoapOperationBindingModel getOperation(String opName) {
    return _operations.get(opName);
  }

  /**
   * Find an operation model based on the SOAPAction and the name of the first
   * soap:body child.
   * @param soapAction SOAPAction string
   * @param elementName qualified name of the first child element of the soap:body element
   * @return operation model for matching operation, <code>null</code> if no match
   * @throws SoapFormatException
   */
  public SoapOperationBindingModel findOperationBindingModel(String soapAction, QName elementName)
          throws SoapFormatException {
    if (soapAction == null) {
      soapAction = "";
    } else {
      soapAction = soapAction.trim();
    }

    if (soapAction.startsWith("\"") && soapAction.endsWith("\"")) {
      soapAction = soapAction.substring(1, soapAction.length() - 1);
    }

    Set<SoapOperationBindingModel> qnameSet = _qnameMap.get(elementName);
    Set<SoapOperationBindingModel> actionSet = _soapActionMap.get(soapAction);
    if (qnameSet == null) {
      return null;
    }

    Set<SoapOperationBindingModel> resultSet = new HashSet<SoapOperationBindingModel>(qnameSet);
    if(resultSet.size() > 1 && actionSet != null) {
    	resultSet.retainAll(actionSet);
    }

    if (resultSet.isEmpty()) {
      return null;
    }

    if (resultSet.size() == 1) {
      return resultSet.iterator().next();
    }

    throw new SoapFormatException("Multiple operations match SOAPAction/Operation Element signature!");
  }

  /**
   * Get the transport URI for this SOAP binding. Technically, according to BP-I
   * this MUST be the HTTP transport, but we do not enforce this here.
   * @return transport URI
   */
  public String getTransportURI() {
    return _soapBinding.getTransportURI();
  }

  private void addToQNameSet(SoapOperationBindingModel opBinding) {
    QName opQName = opBinding.getQName();
    Set<SoapOperationBindingModel> ops = _soapActionMap.get(opQName);
    if (ops == null) {
      _qnameMap.put(opQName, ops = new HashSet<SoapOperationBindingModel>());
    }

    ops.add(opBinding);
  }

  private void addToSoapActionSet(SoapOperationBindingModel opBinding) {
    String soapAction = opBinding.getSOAPAction();
    if (soapAction == null) {
      soapAction = "";
    }

    Set<SoapOperationBindingModel> ops = _soapActionMap.get(soapAction);
    if (ops == null) {
      _soapActionMap.put(soapAction, ops = new HashSet<SoapOperationBindingModel>());
    }

    ops.add(opBinding);
  }

}
