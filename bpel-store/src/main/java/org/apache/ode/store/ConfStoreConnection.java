package org.apache.ode.store;


import java.util.Collection;

/**
 */
public interface ConfStoreConnection {
    void begin();
    void commit();
    void rollback();
    
    DeploymentUnitDAO createDeploymentUnit(String name);

    DeploymentUnitDAO getDeploymentUnit(String name);

    Collection<DeploymentUnitDAO> getDeploymentUnits();

    void close();

    

    
}
