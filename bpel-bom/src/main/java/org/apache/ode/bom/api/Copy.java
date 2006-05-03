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
 * Assignment copy entry. Each copy entry consists of a
 * "left hand side" (L-value) and a "right hand side (R-value).
 * The value on the right hand side is copied to the location
 * referenced in the left hand side.
 */
public interface Copy extends BpelObject {

  /**
   * Get the L-value.
   *
   * @return the L-value.
   */
  To getTo();

  /**
   * Set the L-value.
   *
   * @param to the L-value
   */
  void setTo(To to);

  /**
   * Get the R-value.
   *
   * @return the R-value.
   */
  From getFrom();

  /**
   * Set the R-value.
   *
   * @param from the R-value
   */
  void setFrom(From from);

  boolean isKeepSrcElement();

  void setKeepSrcElement(boolean keepSrcElement);

}
