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
package org.opentools.minerva;

import java.util.Hashtable;

import javax.naming.*;
import javax.naming.spi.ObjectFactory;

import org.opentools.minerva.connector.jdbc.JDBCDataSource;


/**
 * Uses JCA style
 */
public class MinervaJDBCDataSourceFactory implements ObjectFactory {
  
  static final String REF_ID = "refId";

  /**
   * @see javax.naming.spi.ObjectFactory#getObjectInstance(java.lang.Object,
   *      javax.naming.Name, javax.naming.Context, java.util.Hashtable)
   */
  public Object getObjectInstance(Object obj, Name n, Context nameCtx,
                                  Hashtable environment)
                           throws Exception {
    Reference ref = (Reference)obj;
    MinervaPool pool = MinervaPool.get((String)((StringRefAddr)ref.get(REF_ID)).getContent());
    return new JDBCDataSource(pool._connManager, pool._mcf);
  }
  
}
