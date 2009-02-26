package org.apache.ode.bpel.dao;

import org.apache.ode.bpel.common.CorrelationKey;

public interface CorrelatorMessageDAO {

    CorrelationKey getCorrelationKey();

    void setCorrelationKey(CorrelationKey ckey);
}
