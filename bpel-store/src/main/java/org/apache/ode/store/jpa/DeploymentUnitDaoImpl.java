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
import org.apache.ode.store.DeploymentUnitDAO;
import org.apache.ode.store.ProcessConfDAO;
import org.apache.ode.utils.stl.CollectionsX;
import org.apache.ode.utils.stl.MemberOfFunction;

import javax.persistence.*;
import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

/**
 * @author Matthieu Riou <mriou at apache dot org>
 */
@Entity
@Table(name="STORE_DU")
public class DeploymentUnitDaoImpl extends JpaObj implements DeploymentUnitDAO {

    @OneToMany(targetEntity=ProcessConfDaoImpl.class,mappedBy="_du",fetch=FetchType.EAGER,cascade={CascadeType.ALL})
    private Collection<ProcessConfDaoImpl> _processes = new HashSet<ProcessConfDaoImpl>();

    @Basic @Column(name="DEPLOYER")
    private String _deployer;

    @Basic @Column(name="DEPLOYDT")
    private Date _deployDate;

    @Basic @Column(name="DIR")
    private String _dir;

    @Id @Column(name="NAME")
    private String _name;

    public Collection<? extends ProcessConfDAO> getProcesses() {
        return _processes;
    }

    public void setProcesses(Collection<ProcessConfDaoImpl> processes) {
        _processes = processes;
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
     *    column="DEPLOYDT"
     */
    public Date getDeployDate() {
        return _deployDate;
    }

    public void setDeployDate(Date deployDate) {
        _deployDate = deployDate;
    }

    /**
     * @hibernate.id generator-class="assigned"
     * @hibernate.column name="NAME"
     */
    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    /**
     * @hibernate.property column="DIR"
     */
    public String getDeploymentUnitDir() {
        return _dir;
    }

    public void setDeploymentUnitDir(String dir) {
        _dir = dir;
    }

    public void delete() {
        super.delete();
    }

    public ProcessConfDAO createProcess(QName pid, QName type, long version) {
        ProcessConfDaoImpl p = new ProcessConfDaoImpl();
        p.setPID(pid);
        p.setType(type);
        p.setDeploymentUnit(this);
        p.setState(ProcessState.ACTIVE);
        p.setVersion(version);
        getEM().persist(p);
        _processes.add(p);
        getEM().persist(this);
        return p;
    }

    public ProcessConfDAO getProcess(final QName pid) {
        return CollectionsX.find_if(_processes,new MemberOfFunction<ProcessConfDAO>() {
            @Override
            public boolean isMember(ProcessConfDAO o) {
                return o.getPID().equals(pid);
            }

        });
    }

}
