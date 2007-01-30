package org.apache.ode.store;

import org.apache.ode.bpel.iapi.ProcessState;

import javax.xml.namespace.QName;
import java.util.Collection;

/**
 * DAO interface for a process configuration. 
 * @author mriou <mriou at apache dot org>
 */
public interface ProcessConfDAO {
    
    QName getPID();

    QName getType();

    long getVersion();

    DeploymentUnitDAO getDeploymentUnit();
    
    ProcessState getState();

    void setState(ProcessState state);

    void setProperty(QName name, String content);

    String getProperty(QName name);
    
    Collection<QName> getPropertyNames();
    
    void delete();
}
