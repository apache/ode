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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.apache.openjpa.persistence.OpenJPAQuery;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Matthieu Riou <mriou at apache dot org>
 */
public class OpenJPADAO {
	private static final Log __log = LogFactory.getLog(OpenJPADAO.class);

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
    @SuppressWarnings("unchecked")
    protected <T> T getSingleResult(Query qry) {
        List res = qry.getResultList();
        if (res.size() == 0) return null;
        return (T) res.get(0);
    }

    protected <T> void batchUpdateByIds(Iterator<T> ids, Query query, String parameterName) {
    	if( query instanceof OpenJPAQuery ) {
    		OpenJPAQuery openJpaQuery = (OpenJPAQuery)query;
    		int batchSize = openJpaQuery.getFetchPlan().getFetchBatchSize();
    		if( __log.isTraceEnabled() ) __log.trace("BATCH fetchBatchSize = " + batchSize);
    		List<T> batch = new ArrayList<T>();
    		while( ids.hasNext() ) {
	    		for( int i = 0; i < batchSize && ids.hasNext(); i++ ) {
	    			batch.add(ids.next());
	    		}
	    		if( __log.isTraceEnabled() ) __log.trace("BATCH updating " + batch.size() + " objects.");
	    		query.setParameter(parameterName, batch);
	    		query.executeUpdate();
	    		batch.clear();
    		}
    	}
    }
}