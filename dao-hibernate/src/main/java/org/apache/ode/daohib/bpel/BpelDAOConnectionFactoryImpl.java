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

import org.apache.ode.bpel.dao.BpelDAOConnection;
import org.apache.ode.bpel.dao.BpelDAOConnectionFactory;
import org.apache.ode.daohib.SessionManager;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;

/**
 * Hibernate-based {@link org.apache.ode.bpel.dao.BpelDAOConnectionFactory}
 * implementation.
 */
public class BpelDAOConnectionFactoryImpl implements BpelDAOConnectionFactory {
  private static final Log __log = LogFactory
      .getLog(BpelDAOConnectionFactoryImpl.class);

  private SessionManager _sessionManager;

  /**
   * Constructor.
   */
  public BpelDAOConnectionFactoryImpl(SessionManager sessionManager) {
    _sessionManager = sessionManager;
  }

  public BpelDAOConnection getConnection() {
    try {
      return new BpelDAOConnectionImpl(_sessionManager);
    } catch (HibernateException e) {
      __log.error("DbError", e);
      throw e;
    }
  }

  /**
   * @see org.apache.ode.bpel.dao.BpelDAOConnectionFactory#init(java.util.Properties)
   */
  public void init(Properties properties) {
  }
}
