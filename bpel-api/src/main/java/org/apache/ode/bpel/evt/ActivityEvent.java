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
package org.apache.ode.bpel.evt;

/**
 * Base class for all activity events. Note that each activity occurs in some
 * scope, hence this class extends {@link ScopeEvent}.
 */
public abstract class ActivityEvent extends ScopeEvent {
  private String _activityName;
  private String _activityType;

  /** Activity declaration id. */
  private int _declarationId;

  /** Activity Id, unique for each activity <em>instance</em>. */
  private long _aid;

  public ActivityEvent() {
    super();
  }

  /**
   * Gets activity name
   *
   * @return name of activity
   */
  public String getActivityName() {
    return _activityName;
  }

  /**
   * Activity type e.g. switch, terminate, invoke
   * @return type of activity
   */
  public String getActivityType() {
    return _activityType;
  }

  public void setActivityName(String activityName) {
    _activityName = activityName;
  }

  public void setActivityType(String activityType) {
    _activityType = activityType;
  }

  public void setActivityDeclarationId(int declarationId) {
    _declarationId = declarationId;
  }

  public int getActivityDeclarationId() {
    return _declarationId;
  }

  public void setActivityId(long id) {
    _aid = id;
  }

  public long getActivityId() {
    return _aid;
  }

  public TYPE getType() {
    return TYPE.activityLifecycle;
  }

}
