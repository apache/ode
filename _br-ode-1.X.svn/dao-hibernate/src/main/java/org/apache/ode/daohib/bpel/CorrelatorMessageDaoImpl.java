package org.apache.ode.daohib.bpel;

import org.apache.ode.bpel.dao.CorrelatorMessageDAO;
import org.apache.ode.bpel.common.CorrelationKey;
import org.apache.ode.daohib.bpel.hobj.HCorrelatorMessage;
import org.apache.ode.daohib.SessionManager;

public class CorrelatorMessageDaoImpl extends HibernateDao implements CorrelatorMessageDAO {

    private HCorrelatorMessage _hobj;

    public CorrelatorMessageDaoImpl(SessionManager sm, HCorrelatorMessage hobj) {
        super(sm, hobj);
        entering("CorrelatorDaoImpl.CorrelatorDaoImpl");
        _hobj = hobj;
    }

    public CorrelationKey getCorrelationKey() {
        return new CorrelationKey(_hobj.getCorrelationKey());
    }

    public void setCorrelationKey(CorrelationKey ckey) {
        _hobj.setCorrelationKey(ckey.toCanonicalString());
    }
}
