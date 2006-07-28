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
import org.apache.ode.bom.api.Expression;
import org.apache.ode.bom.api.LinkSource;

/**
 * Implementation of the {@link LinkSource} interface.
 */
public class LinkSourceImpl extends BpelObjectImpl implements LinkSource {
  private static final long serialVersionUID = 1L;

  private ActivityImpl _activity;
  private String _linkName;
  private Expression _transitionCondition;

  public Activity getActivity() {
    return _activity;
  }

  public String getLinkName() {
    return _linkName;
  }

  public void setLinkName(String linkName) {
    _linkName = linkName;
  }

  public Expression getTransitionCondition() {
    return _transitionCondition;
  }

  public void setTransitionCondition(Expression transitionCondition) {
    _transitionCondition = transitionCondition;
  }

  void setActivity(ActivityImpl activity) {
    _activity = activity;
  }
}
