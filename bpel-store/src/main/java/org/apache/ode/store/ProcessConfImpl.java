package org.apache.ode.store;

import org.apache.ode.bpel.iapi.ProcessConf;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import java.io.File;
import java.util.Date;
import java.util.Map;

/**
 * @author mriou <mriou at apache dot org>
 */
public class ProcessConfImpl implements ProcessConf {

    private Date deployDate;
    private String deployer;
    private File[] files;
    private QName processId;
    private String packageName;
    private Map<QName,Node> props;
    private boolean active;
    private boolean inMemory;

    public Date getDeployDate() {
        return deployDate;
    }

    public String getDeployer() {
        return deployer;
    }

    public File[] getFiles() {
        return files;
    }

    public QName getProcessId() {
        return processId;
    }

    public String getProcessPackage() {
        return packageName;
    }

    public Map<QName, Node> getProperties() {
        return props;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setDeployDate(Date deployDate) {
        this.deployDate = deployDate;
    }

    public void setDeployer(String deployer) {
        this.deployer = deployer;
    }

    public void setFiles(File[] files) {
        this.files = files;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setProcessId(QName processId) {
        this.processId = processId;
    }

    public void setProps(Map<QName, Node> props) {
        this.props = props;
    }

    public boolean isInMemory() {
        return inMemory;
    }

    public void setInMemory(boolean inMemory) {
        this.inMemory = inMemory;
    }
}
