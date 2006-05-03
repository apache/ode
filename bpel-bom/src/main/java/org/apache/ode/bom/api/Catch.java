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
 * Representation of a BPEL fault handler catch block.
 */
public interface Catch extends Scope, BpelObject {
  /**
   * Get the activity for this catch block. This is the activity that
   * is activated if this catch block is enabled.
   *
   * @return catch activity fault handling activity
   */
  Activity getActivity();

  /**
   * Set the activity for this catch block. This is the activity that
   * is activated if this catch block is enabled.
   *
   * @param activity fault handling activity
   */
  void setActvity(Activity activity);

  /**
   * Sets the name of the fault.  Optional setting.
   *
   * @param name fault name (or <code>null</code>)
   */
  void setFaultName(QName name);

  /**
   * Get the name of the fault.  May be <code>null</code>.
   *
   * @return fault name or <code>null</code>
   */
  QName getFaultName();

  /**
   * Get the fault variable.  May be <code>null</code>
   *
   * @return name of the fault variable
   */
  String getFaultVariable();

  /**
   * Set the fault variable.  May be <code>null</code>
   *
   * @param faultVariable name of the fault variable
   */
  void setFaultVariable(String faultVariable);

  /**
   * Get the fault variable type. The fault variable type
   * must be specified in BPEL 2.0 if the fault variable
   * is set.
   *
   * @return fault variable type or <code>null</code> if none specified.
   */
  QName getFaultVariableMessageType();

  /**
   * Set the fault variable type. The fault variable type
   * must be specified in BPEL 2.0 if the fault variable
   * is set.
   *
   * @param faultVariableType new fault variable type or <code>null</code>.
   */
  void setFaultVariableMessageType(QName faultVariableType);
  
  /**
   * Get the fault variable type. The fault variable type
   * must be specified in BPEL 2.0 if the fault variable
   * is set.
   *
   * @return fault variable type or <code>null</code> if none specified.
   */
  QName getFaultVariableElementType();

  /**
   * Set the fault variable type. The fault variable type
   * must be specified in BPEL 2.0 if the fault variable
   * is set.
   *
   * @param faultVariableType new fault variable type or <code>null</code>.
   */
  void setFaultVariableElementType(QName faultVariableType);
  
}
