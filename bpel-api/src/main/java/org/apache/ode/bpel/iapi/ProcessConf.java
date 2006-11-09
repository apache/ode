package org.apache.ode.bpel.iapi;

import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import java.io.File;
import java.util.Date;
import java.util.Map;

/**
 * @author mriou <mriou at apache dot org>
 */
public interface ProcessConf {

    QName getProcessId();

    Date getDeployDate();

    String getDeployer();

    boolean isActive();

    File[] getFiles();

    Map<QName, Node> getProperties();

    /**
     * Gets the name of the package into which the process is deployed.
     * @return package name
     */
    String getProcessPackage();

    /**
     * @return true if the process should be executed only in-memory without being persisted
     */
    boolean isInMemory();
}
