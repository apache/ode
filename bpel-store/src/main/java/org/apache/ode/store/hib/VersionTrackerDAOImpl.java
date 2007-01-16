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

/**
 * @author Matthieu Riou <mriou at apache dot org>
 * @hibernate.class table="STORE_VERSIONS"
 */
public class VersionTrackerDAOImpl extends HibObj {

    private String _namespace;
    private int _version;

    /**
     * @hibernate.id generator-class="assigned"
     * @hibernate.property column="NS"
     */
    public String getNamespace() {
        return _namespace;
    }

    public void setNamespace(String namespace) {
        _namespace = namespace;
    }

    /**
     * @hibernate.property column="VERSION"
     */
    public int getVersion() {
        return _version;
    }

    public void setVersion(int version) {
        _version = version;
    }
}
