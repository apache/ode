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
package org.apache.ode.store.hib;

import org.apache.ode.bpel.iapi.ProcessState;
import org.apache.ode.store.DeploymentUnitDAO;
import org.apache.ode.store.ProcessConfDAO;
import org.apache.ode.utils.stl.CollectionsX;
import org.apache.ode.utils.stl.MemberOfFunction;

import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

/**
 * @author mriou <mriou at apache dot org>
 * @hibernate.class table="STORE_DU"
 */
public class DeploymentUnitDaoImpl extends HibObj implements DeploymentUnitDAO {

    private Collection<ProcessConfDaoImpl> _processes = new HashSet<ProcessConfDaoImpl>();

    /** User that deployed the process. */
    private String _deployer;

    /** Date of last deployment. */
    private Date _deployDate;

    private String _dir;

    private String _name;

    /**
     * @hibernate.bag
     *  lazy="false"
     *  inverse="true"
     *  cascade="all"
     *  role="store_processes"
     * @hibernate.collection-key
     *  column="DU" foreign-key="none"
     * @hibernate.collection-one-to-many
     *  class="org.apache.ode.store.hib.ProcessConfDaoImpl"
     */
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
        getSession().save(p);
        _processes.add(p);
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

