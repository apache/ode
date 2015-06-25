package org.apache.ode.bpel.clapi;

import java.util.Collection;

import javax.xml.namespace.QName;

import org.apache.ode.bpel.iapi.ProcessStore;

public interface ClusterProcessStore extends ProcessStore {
    public void deployProcesses(String duName);

    public Collection<QName> undeployProcesses(String duName);
}
