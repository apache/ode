package org.apache.ode.bpel.dao;

import java.util.Set;

import org.apache.ode.bpel.common.InstanceFilter;
import org.apache.ode.bpel.iapi.ProcessConf.CLEANUP_CATEGORY;

/**
 * An implementation of this interface provides a way to delete runtime process instances
 * through the InstanceFilter.
 * 
 * @author sean
 *
 */
public interface FilteredInstanceDeletable {
    /**
     * Deletes instance filter by the given instance filter and clean up categories.
     * 
     * @param filter instance filter
     * @param categories clean up categories
     * @return returns the number of instances that are deleted
     */
    int deleteInstances(InstanceFilter filter, Set<CLEANUP_CATEGORY> categories);
}
