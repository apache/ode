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

package org.apache.ode.tools;

import org.apache.ode.utils.msg.MessageBundle;

public class ToolMessages extends MessageBundle {

  public String msgBadUrl(String url, String message) {
    return this.format("{0} does not appear to be a valid URL: {1}", url, message);
  }

  public String msgSoapErrorOnSend(String msg) {
    return this.format("Unable to send message due to SOAP-related error: {0}", msg);
  }

  public String msgIoErrorOnSend(String msg) {
    return this.format("Unable to send message due to I/O-related error: {0}", msg);
  }

}
