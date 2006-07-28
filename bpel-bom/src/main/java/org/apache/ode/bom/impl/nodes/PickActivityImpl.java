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
package org.apache.ode.bom.impl.nodes;

import org.apache.ode.bom.api.Activity;
import org.apache.ode.bom.api.OnAlarm;
import org.apache.ode.bom.api.OnMessage;
import org.apache.ode.bom.api.PickActivity;
import org.apache.ode.utils.NSContext;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * BPEL object model representation of a <code>&lt;pick&gt;</code> activity.
 */
public class PickActivityImpl extends ActivityImpl implements PickActivity {
  private static final long serialVersionUID = -1L;

  private boolean _createInstance = false;

  private HashSet<OnMessage> _onMessages = new HashSet<OnMessage>();
  private HashSet<OnAlarm> _onAlarms = new HashSet<OnAlarm>();

  /**
   * Constructor.
   *
   * @param nsContext namespace context
   */
  public PickActivityImpl(NSContext nsContext) {
    super(nsContext);
  }

  public PickActivityImpl() {
    super();
  }


  public boolean getCreateInstance() {
    return _createInstance;
  }

  public void setCreateInstance(boolean createInstance) {
    _createInstance = createInstance;
  }

  public boolean isCreateInstance() {
    return _createInstance;
  }

  /**
   * @see Activity#getType()
   */
  public String getType() {
    return "pick";
  }

  public void addOnMessage(OnMessage onMsg) {
    ((OnMessageImpl) onMsg).setDeclaredIn(this);
    _onMessages.add(onMsg);
  }

  public Set <OnMessage> getOnMessages() {
    return Collections.unmodifiableSet(_onMessages);
  }

  public void addOnAlarm(OnAlarm onAlarm) {
    ((OnAlarmImpl) onAlarm).setDeclaredIn(this);
    _onAlarms.add(onAlarm);
  }

  public Set<OnAlarm> getOnAlarms() {
    return Collections.unmodifiableSet(_onAlarms);
  }

}
