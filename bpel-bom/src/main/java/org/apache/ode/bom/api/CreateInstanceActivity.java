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
package org.apache.ode.bom.api;

/**
 * Interface implemented by activities that can be used to create an process
 * instance.
 */
public interface CreateInstanceActivity extends Activity {

  /**
   * Get the value of the createInstance flag.
   *
   * @return value of createInstance flag
   */
  boolean getCreateInstance();

  /**
   * Set the value of the createInstance flag.
   *
   * @param createInstance new createInstance flag
   */
  void setCreateInstance(boolean createInstance);

}
