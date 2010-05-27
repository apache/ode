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

import org.apache.commons.logging.Log;

public class ClineCommandContext implements CommandContext {

  private Log _log;

  public ClineCommandContext(Log l) {
    _log = l;
  }

  public void outln(String s) {
    System.out.println(s);
  }

  public void out(String s) {
    System.out.print(s);
  }

  public void errln(String s) {
    System.err.println(s);
  }

  public void error(String s) {
    _log.error(s);
  }

  public void error(String s, Throwable t) {
    _log.error(s,t);
  }

  public void info(String s) {
    _log.info(s);
  }

  public void info(String s, Throwable t) {
    _log.info(s,t);
  }

  public void debug(String s, Throwable t) {
    _log.debug(s,t);
  }

  public void debug(String s) {
    _log.debug(s);
  }

  public void warn(String s) {
    _log.warn(s);
  }

  public void warn(String s, Throwable t) {
    _log.warn(s,t);
  }
}
