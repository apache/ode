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

/**
 * A representation of a BPEL link target. A link target is a tuple that joins
 * a link decleration (by reference) and an activity (by context).
 */
public interface LinkTarget extends BpelObject {
  /**
   * Get the activity that owns this link target.
   *
   * @return owner {@link Activity} object
   */
  Activity getActivity();

  /**
   * Get the name of the refernced link.
   *
   * @return link name
   */
  String getLinkName();

  /**
   * Set the name of the referenced link.
   *
   * @param linkName link name
   */
  void setLinkName(String linkName);

}
