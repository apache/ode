package org.apache.ode.bpel.evt;

/**
 * Used in Event Listeners to get Activity Variables and its data.
 * @author Raja Balasubramanian
 */
public interface EventContext {
    /**
     * Get ScopeInstanceId
     * @return scopeInstanceId
     */
    Long getScopeInstanceId();

    /**
     * Get All variable names used in this scope Instance
     * @return Array of Variable Names. If no variable(s) exists, null will be returned.
     */
    String[] getVariableNames();

    /**
     * Get Variable data for the given variable name, for this scope instance
     * @param varName Variable Name
     * @return DOM Node as XML String. If no value exists or variable not initialized, NULL will be returnrd.
     */
    String getVariableData(String varName);
}
