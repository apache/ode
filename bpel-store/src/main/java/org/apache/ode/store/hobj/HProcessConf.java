package org.apache.ode.store.hobj;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * @author mriou <mriou at apache dot org>
 * @hibernate.class table="BPEL_PROCESS_CONF"
 */
public class HProcessConf {

    private Long _id;

    /** {@link HProcessProperty}s for this process. */
    private Set<HProcessProperty> _properties = new HashSet<HProcessProperty>();

    /** Simple name of the process. */
    private String _processId;

    /** User that deployed the process. */
    private String _deployer;

    /** Date of last deployment. */
    private Date _deployDate;

    /** Process name. */
    private String _typeName;

    /** Process namespace. */
    private String _typeNamespace;

    /** Process version. */
    private int _version;

    /** Whether process is retired */
    private boolean _active;

    /**
     * @hibernate.id generator-class="native" column="ID"
     */
    public Long getId() {
        return _id;
    }

    public void setId(Long id) {
        _id = id;
    }

    /**
     * @hibernate.set
     *  lazy="true"
     *  inverse="true"
     *  cascade="delete"
     * @hibernate.collection-key
     *  column="PROCESS_ID"
     * @hibernate.collection-one-to-many
     *   class="org.apache.ode.store.hobj.HProcessProperty"
     */
    public Set<HProcessProperty> getProperties() {
        return _properties;
    }

    public void setProperties(Set<HProcessProperty> properties) {
        _properties = properties;
    }

    /**
     *
     * @hibernate.property
     * @hibernate.column
     *  name="PROCID"
     *  not-null="true"
     *  unique="true"
     */
    public String getProcessId() {
        return _processId;
    }

    public void setProcessId(String processId) {
        _processId = processId;
    }

    /**
     * The user that deployed the process.
     * @hibernate.property
     *    column="deployer"
     */
    public String getDeployer() {
        return _deployer;
    }

    public void setDeployer(String deployer) {
        _deployer = deployer;
    }



    /**
     * The date the process was deployed.
     * @hibernate.property
     *    column="deploydate"
     */
    public Date getDeployDate() {
        return _deployDate;
    }

    public void setDeployDate(Date deployDate) {
        _deployDate = deployDate;
    }

    /**
     * The type of the process (BPEL process definition name).
     * @hibernate.property
     *     column="type_name"
     */
    public String getTypeName() {
        return _typeName;
    }

    public void setTypeName(String processName) {
        _typeName = processName;
    }

    /**
     * The type of the process (BPEL process definition name).
     * @hibernate.property
     *     column="type_ns"
     */
    public String getTypeNamespace() {
        return _typeNamespace;
    }

    public void setTypeNamespace(String processName) {
        _typeNamespace = processName;
    }

    /**
     * The process version.
     * @hibernate.property
     *    column="version"
     */
    public int getVersion() {
        return _version;
    }

    public void setVersion(int version) {
        _version = version;
    }

    /**
     * The process status.
     * @hibernate.property
     *    column="ACTIVE"
     */
    public boolean isActive() {
        return _active;
    }

    public void setActive(boolean active) {
        _active = active;
    }

}
