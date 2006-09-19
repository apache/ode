package org.apache.ode.test;

import org.apache.ode.bpel.iapi.EndpointReference;
import org.apache.ode.bpel.iapi.PartnerRoleChannel;
import org.apache.ode.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class PartnerRoleChannelImpl implements PartnerRoleChannel {
	
	public PartnerRoleChannelImpl() {

	}

	public void close() {

	}

	public EndpointReference getInitialEndpointReference() {
		final Document doc = DOMUtils.newDocument();
		Element serviceref = doc.createElementNS(EndpointReference.SERVICE_REF_QNAME.getNamespaceURI(),
                EndpointReference.SERVICE_REF_QNAME.getLocalPart());
        doc.appendChild(serviceref);
		return new EndpointReference() {
            public Document toXML() {
              return doc;
            }
        };
	}
}

