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

import java.util.List;

/**
 * Base interface for BPEL composite activities <code>&lt;flow&gt;</code> and <code>&lt;sequence&gt;</code>.
 * This interface provides methods for manipulating an ordered list of child activities.
 */
public interface CompositeActivity extends Activity {

  /**
   * Get the (ordered) list of child activities.
   *
   * @return immutable list of child {@link Activity} objects
   */
  List<Activity> getChildren();

  /**
   * Remove a child from the list of child activities.
   *
   * @param childToRemove child {@link Activity} to remove
   */
  void removeChild(Activity childToRemove);

  /**
   * Add (append) a child to the list of child activities.
   *
   * @param childToAdd {@link Activity} to add to end of list
   */
  void addChild(Activity childToAdd);

  /**
   * Add a child to the list of child activities.
   *
   * @param idx        position (index)
   * @param childToAdd {@link Activity} to add
   */
  void addChild(int idx, Activity childToAdd);

}
