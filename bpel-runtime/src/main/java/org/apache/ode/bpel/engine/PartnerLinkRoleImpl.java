package org.apache.ode.bpel.engine;

import org.apache.ode.bpel.iapi.EndpointReference;
import org.apache.ode.bpel.o.OPartnerLink;

/**
 * @author Matthieu Riou <mriou at apache dot org>
 */
abstract class PartnerLinkRoleImpl {
    protected OPartnerLink _plinkDef;
    protected EndpointReference _initialEPR;
    protected BpelProcess _process;

    PartnerLinkRoleImpl(BpelProcess process, OPartnerLink plink) {
        _plinkDef = plink;
        _process = process;
    }
    String getPartnerLinkName() {
        return _plinkDef.name;
    }
    /**
     * Get the initial value of this role's EPR. This value is obtained from
     * the integration layer when the process is enabled on the server.
     *
     * @return initial epr
     */
    EndpointReference getInitialEPR() {
        return _initialEPR;
    }

}
