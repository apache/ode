package org.apache.ode.bpel.rapi;

import java.util.Collection;
import java.util.List;

import javax.xml.namespace.QName;

public interface ProcessModel {

//	OdeRTInstance newInstance();

	PartnerLinkModel getPartnerLink(String name);
	
	PartnerLinkModel getPartnerLink(int partnerLinkModelId);

	Collection<? extends PartnerLinkModel> getAllPartnerLinks();

	String getGuid();

	QName getQName();

	List<String> getCorrelators();

}
