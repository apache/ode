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
package org.apache.ode.daohib.bpel.hobj;

/**
 * @hibernate.subclass table="BPEL_CORRELATOR_ENTRY" discriminator-value="S"
 * 
 */
public class HCorrelatorSelector extends HCorrelatorEntry {

    private HProcessInstance _instance;

    private String _groupId;

    private int _idx;

    /**
     * @hibernate.many-to-one column="PIID"
     */
    public HProcessInstance getInstance() {
        return _instance;
    }

    public void setInstance(HProcessInstance instance) {
        _instance = instance;
    }

    /**
     * @hibernate.property column="SELGRPID"
     * @hibernate.column name="SELGRPID" index="IDX_SELECTOR_SELGRPID"
     */
    public String getGroupId() {
        return _groupId;
    }

    public void setGroupId(String groupId) {
        _groupId = groupId;
    }

    /**
     * @hibernate.property column="IDX"
     */
    public int getIndex() {
        return _idx;
    }

    public void setIndex(int idx) {
        _idx = idx;
    }
}
