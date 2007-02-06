package org.apache.ode.bpel.engine;

/**
 * @author Matthieu Riou <mriou at apache dot org>
 */
public interface ProcessLifecycleCallback {

    void hydrated(BpelProcess process);
}
