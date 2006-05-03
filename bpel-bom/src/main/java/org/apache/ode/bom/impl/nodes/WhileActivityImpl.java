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
import org.apache.ode.bom.api.Expression;
import org.apache.ode.bom.api.WhileActivity;
import org.apache.ode.utils.NSContext;

/**
 * BPEL object model representation of a <code>&lt;while&gt;</code> activity.
 */
public class WhileActivityImpl extends ActivityImpl implements WhileActivity {

  private static final long serialVersionUID = -1L;

  /**
   * Constructor.
   *
   * @param nsContext namespace context
   */
  public WhileActivityImpl(NSContext nsContext) {
    super(nsContext);
  }

  private Activity _activity;

  private Expression _condition;

  public WhileActivityImpl() {
    super();
  }

  public void setActivity(Activity activity) {
    _activity = activity;
  }

  public org.apache.ode.bom.api.Activity getActivity() {
    return _activity;
  }

  public void setCondition(Expression condition) {
    _condition = condition;
  }

  public Expression getCondition() {
    return _condition;
  }

  /**
   * @see Activity#getType()
   */
  public String getType() {
    return "while";
  }
}
