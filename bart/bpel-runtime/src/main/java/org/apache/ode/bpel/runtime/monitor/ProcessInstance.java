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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


/**
 * Describes a process instance.
 */
public class ProcessInstance implements Serializable {
  private static final long serialVersionUID = 1L;

  private String _instanceId;
  private String _rootScopeId;
  private long _createTime;
  private short _state;
  private Map<String, Properties> _correlationKeys = new HashMap<String, Properties>();

  /**
   * Constructor.
   * @param instanceId unique ID for this process instance
   * @param rootScopeId unique ID for the root scope of this process instance
   * @param createTime create time of this process instance
   * @param state state of this process instance
   */
  public ProcessInstance(String instanceId, String rootScopeId, long createTime, short state) {
    _instanceId = instanceId;
    _createTime = createTime;
    _rootScopeId = rootScopeId;
    _state = state;
  }

  /**
   * The correlation values for a correlation set.
   *
   * @param name name of the global correlation set.
   *
   * @return Values of the correlation set.
   */
  public Properties getCorrelation(String name) {
    return _correlationKeys.get(name);
  }

  /**
   * The time the instance was created.
   *
   * @return Returns the createTime.
   */
  public long getCreateTime() {
    return _createTime;
  }

  /**
   * Set of global correlation sets.
   *
   * @return Returns the names of global correlation sets.
   */
  public String[] getProcessCorrelations() {
    return _correlationKeys.keySet().toArray(new String[_correlationKeys.size()]);
  }

  /**
   * Unique identifier for the process instance.
   *
   * @return Returns the process instanceId.
   */
  public String getProcessInstanceId() {
    return _instanceId;
  }

  /**
   * Unique identifier for the root scope of the process.
   *
   * @return Returns the rootScopeId.
   */
  public String getRootScopeId() {
    return _rootScopeId;
  }

  /**
   * State of this process instance.
   *
   * @return staet of this process instance
   */
  public short getState() {
    return _state;
  }

  /**
   * Adds a global correlation set with its values
   *
   * @param setName correlation set name
   * @param values correlation set values
   */
  public void addCorrelationSet(String setName, Properties values) {
    _correlationKeys.put(setName, (values == null) ? new Properties() : values);
  }

}
