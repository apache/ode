package org.apache.ode.bpel.engine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.iapi.Endpoint;
import org.apache.ode.bpel.iapi.PartnerRoleChannel;
import org.apache.ode.bpel.o.OPartnerLink;

/**
 * @author Matthieu Riou <mriou at apache dot org>
 */
class PartnerLinkPartnerRoleImpl extends PartnerLinkRoleImpl {
    static final Log __log = LogFactory.getLog(BpelProcess.class);

    Endpoint _initialPartner;

    public PartnerRoleChannel _channel;

    PartnerLinkPartnerRoleImpl(BpelProcess process, OPartnerLink plink, Endpoint initialPartner) {
        super(process, plink);
        _initialPartner = initialPartner;
    }

    public void processPartnerResponse(PartnerRoleMessageExchangeImpl messageExchange) {
        if (__log.isDebugEnabled()) {
            __log.debug("Processing partner's response for partnerLink: " + messageExchange);
        }

        BpelRuntimeContextImpl processInstance =
                _process.createRuntimeContext(messageExchange.getDAO().getInstance(), null, null);
        processInstance.invocationResponse(messageExchange);
        processInstance.execute();
    }

}
