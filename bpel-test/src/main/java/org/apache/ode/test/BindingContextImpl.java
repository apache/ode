package org.apache.ode.test;

import javax.wsdl.PortType;
import javax.xml.namespace.QName;

import org.apache.ode.bpel.iapi.BindingContext;
import org.apache.ode.bpel.iapi.DeploymentUnit;
import org.apache.ode.bpel.iapi.Endpoint;
import org.apache.ode.bpel.iapi.EndpointReference;
import org.apache.ode.bpel.iapi.PartnerRoleChannel;
import org.apache.ode.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class BindingContextImpl implements BindingContext {

	public Element activateMyRoleEndpoint(QName processId,
			DeploymentUnit deploymentUnit, Endpoint myRoleEndpoint,
			PortType portType) {
		
		Document doc = DOMUtils.newDocument();
		Element serviceref = doc.createElementNS(EndpointReference.SERVICE_REF_QNAME.getNamespaceURI(),
                EndpointReference.SERVICE_REF_QNAME.getLocalPart());
        serviceref.setNodeValue(deploymentUnit.getDefinitionForNamespace(myRoleEndpoint.serviceName
                .getNamespaceURI()) +":" +
                myRoleEndpoint.serviceName +":" +
                myRoleEndpoint.portName);
        doc.appendChild(serviceref);
		return doc.getDocumentElement();
	}

	public void deactivateMyRoleEndpoint(Endpoint myRoleEndpoint) {

	}

	public PartnerRoleChannel createPartnerRoleChannel(QName processId,
			DeploymentUnit deploymentUnit, PortType portType,
			Endpoint initialPartnerEndpoint) {
		return null;
	}

}
