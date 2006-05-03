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
import org.apache.ode.bom.api.ScopeActivity;
import org.apache.ode.utils.NSContext;


/**
 * ScopeActivityImpl definition
 *
 * @author jguinney
 */
public class ScopeActivityImpl extends ScopeImpl implements ScopeActivity {

  private static final long serialVersionUID = -1L;

  private Activity _child;

  /**
   * Constructor.
   *
   * @param nsContext namespace context
   */
  public ScopeActivityImpl(NSContext nsContext) {
    super(nsContext);
  }

  public ScopeActivityImpl() {
    super();
  }

  public void setChildActivity(Activity activity) {
    _child = activity;
  }

  public Activity getChildActivity() {
    return _child;
  }

  /**
   * @see Activity#getType()
   */
  public String getType() {
    return "scope";
  }
}
