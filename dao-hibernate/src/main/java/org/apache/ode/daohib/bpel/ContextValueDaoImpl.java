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
package org.apache.ode.daohib.bpel;

import org.apache.ode.bpel.dao.ContextValueDAO;
import org.apache.ode.daohib.SessionManager;
import org.apache.ode.daohib.bpel.hobj.HContextValue;

/**
 * Hibernate-based {@link ContextValueDAO} implementation.
 */
public class ContextValueDaoImpl extends HibernateDao implements ContextValueDAO {

    private HContextValue _self;

	public ContextValueDaoImpl(SessionManager sessionManager, HContextValue hobj) {
        super(sessionManager, hobj);
        entering("PartnerLinkDAOImpl.PartnerLinkDAOImpl");
        _self = hobj;
    }

	public String getKey() {
		return _self.getKey();
	}

	public String getNamespace() {
		return _self.getNamespace();
	}

	public String getValue() {
		return _self.getValue();
	}

	public void setKey(String key) {
		_self.setKey(key);
	}

	public void setNamespace(String namespace) {
		_self.setNamespace(namespace);
	}

	public void setValue(String value) {
		_self.setValue(value);
	}

}
