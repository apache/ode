package org.apache.ode.bpel.memdao;

import org.apache.ode.bpel.dao.ResourceRouteDAO;

public class ResourceRouteDAOImpl extends DaoBaseImpl implements ResourceRouteDAO {
    private Long _id;
    private String url;
    private String method;
    private String pickResponseChannel;
    private int selectorIdx;

    public ResourceRouteDAOImpl(String url, String method, String pickResponseChannel, int selectorIdx) {
        _id = IdGen.newProcessId();
        this.url = url;
        this.method = method;
        this.pickResponseChannel = pickResponseChannel;
        this.selectorIdx = selectorIdx;
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
}
