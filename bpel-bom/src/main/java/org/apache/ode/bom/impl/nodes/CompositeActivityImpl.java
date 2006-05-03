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
package org.apache.ode.bom.impl.nodes;

import org.apache.ode.bom.api.Activity;
import org.apache.ode.bom.api.CompositeActivity;
import org.apache.ode.utils.NSContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Base class for all BPEL structured activities such as <code>flow</code>,
 * <code>sequence</code>, and <code>while</code>. This class provides
 * facilities for keeping track of child activities.
 */
public abstract class CompositeActivityImpl extends ActivityImpl implements CompositeActivity {

  private final ArrayList<Activity> _orderedChildren = new ArrayList<Activity>();

  protected CompositeActivityImpl() {
    super();
  }

  /**
   * Constructor.
   *
   * @param nsContext
   */
  protected CompositeActivityImpl(NSContext nsContext) {
    super(nsContext);
  }


  public List<Activity> getChildren() {
    return Collections.unmodifiableList(_orderedChildren);
  }

  public void removeChild(Activity childToRemove) {
    _orderedChildren.remove(childToRemove);
  }

  public void addChild(Activity childToAdd) {
    _orderedChildren.add(childToAdd);
  }

  public void addChild(int idx, Activity childToAdd) {
    _orderedChildren.add(idx, childToAdd);
  }

}
