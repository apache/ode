package org.apache.ode.store;

import java.util.Collection;

import javax.xml.namespace.QName;

import org.apache.ode.bpel.iapi.ProcessState;

/**
 * DAO interface for a process configuration. 
 * @author mriou <mriou at apache dot org>
 */
public interface ProcessConfDAO {
    
    QName getPID();

    QName getType();

    int getVersion();

    DeploymentUnitDAO getDeploymentUnit();
    
    ProcessState getState();

    void setState(ProcessState state);

    void setProperty(QName name, String content);

    String getProperty(QName name);
    
    Collection<QName> getPropertyNames();
    
    void delete();
}
