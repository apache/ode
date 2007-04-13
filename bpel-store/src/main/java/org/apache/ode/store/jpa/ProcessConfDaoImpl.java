/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.ode.store.jpa;

import org.apache.ode.bpel.iapi.ProcessState;
import org.apache.ode.store.ProcessConfDAO;
import org.apache.ode.utils.stl.CollectionsX;
import org.apache.ode.utils.stl.UnaryFunction;

import javax.persistence.*;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * @author Matthieu Riou <mriou at apache dot org>
 */
@Entity
@Table(name="STORE_PROCESS")
public class ProcessConfDaoImpl extends JpaObj implements ProcessConfDAO {

    @ManyToOne(targetEntity=DeploymentUnitDaoImpl.class,fetch=FetchType.EAGER,cascade={CascadeType.ALL})
    @Column(name="DU")
    private DeploymentUnitDaoImpl _du;

    @OneToMany(fetch=FetchType.EAGER,cascade={CascadeType.ALL})
    @MapKey(name="_key")
    @JoinTable(name="STORE_PROC_TO_PROP")
    private HashMap<String,ProcessConfPropertyDaoImpl> _properties = new HashMap<String,ProcessConfPropertyDaoImpl>();

    @Id @Column(name="PID")
    private String _processId;

    @Basic @Column(name="TYPE")
    private String _type;

    @Basic @Column(name="VERSION")
    private long _version;

    @Basic @Column(name="STATE")
    private String _state;

    public DeploymentUnitDaoImpl getDeploymentUnit() {
        return _du;
    }

    public void setDeploymentUnit(DeploymentUnitDaoImpl du) {
        _du = du;
    }

    /**
     *
     * @hibernate.id generator-class="assigned"
     * @hibernate.column
     *  name="PID"
     *  not-null="true"
     */
    public String getPID_() {
        return _processId;
    }

    public void setPID_(String processId) {
        _processId = processId;
    }


    /**
     * The type of the process (BPEL process definition name).
     * @hibernate.property
     *     column="TYPE"
     */
    public String getType_() {
        return _type;
    }

    public void setType_(String type) {
        _type = type;
    }


    /**
     * The process version.
     * @hibernate.property
     *    column="version"
     */
    public long getVersion() {
        return _version;
    }

    public void setVersion(long version) {
        _version = version;
    }

    public String getState_() {
        return _state;
    }

    public void setState_(String state) {
        _state = state;
    }

    public QName getPID() {
        return QName.valueOf(getPID_());
    }

    public void setPID(QName pid) {
        setPID_(pid.toString());
    }

    public void setState(ProcessState state) {
        setState_(state.toString());
    }

    public void setProperty(QName name, String content) {
        ProcessConfPropertyDaoImpl prop = new ProcessConfPropertyDaoImpl();
        prop.setKey(name.toString());
        prop.setValue(content);
        getEM().persist(prop);
        _properties.put(name.toString(),prop);
        getEM().persist(this);
    }

    public void delete() {
        super.delete();
    }

    public QName getType() {
        return QName.valueOf(getType_());
    }

    public void setType(QName type) {
        setType_(type.toString());
    }

    public ProcessState getState() {
        return ProcessState.valueOf(getState_());
    }

    public String getProperty(QName name) {
        return _properties.get(name.toString()).getValue();
    }

    public Collection<QName> getPropertyNames() {
        return CollectionsX.transform(new ArrayList<QName>(), _properties.keySet(),new UnaryFunction<String,QName>() {
            public QName apply(String x) {
                return QName.valueOf(x);
            }

        });
    }

}
