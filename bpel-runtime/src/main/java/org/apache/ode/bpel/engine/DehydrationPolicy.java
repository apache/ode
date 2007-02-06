package org.apache.ode.bpel.engine;

import java.util.List;

/**
 * Defines a policy to dehydrate running processes based on a limit in total
 * process count or processes that haven't been used for a while.
 * @author Matthieu Riou <mriou at apache dot org>
 */
public interface DehydrationPolicy {

    /**
     * Checks the currently running processes and marks some of them for
     * dehydration according to a specifically configured policy. The
     * returned processes will be dehydrated by the engine.
     * @param runningProcesses all running (currently hydrated) processes
     * @return processes elected for dehydration
     */
    List<BpelProcess> markForDehydration(List<BpelProcess> runningProcesses);
}
