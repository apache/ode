package org.apache.ode.bpel.iapi;

import org.w3c.dom.Node;

import javax.wsdl.Definition;
import javax.xml.namespace.QName;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author mriou <mriou at apache dot org>
 */
public interface ProcessStore {

    /**
     * Gets the directory where all processes are deployed.
     * @return directory file
     */
    File getDeploymentDir();

    /**
     * Deploys a process from the filesystem.
     * @param deploymentUnitDirectory directory containing all deployment files
     * @return a collection of process ids (deployed processes)
     */
    Collection<QName> deploy(File deploymentUnitDirectory);

    /**
     * Undeploys a package.
     * @param file package
     * @return collection of successfully deployed process names
     */
    Collection<QName> undeploy(File file);

    /**
     * Lists the names of all the packages that have been deployed (corresponds
     * to a directory name on the file system).
     * @return an array of package names
     */
    String[] listDeployedPackages();

    /**
     * Lists all processe ids in a given package.
     * @return an array of process id QNames
     */
    QName[] listProcesses(String packageName);

    /**
     * Get the list of all active processes known to the store.
     * @return list of active processes qnames with their compiled definition
     */
    Map<QName, byte[]> getActiveProcesses();

    /**
     * Gets the list of endpoints a process should provide.
     * @param processId
     * @return map of partner link names and associated enpoints
     */
    Map<String, Endpoint> getProvideEndpoints(QName processId);

    /**
     * Gets the list of endpoints a process invokes.
     * @param processId
     * @return map of partner link names and associated enpoints
     */
    Map<String, Endpoint> getInvokeEndpoints(QName processId);

    /**
     * Marks a process as active or inactive
     * @param processId
     * @param status true for active, false for inactive
     */
    void markActive(QName processId, boolean status);

    /**
     * Gets the list of message interceptor class names configured on a process.
     * @param processId
     * @return list of interceptor class names
     */
    List<String> getMexInterceptors(QName processId);

    /**
     * Gets all the details of a process configuration (properties, deploy dates, ...)
     * @param processId
     * @return process configuration details
     */
    ProcessConf getProcessConfiguration(QName processId);

    /**
     * Gets the WSDL definition used in a process into which a service is defined.
     * @param processId
     * @param serviceName
     * @return definition
     */
    Definition getDefinitionForService(QName processId, QName serviceName);

    void setProperty(QName processId, String name, String namespace, String value);
    void setProperty(QName processId, String name, String namespace, Node value);

    /**
     * Gets the event setting for an activity that would be in a given process
     * and having the provided scope hierarchy.
     * @param processId
     * @param scopeNames names of parent scopes starting from the directly enclosing scope to the highest scope
     * @return enable event types
     */
    List<String> getEventsSettings(QName processId, List<String> scopeNames);
}
