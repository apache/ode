package org.apache.ode.store.dao;

import org.apache.ode.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import java.util.Date;
import java.util.HashMap;

/**
 * @author mriou <mriou at apache dot org>
 */
public class ProcessConfDAOInMem implements ProcessConfDAO {

    private QName _processId;
    private String _deployer;
    private Date _deployDate;
    private HashMap<QName, Node> _properties = new HashMap<QName, Node>();
    private String typeName;
    private String typeNamespace;
    private int version;
    private boolean active;

    public Date getDeployDate() {
        return _deployDate;
    }

    public void setDeployDate(Date deployDate) {
        _deployDate = deployDate;
    }

    public String getDeployer() {
        return _deployer;
    }

    public void setDeployer(String deployer) {
        _deployer = deployer;
    }

    public QName getProcessId() {
        return _processId;
    }

    public void setProcessId(QName processId) {
        _processId = processId;
    }

    public HashMap<QName, Node> getProperties() {
        return _properties;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getTypeNamespace() {
        return typeNamespace;
    }

    public void setTypeNamespace(String typeNamespace) {
        this.typeNamespace = typeNamespace;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public void setProperty(String name, String ns, Node content) {
        _properties.put(new QName(ns, name), content);
    }

    public void setProperty(String name, String ns, String content) {
        Document doc = DOMUtils.newDocument();        
        _properties.put(new QName(ns, name), doc.createTextNode(content));
    }

    public void delete() {

    }
}
