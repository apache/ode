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

import java.util.Set;

/**
 * Reperesentation of a BPEL <code>&lt;pick&gt;</code> activity.
 */
public interface PickActivity extends CreateInstanceActivity {
  /**
   * Set the "create instance" flag.
   *
   * @param createInstance value of the "create instance" flag.
   */
  void setCreateInstance(boolean createInstance);

  /**
   * Check if the "create instance" flag is set.
   *
   * @return value of the "create instance" flag.
   */
  boolean isCreateInstance();

  /**
   * Add an 'onMessage' waiter.
   *
   * @param onMsg
   */
  void addOnMessage(OnMessage onMsg);

  /**
   * Return all 'onMessage' handlers.
   *
   * @return
   */
  Set<OnMessage> getOnMessages();

  /**
   * Adds an alarm for the pick.
   *
   * @param onAlarm
   */
  void addOnAlarm(OnAlarm onAlarm);

  /**
   * Gets the alarm for the pick; may be <code>null</code>.
   *
   * @return
   */
  Set<OnAlarm> getOnAlarms();
}
