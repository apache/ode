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
 * Representation of a BPEL <code>&lt;receive&gt;</code> activity.
 */
public interface ReceiveActivity extends CreateInstanceActivity, Communication {

  /**
   * Set the optional message exchange identifier
   * @param messageExchange
   */
  void setMessageExchangeId(String messageExchange);
  
  /**
   * Get the optional message exchange identifier.
   * @return
   */
  String getMessageExchangeId();
  
  /**
   * Set the name of the variable that will hold the input message.
   *
   * @param variable name of input message variable
   */
  void setVariable(String variable);

  /**
   * Get the name of the variable that will hold the input message.
   *
   * @return name of input message variable
   */
  String getVariable();
}
