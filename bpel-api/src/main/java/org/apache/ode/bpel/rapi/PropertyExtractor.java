package org.apache.ode.bpel.rapi;

/**
 * Encapsulates the extraction of a property value for non alias based correlation.
 */
public interface PropertyExtractor {

    String getMessageVariableName();
}
