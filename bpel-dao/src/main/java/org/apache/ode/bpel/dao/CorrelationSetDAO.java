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
package org.apache.ode.bpel.dao;

import org.apache.ode.bpel.common.CorrelationKey;

import javax.xml.namespace.QName;
import java.util.Map;

/**
 * <p>
 * Data access object representing a BPEL correlation set.
 * Correlation sets are late-bound constants that "belong"
 * either to the process or to a scope.
 * </p>
 */
public interface CorrelationSetDAO {

  public Long getCorrelationSetId();

  /**
   * Get the name of the correlation set.
   * @return name of the correlation set
   */
  public String getName();

  /**
   * Get the scope instance to which this correlation set belongs.
   *
   * @see {@link ScopeDAO}
   * @return owning scope instance
   */
  public ScopeDAO getScope();

  /**
   * Sets the value of the correlation set.
   * @param names qualified names of the correlation set properties
   * @param values
   */
  public void setValue(QName[] names, CorrelationKey values);

  /**
   * Get the value of the correlation set.
   *
   * @return valu of correlation set
   */
  public CorrelationKey getValue();

  /**
   * Get correlation set properties with their values as a Map.
   * @return Map with the property qualified name as key and value as Map value.
   */
  public Map<QName, String> getProperties();

    /**
     * @return the process this correlation is related to, gives a chance of optimization to the underlying impl
     */
    ProcessDAO getProcess();
    /**
     * @return the instance this correlation is related to, gives a chance of optimization to the underlying impl
     */
    ProcessInstanceDAO getInstance();
}
