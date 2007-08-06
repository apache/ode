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

import org.apache.openjpa.persistence.OpenJPAPersistence;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

/**
 * @author Matthieu Riou <mriou at apache dot org>
 */
public class OpenJPADAO {

    protected BPELDAOConnectionImpl getConn() {
        return BPELDAOConnectionFactoryImpl._connections.get();
    }

    protected EntityManager getEM() {
        return OpenJPAPersistence.getEntityManager(this);
    }

    /**
     * javax.persistence.Query either let you query for a collection or a single
     * value throwing an exception if nothing is found. Just a convenient shortcut
     * for single results allowing null values
     * @param qry query to execute
     * @return whatever you assign it to
     */
    protected <T> T getSingleResult(Query qry) {
        List res = qry.getResultList();
        if (res.size() == 0) return null;
        return (T) res.get(0);

    }
}
