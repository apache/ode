package org.apache.ode.bpel.rapi;

import org.apache.ode.bpel.common.CorrelationKey;

public interface Selector {

    /**
     * @return
     */
    boolean isOneWay();

    /**
     * @return
     */
    PartnerLink getPartnerLink();

    /**
     * @return
     */
    String getOperation();

    /**
     * @return
     */
    String getMesageExchangeId();

    /**
     * @return
     */
    CorrelationKey getCorrelationKey();

}
