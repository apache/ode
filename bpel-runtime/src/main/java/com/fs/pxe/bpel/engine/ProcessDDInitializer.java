package com.fs.pxe.bpel.engine;

import com.fs.utils.msg.MessageBundle;
import com.fs.utils.Namespaces;
import com.fs.utils.DOMUtils;
import com.fs.pxe.bpel.o.OProcess;
import com.fs.pxe.bpel.o.OPartnerLink;
import com.fs.pxe.bpel.dao.ProcessDAO;
import com.fs.pxe.bpel.dao.PartnerLinkDAO;
import com.fs.pxe.bpel.iapi.BpelEngineException;
import com.fs.pxe.bpel.iapi.EndpointReference;
import com.fs.pxe.bpel.iapi.EndpointReferenceContext;
import com.fs.pxe.bom.wsdl.Definition4BPEL;
import com.fs.pxe.bpel.dd2.TDeployment;
import com.fs.pxe.bpel.dd2.TInvoke;
import com.fs.pxe.bpel.dd2.TProvide;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Document;

import javax.xml.namespace.QName;
import javax.wsdl.Service;
import javax.wsdl.Port;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.UnknownExtensibilityElement;
import java.util.HashMap;

/**
 * Extracts and treats process deployment descriptors. The descriptor interacts
 * with the process deployment either by modifying the {@link com.fs.pxe.bpel.dao.ProcessDAO}.
 */
class ProcessDDInitializer {

  private static final Messages __msgs = MessageBundle
      .getMessages(Messages.class);

  private static final Log __log = LogFactory.getLog(ProcessDDInitializer.class);

  private OProcess _oprocess;
  private TDeployment.Process _dd;
  private Definition4BPEL[] _defs;
  private EndpointReferenceContext _eprContext;

  public ProcessDDInitializer(OProcess oprocess, Definition4BPEL[] defs,
                              TDeployment.Process dd, EndpointReferenceContext eprContext) {
    _oprocess = oprocess;
    _dd = dd;
    _defs = defs;
    _eprContext = eprContext;
  }

  public void update(ProcessDAO processDAO) {
    handleEndpoints(processDAO);
    handleProperties(processDAO);
  }

  private void handleEndpoints(ProcessDAO processDAO) {
    // For the moment the dao has both my role and partner role which isn't so nice.
    // Need to have a map to use the same object for provide and invoke.
    HashMap<String,PartnerLinkDAO> tmpPLinkMap = new HashMap<String, PartnerLinkDAO>();
    if (_dd.getProvideList().size() > 0) {
      for (TProvide provide : _dd.getProvideList()) {
        OPartnerLink pLink = _oprocess.getPartnerLink(provide.getPartnerLink());
        if (pLink == null) {
          String msg = ProcessDDInitializer.__msgs.msgDDPartnerLinkNotFound(provide.getPartnerLink());
          ProcessDDInitializer.__log.error(msg);
          throw new BpelEngineException(msg);
        }
        if (!pLink.hasMyRole()) {
          String msg = ProcessDDInitializer.__msgs.msgDDMyRoleNotFound(provide.getPartnerLink());
          ProcessDDInitializer.__log.error(msg);
          throw new BpelEngineException(msg);
        }
        PartnerLinkDAO eprdao = processDAO.addDeployedPartnerLink(pLink.getId(),
            pLink.name, pLink.myRoleName, pLink.partnerRoleName);
        tmpPLinkMap.put(pLink.name, eprdao);
        eprdao.setMyRoleServiceName(provide.getService().getName());
        Element epr = getService(provide.getService().getName(), provide.getService().getPort());
        if (epr == null) {
          String msg = ProcessDDInitializer.__msgs.msgUndefinedServicePort(
                  provide.getService().getName(), provide.getService().getPort());
          ProcessDDInitializer.__log.error(msg);
          throw new BpelEngineException(msg);
        }
        // Making sure we're dealing only with WSA endpoints internally.
        // These are much easier to deal with.
        EndpointReference endpointRef = _eprContext.convertEndpoint(Namespaces.WS_ADDRESSING_ENDPOINT,
                createServiceRef(epr));
        eprdao.setMyEPR(endpointRef.toXML().getDocumentElement());
      }
    }
    if (_dd.getInvokeList().size() > 0) {
      for (TInvoke invoke : _dd.getInvokeList()) {
        OPartnerLink pLink = _oprocess.getPartnerLink(invoke.getPartnerLink());
        if (pLink == null) {
          String msg = ProcessDDInitializer.__msgs.msgDDPartnerLinkNotFound(invoke.getPartnerLink());
          ProcessDDInitializer.__log.error(msg);
          throw new BpelEngineException(msg);
        }
        if (!pLink.hasPartnerRole()) {
          String msg = ProcessDDInitializer.__msgs.msgDDPartnerRoleNotFound(invoke.getPartnerLink());
          ProcessDDInitializer.__log.error(msg);
          throw new BpelEngineException(msg);
        }
        // TODO Handle non initialize partner roles that just provide a binding
        if (!pLink.initializePartnerRole && _oprocess.version.equals(Namespaces.WS_BPEL_20_NS)) {
          String msg = ProcessDDInitializer.__msgs.msgDDNoInitiliazePartnerRole(invoke.getPartnerLink());
          ProcessDDInitializer.__log.error(msg);
          throw new BpelEngineException(msg);
        }
        PartnerLinkDAO eprdao;
        if (tmpPLinkMap.get(pLink.name) != null) eprdao = tmpPLinkMap.get(pLink.name);
        else eprdao = processDAO.addDeployedPartnerLink(pLink.getId(),
                pLink.name, pLink.myRoleName, pLink.partnerRoleName);
        Element epr = getService(invoke.getService().getName(), invoke.getService().getPort());
        if (epr == null) {
          String msg = ProcessDDInitializer.__msgs.msgUndefinedServicePort(
                  invoke.getService().getName(), invoke.getService().getPort());
          ProcessDDInitializer.__log.error(msg);
          throw new BpelEngineException(msg);
        }
        // Making sure we're dealing only with WSA endpoints internally.
        // These are much easier to deal with.
        EndpointReference endpointRef = _eprContext.convertEndpoint(Namespaces.WS_ADDRESSING_ENDPOINT,
                createServiceRef(epr));
        eprdao.setPartnerEPR(endpointRef.toXML().getDocumentElement());
      }
    }
  }

