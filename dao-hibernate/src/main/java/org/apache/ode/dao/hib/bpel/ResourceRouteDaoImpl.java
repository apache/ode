package org.apache.ode.dao.hib.bpel;

import org.apache.ode.dao.bpel.ResourceRouteDAO;
import org.apache.ode.dao.bpel.ProcessInstanceDAO;
import org.apache.ode.dao.hib.bpel.hobj.HResourceRoute;
import org.apache.ode.dao.hib.SessionManager;

public class ResourceRouteDaoImpl extends HibernateDao implements ResourceRouteDAO {

    HResourceRoute _self;

    public ResourceRouteDaoImpl(SessionManager sessionManager, HResourceRoute hrr) {
        super(sessionManager, hrr);
        _self = hrr;
    }

    public String getUrl() {
        return _self.getUrl();
    }

    public void setUrl(String url) {
        _self.setUrl(url);
    }

    public String getMethod() {
        return _self.getMethod();
    }

    public void setMethod(String method) {
        _self.setMethod(method);
    }

    public String getPickResponseChannel() {
        return _self.getChannelId();
    }

    public void setPickResponseChannel(String channelId) {
        _self.setChannelId(channelId);
    }

    public int getSelectorIdx() {
        return _self.getIndex();
    }

    public void setSelectorIdx(int index) {
        _self.setIndex(index);
    }

    public ProcessInstanceDAO getInstance() {
        return new ProcessInstanceDaoImpl(_sm, _self.getInstance());
    }
}
