package org.apache.ode.bpel.rtrep.v2;

import javax.wsdl.Operation;

public interface OComm {

    OPartnerLink getPartnerLink();
    Operation getOperation();
    OResource getResource();

    boolean isRestful();
}
