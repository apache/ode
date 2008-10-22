package org.apache.ode.bpel.rapi;

import java.util.Collection;
import java.util.List;

import javax.xml.namespace.QName;

public interface ProcessModel {

	PartnerLinkModel getPartnerLink(String name);
	
	PartnerLinkModel getPartnerLink(int partnerLinkModelId);

	Collection<? extends PartnerLinkModel> getAllPartnerLinks();

    Collection<? extends ResourceModel> getProvidedResources();

	String getGuid();

	QName getQName();

	List<String> getCorrelators();

    ActivityModel getChild(final int id);

    int getModelVersion();
}
