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
 * Representation of the BPEL <code>&lt;reply&gt;</code> activity.
 */
public interface ReplyActivity extends Activity, Communication {

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
   * Set the fault name with which to reply.
   *
   * @param name the fault name or <code>null</code> to disable fault reply.
   */
  void setFaultName(QName name);

  /**
   * Get the fault name with which to reply.
   *
   * @return the fault name
   */
  QName getFaultName();


  /**
   * Set the variable containing the reply message.
   *
   * @param variable name of variable containing the reply message
   */
  void setVariable(String variable);

  /**
   * Get the variable containing the reply message.
   *
   * @return name of variable containing the reply message
   */
  String getVariable();


}