  private void handleProperties(ProcessDAO processDAO) {
    if (_dd.getPropertyList().size() > 0) {
      for (TDeployment.Process.Property property : _dd.getPropertyList()) {
        String textContent = DOMUtils.getTextContent(property.getDomNode());
        if (textContent != null) {
          processDAO.setProperty(property.getName(), property.getNamespace(),
              textContent);
        } else {
          Element elmtContent = DOMUtils.getElementContent(property
              .getDomNode());
          processDAO.setProperty(property.getName(), property.getNamespace(),
              elmtContent);
        }
      }
    }
  }

  private Element getService(QName name, String portName) {
    for (Definition4BPEL definition4BPEL : _defs) {
      Service serviceDef = definition4BPEL.getService(name);
      if (serviceDef != null) {
        Port portDef = serviceDef.getPort(portName);
        if (portDef != null) {
          Document doc = DOMUtils.newDocument();
          Element service = doc.createElementNS(Namespaces.WSDL_11, "service");
          service.setAttribute("name", serviceDef.getQName().getLocalPart());
          service.setAttribute("targetNamespace", serviceDef.getQName().getNamespaceURI());
          Element port = doc.createElementNS(Namespaces.WSDL_11, "port");
          service.appendChild(port);
          port.setAttribute("name", portDef.getName());
          port.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:bindns",
                  portDef.getBinding().getQName().getNamespaceURI());
          port.setAttribute("bindns:binding", portDef.getName());
          for (Object extElmt : portDef.getExtensibilityElements()) {
            if (extElmt instanceof SOAPAddress) {
              Element soapAddr = doc.createElementNS(Namespaces.SOAP_NS, "address");
              port.appendChild(soapAddr);
              soapAddr.setAttribute("location", ((SOAPAddress)extElmt).getLocationURI());
            } else {
              port.appendChild(doc.importNode(((UnknownExtensibilityElement)extElmt).getElement(), true));
            }
          }
          return service;
        }
      }
    }
    return null;
  }

  public boolean exists() {
    return _dd != null;
  }

  public void init(ProcessDAO newDao) {
    newDao.setRetired(_dd.getRetired());
    newDao.setActive(_dd.getActive());
    for (String correlator : _oprocess.getCorrelators()) {
      newDao.addCorrelator(correlator);
    }
  }

  /**
   * Create-and-copy a service-ref element.
   * @param elmt
   * @return wrapped element
   */
  private Element createServiceRef(Element elmt) {
    Document doc = DOMUtils.newDocument();
    QName elQName = new QName(elmt.getNamespaceURI(),elmt.getLocalName());
    // If we get a service-ref, just copy it, otherwise make a service-ref wrapper
    if (!EndpointReference.SERVICE_REF_QNAME.equals(elQName)) {
      Element serviceref = doc.createElementNS(EndpointReference.SERVICE_REF_QNAME.getNamespaceURI(),
          EndpointReference.SERVICE_REF_QNAME.getLocalPart());
      serviceref.appendChild(doc.importNode(elmt,true));
      doc.appendChild(serviceref);
    } else {
      doc.appendChild(doc.importNode(elmt, true));
    }

    return doc.getDocumentElement();
  }

}
