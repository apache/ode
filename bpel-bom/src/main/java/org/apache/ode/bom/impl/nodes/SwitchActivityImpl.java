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
import org.apache.ode.bom.api.SwitchActivity;
import org.apache.ode.utils.NSContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * BPEL object model representation of a <code>&lt;switch&gt;</code> _activity.
 */
public class SwitchActivityImpl extends CompositeActivityImpl implements SwitchActivity {

  private static final long serialVersionUID = -1L;

  private ArrayList<Case> _cases = new ArrayList<Case>();

  /**
   * Constructor.
   *
   * @param nsContext namespace context
   */
  public SwitchActivityImpl(NSContext nsContext) {
    super(nsContext);
  }

  public SwitchActivityImpl() {
    super();
  }

  public List<Case> getCases() {
    return Collections.unmodifiableList(_cases);
  }


  public String getType() {
    return "switch";
  }

  public void addCase(Expression condition, Activity activity) {
    _cases.add(new CaseImpl(activity, condition));
  }

  /**
   * BPEL object model representation of a <code>&lt;case&gt;</code>.
   */
  static class CaseImpl extends BpelObjectImpl implements Case {

    private static final long serialVersionUID = 1L;
		private Expression _condition;
    private Activity _activity;

    /**
     * Constructor
     *
     * @param activity  the _activity for this case
     * @param condition the _condition for this case
     */
    CaseImpl(Activity activity, Expression condition) {
      this._activity = activity;
      this._condition = condition;
    }

    public void setActivity(Activity activity) {
      this._activity = activity;
    }

    public void setCondition(Expression condition) {
      _condition = condition;
    }

    /**
     * Get the _activity associated with this case.
     *
     * @return the _activity
     */
    public Activity getActivity() {
      return _activity;
    }

    /**
     * Get the _condition associated with this case.
     *
     * @return the _condition
     */
    public Expression getCondition() {
      return _condition;
    }
  }
}
