package org.apache.ode.store;

import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.Date;

/**
 * DAO interface for a "deployment unit", a collection of processes deployed as a single
 * unit.
 * 
 * @author mszefler
 *
 */
public interface DeploymentUnitDAO {

    /**
     * Get the name of the deployment unit.
     * @return du name
     */
    String getName();
    
    /**
     * Get the deployment unit directory path. 
     * @return deployment unit directory path
     */
    String getDeploymentUnitDir();
    
    
    void setDeploymentUnitDir(String dir);
    
    /**
     * Get the collection of processes that are deployed as part of this deployment unit.
     * @return
     */
    Collection<? extends ProcessConfDAO> getProcesses();

    /**
     * Get the date/time the DU was deployed.
     * @return
     */
    Date getDeployDate();

    /**
     * Get the userid of the user doing the deploying.
     * @return
     */
    String getDeployer();

    
    /**
     * Delete this deployment unit (deletes all the children).
     */
    void delete();

    ProcessConfDAO createProcess(QName pid, QName type, long version);

    ProcessConfDAO getProcess(QName pid);
    
}
