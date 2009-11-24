package org.apache.ode.bpel.rapi;

import javax.wsdl.Operation;
import javax.wsdl.PortType;
import java.util.Set;

public interface PartnerLinkModel {
    
    int getId();
    
    String getName();
    
    ScopeModel getDeclaringScope();
    
    boolean hasMyRole();

    boolean hasPartnerRole();

    String getMyRoleName();

    Operation getMyRoleOperation(String operation);

    String getPartnerRoleName();
    
    Operation getPartnerRoleOperation(String operation);

    boolean isInitializePartnerRoleSet();

    PortType getMyRolePortType();

    PortType getPartnerRolePortType();

    boolean isCreateInstanceOperation(Operation operation);

    Set<CorrelationSetModel> getCorrelationSetsForOperation(Operation operation);

    Set<CorrelationSetModel> getUniqueCorrelationSetsForOperation(Operation operation);
}
