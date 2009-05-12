package org.apache.ode.bpel.rapi;

/**
 * Compiled representation of a web resource.
 */
public interface ResourceModel {

    String getName();

    Object getSubpath();

    ResourceModel getReference();

    String getMethod();

    boolean isInstantiateResource();
}
