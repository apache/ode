package org.apache.ode.dao.jpa;

import java.util.Iterator;

import javax.persistence.Query;

/**
 * this is interface that will include the methods that will be used in JPA DAO,
 * But the implementation should be different from various JPA vendor, like OpenJPA, Hibernate etc.
 * 
 * @author Jeff Yu
 *
 */
public interface JpaOperator {
	
	public <T> void batchUpdateByIds(Iterator<T> ids, Query query, String parameterName);
	
	public void setBatchSize(Query query, int limit);

}