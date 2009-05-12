package org.apache.ode.bpel.memdao;

import org.apache.ode.bpel.dao.ResourceRouteDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;

public class ResourceRouteDAOImpl extends DaoBaseImpl implements ResourceRouteDAO {
    private Long _id;
    private String url;
    private String method;
    private String pickResponseChannel;
    private int selectorIdx;

    private ProcessInstanceDaoImpl instance;

    public ResourceRouteDAOImpl(String url, String method, String pickResponseChannel,
                                int selectorIdx, ProcessInstanceDaoImpl instance) {
        _id = IdGen.newProcessId();
        this.url = url;
        this.method = method;
        this.pickResponseChannel = pickResponseChannel;
        this.selectorIdx = selectorIdx;
        this.instance = instance;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getPickResponseChannel() {
        return pickResponseChannel;
    }

    public void setPickResponseChannel(String pickResponseChannel) {
        this.pickResponseChannel = pickResponseChannel;
    }

    public int getSelectorIdx() {
        return selectorIdx;
    }

    public void setSelectorIdx(int selectorIdx) {
        this.selectorIdx = selectorIdx;
    }

    public ProcessInstanceDAO getInstance() {
        return instance;
    }
}
