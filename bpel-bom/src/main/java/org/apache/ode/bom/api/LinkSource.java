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
package org.apache.ode.bom.api;

/**
 * BOM representation of a "link source" (i.e. a <code>&lt;source...&gt;</code> element) declaration.
 * A {@link LinkSource} is a triple joining a link declaration (by reference), an activity
 * declaration (by context) and a transition condition (by containment).
 */
public interface LinkSource extends BpelObject {

  /**
   * Get the activity that declares this link source.
   *
   * @return declaring {@link Activity} object
   */
  Activity getActivity();

  /**
   * Get the refernced link.
   *
   * @return name of referenced link
   */
  String getLinkName();

  /**
   * Set the referenced link.
   *
   * @param linkName name of referenced link
   */
  void setLinkName(String linkName);


  /**
   * Get the link transition condition.
   *
   * @return transition condition {@link Expression}
   */
  Expression getTransitionCondition();

  /**
   * Set the link transition condition.
   *
   * @param transitionCondition transition condition {@link Expression}
   */
  void setTransitionCondition(Expression transitionCondition);

}
