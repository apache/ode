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

import org.apache.ode.bpel.dao.MessageRouteDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.daohib.SessionManager;
import org.apache.ode.daohib.bpel.hobj.HCorrelatorSelector;

/**
 * Hibernate-based {@link MessageRouteDAO} implementation.
 */
class MessageRouteDaoImpl extends HibernateDao implements MessageRouteDAO {
	
  private HCorrelatorSelector _selector;

	public MessageRouteDaoImpl(SessionManager sm, HCorrelatorSelector hobj) {
		super(sm, hobj);
    _selector = hobj;
	}
	/**
	 * @see org.apache.ode.bpel.dao.MessageRouteDAO#getTargetInstance()
	 */
	public ProcessInstanceDAO getTargetInstance() {
		return new ProcessInstanceDaoImpl(_sm, _selector.getInstance());
	}

  public String getGroupId() {
    return _selector.getGroupId();
  }

  public int getIndex() {
    return _selector.getIndex();
  }

}
