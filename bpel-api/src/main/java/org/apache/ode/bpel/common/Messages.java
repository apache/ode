
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

package org.apache.ode.bpel.common;

import org.apache.ode.utils.msg.MessageBundle;

import java.util.Collection;

/**
 * Human-readable messages for the common classes.
 */
public class Messages extends MessageBundle {

  /**
   * The filter "{0}" is not recognized; try one of {1}.
   */
  public String msgUnrecognizedFilterKey(String filterKey, Collection<String> filterKeys) {
    return this.format("The filter \"{0}\" is not recognized; try one of {1}.", filterKey,
        filterKeys);
  }

  /**
   * The restriction for filter "{0}" must follow the ISO-8601 date or date/time
   * standard (yyyyMMddhhmmss); "{1}" does not follow this form.
   */
  public String msgISODateParseErr(String filterKey, String restriction) {
    return this.format("The restriction for filter \"{0}\" must follow the"
        + " ISO-8601 date or date/time standard (yyyyMMddhhmmss);"
        + "\"{1}\" does not follow this form.", filterKey, restriction);
  }

}
