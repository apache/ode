package org.apache.ode.bpel.rapi;

import java.util.Collection;
import java.util.List;

import javax.xml.namespace.QName;

public interface ProcessModel {

    PartnerLinkModel getPartnerLink(String name);
    
    PartnerLinkModel getPartnerLink(int partnerLinkModelId);

    Collection<? extends PartnerLinkModel> getAllPartnerLinks();

    String getGuid();

    Collection<? extends ResourceModel> getProvidedResources();

    QName getQName();

    List<String> getCorrelators();

    ActivityModel getChild(final int id);

    int getModelVersion();

    ConstantsModel getConstantsModel();
    
    ScopeModel getProcessScope();

    byte[] getGlobalState();
}
