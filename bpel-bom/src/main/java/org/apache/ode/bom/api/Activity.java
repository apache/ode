/*
 * Copyright 2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ode.bom.api;

import java.util.Set;

/**
 * Interface common to all BPEL activities. This interface provides methods for manipulating the
 * so-called "common attributes" such as source and target links, activity name, and supress join
 * failure flag.
 */
public interface Activity extends BpelObject, JoinFailureSuppressor {

  /**
   * Set the join condition.
   *
   * @param joinCondition the join condition
   */
  void setJoinCondition(Expression joinCondition);

  /**
   * Get the join condition.
   *
   * @return the join condition
   */
  Expression getJoinCondition();

  /**
   * Get the {@link LinkSource}s for this activity.
   *
   * @return set of {@link LinkSource}s
   */
  Set<LinkSource> getLinkSources();

  /**
   * Get the {@link LinkTarget}s for this activity.
   *
   * @return set of {@link LinkTarget}s
   */
  Set<LinkTarget> getLinkTargets();

  /**
   * The type of activity (e.g. "while", "sequence", "etc")
   *
   * @return activity type string
   */
  String getType();

  /**
   * Add a link source to this activity.
   *
   * @param linkSource link source to add
   */
  void addSource(LinkSource linkSource);

  /**
   * Add a link target to this activity.
   *
   * @param linkTarget link target to add
   */
  void addTarget(LinkTarget linkTarget);

  /**
   * Set (or clear) the user-defined name of this activity.
   *
   * @param name user-defined name or <code>null</code> to clear
   */
  void setName(String name);

  /**
   * Get the (optional) user-defined name for this activity.
   *
   * @return user-defined name or <code>null</code>
   */
  String getName();


}
