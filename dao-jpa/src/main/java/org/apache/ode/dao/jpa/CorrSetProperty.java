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
package org.apache.ode.dao.jpa;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * @author Matthieu Riou <mriou at apache dot org>
 */
@Entity
@Table(name="ODE_CORSET_PROP")
@NamedQueries({
    @NamedQuery(name=CorrSetProperty.DELETE_CORSET_PROPERTIES_BY_PROPERTY_IDS, query="delete from CorrSetProperty as p where p.corrSetId in(:corrSetIds)")
})
public class CorrSetProperty {
    public final static String DELETE_CORSET_PROPERTIES_BY_PROPERTY_IDS = "DELETE_CORSET_PROPERTIES_BY_PROPERTY_IDS";

    @Id @Column(name="ID")
    @GeneratedValue(strategy=GenerationType.AUTO)
    @SuppressWarnings("unused")
    private Long _id;
    @Basic @Column(name="PROP_KEY")
    private String propertyKey;
    @Basic @Column(name="PROP_VALUE")
    private String propertyValue;

    @SuppressWarnings("unused")
    @Basic @Column(name="CORRSET_ID", insertable=false, updatable=false, nullable=true)
    private Long corrSetId;

    @ManyToOne(fetch=FetchType.LAZY,cascade={CascadeType.PERSIST}) @Column(name="CORRSET_ID")
    private CorrelationSetDAOImpl _corrSet;

    public CorrSetProperty() {
    }
    public CorrSetProperty(String propertyKey, String propertyValue) {
        this.propertyKey = propertyKey;
        this.propertyValue = propertyValue;
    }

    public String getPropertyKey() {
        return propertyKey;
    }

    public void setPropertyKey(String propertyKey) {
        this.propertyKey = propertyKey;
    }

    public String getPropertyValue() {
        return propertyValue;
    }

    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
    }

    public CorrelationSetDAOImpl getCorrSet() {
        return _corrSet;
    }

    public void setCorrSet(CorrelationSetDAOImpl corrSet) {
        _corrSet = corrSet;
    }
}
