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

package org.apache.ode.bpel.engine;

import java.util.HashMap;
import java.util.Map;

/**
 * Wrapper around job detail map. 
 *
 */
public class WorkEvent {

  private Map<String,Object> _jobDetail;
  
  WorkEvent(Map<String,Object> jobDetail) {
    _jobDetail = jobDetail;
  }
  
  WorkEvent() {
    _jobDetail = new HashMap<String,Object>();
  }

  Long getIID() {
    return (Long)_jobDetail.get("iid");
  }
  
  Type getType() {
    return Type.valueOf((String)_jobDetail.get("type"));
  }
  
  void setType(Type timer) {
  
    _jobDetail.put("type", timer.toString());
    
  }

  
  Map<String,Object> getDetail() { 
    return _jobDetail;
  }
  
  public enum Type {
    TIMER,
    RESUME, 
    INVOKE_RESPONSE
  }

  public String getChannel() {
    return (String) _jobDetail.get("channel");
  }

  public void setIID(Long instanceId) {
    _jobDetail.put("iid", instanceId);
  }

  public void setChannel(String channel) {

    _jobDetail.put("channel", channel);
    
  }

  public String getMexId() {
    return (String) _jobDetail.get("mexid");
  }

  public void setMexId(String mexId) {
    _jobDetail.put("mexid", mexId);
  }
}
