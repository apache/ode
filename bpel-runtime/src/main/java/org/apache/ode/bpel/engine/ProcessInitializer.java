package org.apache.ode.bpel.engine;

import javax.xml.namespace.QName;

import org.apache.ode.bpel.dao.PartnerLinkDAO;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.dd.TDeploymentDescriptor;
import org.apache.ode.bpel.dd.TRoles;
import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.bpel.iapi.EndpointReference;
import org.apache.ode.bpel.o.OPartnerLink;
import org.apache.ode.bpel.o.OProcess;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.Namespaces;
import org.apache.ode.utils.msg.MessageBundle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Extracts and treats process deployment descriptors. The descriptor interacts
 * with the process deployment either by modifying the {@link ProcessDAO}.
 */
class ProcessInitializer {

  private static final Messages __msgs = MessageBundle
      .getMessages(Messages.class);

  private static final Log __log = LogFactory.getLog(ProcessInitializer.class);

  private OProcess _oprocess;

  private TDeploymentDescriptor _dd;

  public ProcessInitializer(OProcess oprocess, TDeploymentDescriptor dd) {
    _oprocess = oprocess;
    _dd = dd;
  }

  public void update(ProcessDAO processDAO) {
    handleEndpoints(processDAO);
    handleProperties(processDAO);
  }

  private void handleEndpoints(ProcessDAO processDAO) {
    
    if (_dd.getEndpointRefs() != null) {
      for (int m = 0; m < _dd.getEndpointRefs().getEndpointRefList().size(); m++) {
        TDeploymentDescriptor.EndpointRefs.EndpointRef endpointRef = _dd
            .getEndpointRefs().getEndpointRefList().get(m);
        OPartnerLink pLink = _oprocess.getPartnerLink(endpointRef
            .getPartnerLinkName());
        if (pLink == null) {
          String msg = __msgs.msgDDPartnerLinkNotFound(endpointRef
              .getPartnerLinkName());
          __log.error(msg);
          throw new BpelEngineException(msg);
        }
        
        PartnerLinkDAO eprdao = processDAO.addDeployedPartnerLink(pLink.getId(), 
            pLink.name,
            pLink.myRoleName,
            pLink.partnerRoleName);
        
        if (!endpointRef.isSetPartnerLinkRole()
            || endpointRef.getPartnerLinkRole().equals(TRoles.PARTNER_ROLE)) {
          if (!pLink.hasPartnerRole()) {
            String msg = __msgs.msgDDPartnerRoleNotFound(endpointRef
                .getPartnerLinkName());
            __log.error(msg);
            throw new BpelEngineException(msg);
          }
          if (!pLink.initializePartnerRole
              && _oprocess.version.equals(Namespaces.WS_BPEL_20_NS)) {
            String msg = __msgs.msgDDNoInitiliazePartnerRole(endpointRef
                .getPartnerLinkName());
            __log.error(msg);
            throw new BpelEngineException(msg);
          }
          eprdao.setPartnerEPR(createServiceRef(DOMUtils.getElementContent(endpointRef.getDomNode())));
        } else {
          if (!pLink.hasMyRole()) {
            String msg = __msgs.msgDDMyRoleNotFound(endpointRef
                .getPartnerLinkName());
            __log.error(msg);
            throw new BpelEngineException(msg);
          }
          // If a service name is not specified, then we use the process name to
          // derive one.
          if (endpointRef.getServiceName() == null)
            eprdao.setMyRoleServiceName(new QName(processDAO.getProcessId().getNamespaceURI(),
                processDAO.getProcessId().getLocalPart() + "." + pLink.name));
          else
            eprdao.setMyRoleServiceName(endpointRef.getServiceName());
          eprdao.setMyEPR(createServiceRef(DOMUtils.getElementContent(endpointRef.getDomNode())));
        }
      }
    }
  }

  private void handleProperties(ProcessDAO processDAO) {
    if (_dd.getProcessProperties() != null) {
      for (int m = 0; m < _dd.getProcessProperties().getPropertyList().size(); m++) {
        TDeploymentDescriptor.ProcessProperties.Property property = _dd
            .getProcessProperties().getPropertyList().get(m);
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


  /**
   * Create-and-copy a service-ref element. 
   * @param elmt
   * @return
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
}
