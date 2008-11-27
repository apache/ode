package org.apache.ode.bpel.dao;

public interface ResourceRouteDAO {
    public String getUrl();

    public void setUrl(String url);

    public String getMethod();

    public void setMethod(String method);

    public String getPickResponseChannel();

    public void setPickResponseChannel(String pickResponseChannel);

    public int getSelectorIdx();

    public void setSelectorIdx(int selectorIdx);

    public ProcessInstanceDAO getInstance();

}
