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
 * Representation of the BPEL <code>&lt;switch&gt;</code> activity.
 */
public interface SwitchActivity extends Activity {
  /**
   * Get the cases for this switch.
   *
   * @return the cases
   */
  List<Case> getCases();

  /**
   * Add a case to this switch.
   *
   * @param condition the condition for the case to be added
   * @param activity  the activity
   */
  void addCase(Expression condition, Activity activity);


  /**
   * BPEL object model representation of a <code>&lt;case&gt;</code>.
   */
  public interface Case extends BpelObject {

    /**
     * Get the activity for this case.
     *
     * @return activity enabled when case is satisfied
     */
    public Activity getActivity();

    /**
     * Set the activity for this case.
     *
     * @param activity activity enabled when case is satisfied
     */
    public void setActivity(Activity activity);


    /**
     * Get the condition associated with this case.
     *
     * @return the condition
     */
    public Expression getCondition();

    /**
     * Set the condition associated with this case.
     *
     * @param condition the condition
     */
    public void setCondition(Expression condition);
  }
}
