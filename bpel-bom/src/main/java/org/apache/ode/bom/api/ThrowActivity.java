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

import javax.xml.namespace.QName;

/**
 * Representation of the BPEL <code>&lt;throw&gt;</code> activity.
 */
public interface ThrowActivity extends Activity {

  /**
   * Set the thrown fault name.
   *
   * @param faultName name of thrown fault
   */
  void setFaultName(QName faultName);

  /**
   * Get the thrown fault name.
   *
   * @return name of thrown fault
   */
  QName getFaultName();

  /**
   * Set the fault variable.
   *
   * @param faultVariable name of the variable containing fault data
   */
  void setFaultVariable(String faultVariable);

  /**
   * Get the fault variable.
   *
   * @return name of variable containing fault data
   */
  String getFaultVariable();
}
