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
 * Representation of the BPEL <code>&lt;compensate&gt;</code> activity.
 */
public interface CompensateActivity extends Activity {

  /**
   * Set the name of the compensated scope: the scope which is compensated by this
   * activity.
   *
   * @param scope scope compensated by this activity
   */
  void setScopeToCompensate(String scope);

  /**
   * Get the name of the compensate scope: the scope which is compensated by this activity.
   *
   * @return scope compensated by this activity
   */
  String getScopeToCompensate();

}
