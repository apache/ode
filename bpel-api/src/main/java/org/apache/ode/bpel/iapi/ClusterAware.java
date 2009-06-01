package org.apache.ode.bpel.iapi;

/**
 * The interface to implement for a custom Scheduler implementation to support
 * Clustering.
 * 
 * @author sean
 *
 */
public interface ClusterAware {
    /**
     * A custom implementation should return true if the node that this method is called
     * is the coordinator of the cluster.
     * 
     * @return true when the node is coordinator
     */
    boolean amICoordinator();
}
