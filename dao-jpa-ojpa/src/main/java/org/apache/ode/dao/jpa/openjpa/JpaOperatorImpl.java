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
package org.apache.ode.dao.jpa.openjpa;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.dao.jpa.JpaOperator;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.apache.openjpa.persistence.OpenJPAQuery;

/**
 * 
 * 
 * @author Matthieu Riou <mriou at apache dot org>
 * @author Jeff Yu
 */
public class JpaOperatorImpl implements JpaOperator {
	private static final Log __log = LogFactory.getLog(JpaOperatorImpl.class);

    public <T> void batchUpdateByIds(Iterator<T> ids, Query query, String parameterName) {
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

	public void setBatchSize(Query query, int limit) {
        OpenJPAQuery kq = OpenJPAPersistence.cast(query);
        kq.getFetchPlan().setFetchBatchSize(limit);
	}
}