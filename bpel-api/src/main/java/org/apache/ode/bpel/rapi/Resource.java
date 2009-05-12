package org.apache.ode.bpel.rapi;

public interface Resource {

    String getName();

    ResourceModel getModel();

    Long getScopeInstanceId();
}
