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
package org.apache.ode.bpel.runtime.monitor;

import org.apache.ode.bpel.common.CorrelationKey;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * Filter for querying and inspecting process instances.
 */
public class InstanceFilter implements Serializable {
  private static final long serialVersionUID = 2078326392929490844L;
  private Date _startTime;
  private Date _endTime;
  private short[] _states;
  private int _maxReturn = 50;
  private int _skip = 0;
  private Map<String, CorrelationKey> _correlations =
    new HashMap<String, CorrelationKey>();

  /**
   * Returns the correlation correlationKey for a correlation set filter.
   *
   * @param setName correlation set filter name
   *
   * @return the correlation correlationKey
   */
  public CorrelationKey getCorrelationSetFilter(String setName) {
    return _correlations.get(setName);
  }

  /**
   * Returns the names of the correlation sets used in this filter.
   *
   * @return the names of the correlation sets used in this filter
   */
  public String[] getCorrelationSetFilters() {
    return _correlations.keySet().toArray(new String[_correlations.size()]);
  }

  /**
   * Filters all process instances <b>started</b> before the specified end
   * time. Not setting this field will ignore the end time.
   *
   * @param endTime The endTime to set.
   */
  public void setEndTime(Date endTime) {
    _endTime = endTime;
  }

  /**
   * Filters all process instances <b>started</b> before the specified end
   * time.
   *
   * @return Returns the end time filter.
   */
  public Date getEndTime() {
    return _endTime;
  }

  /**
   * The maximum number of instances to return.
   *
   * @param maxReturn The maxReturn to set.
   */
  public void setMaxReturn(int maxReturn) {
    _maxReturn = maxReturn;
  }

  /**
   * The maximum number of instances to return.
   *
   * @return Returns the maxReturn.
   */
  public int getMaxReturn() {
    return _maxReturn;
  }

  /**
   * The number of instances to skip.  Useful for paging for instance
   * iteration.
   *
   * @param skip The skip to set.
   */
  public void setSkip(int skip) {
    _skip = skip;
  }

  /**
   * The number of instances to skip.  Useful for paging for instance
   * iteration.
   *
   * @return Returns the skip.
   */
  public int getSkip() {
    return _skip;
  }

  /**
   * Filters all process instances <b>started</b> after the specified start
   * time. Not setting this field will ignore the start time.
   *
   * @param startTime The startTime to set.
   */
  public void setStartTime(Date startTime) {
    _startTime = startTime;
  }

  /**
   * Filters all process instances <b>started</b> after the specified start
   * time.
   *
   * @return Returns the startTime.
   */
  public Date getStartTime() {
    return _startTime;
  }

  /**
   * Returns only process instances in the specified states. States are
   * defined in <code>ProcessInstance</code>. Leaving this field blank will
   * return instances in any state.
   *
   * @param states The states to set.
   */
  public void setStates(short[] states) {
    _states = states;
  }

  /**
   * Returns only process instances in the specified states. States are
   * defined in <code>ProcessInstance</code>. Leaving this field blank will
   * return instances in any state.
   *
   * @return Returns the states.
   */
  public short[] getStates() {
    return _states;
  }

  /**
   * Add a correlation set filter.
   *
   * @param correlationSetName
   * @param correlation
   */
  public void addCorrelationSetFilter(String correlationSetName, CorrelationKey correlation) {
    _correlations.put(correlationSetName, correlation);
  }

}
