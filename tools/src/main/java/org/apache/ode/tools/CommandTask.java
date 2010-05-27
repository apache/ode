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

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;


/**
 * Base <code>Task</code> that implements <code>CommandContext</code>.  This is
 * extended by other <code>Task</code> implementations that serve as
 * <code>Command</code> wrappers.
 */
public abstract class CommandTask extends Task implements CommandContext {

  public void outln(String s) {
    handleOutput(s);
  }

  public void out(String s) {
    handleOutput(s);
  }

  public void errln(String s) {
    handleErrorOutput(s);
  }

  public void error(String s) {
    log(s,Project.MSG_ERR);
  }

  public void error(String s, Throwable t) {
    log(s,Project.MSG_ERR);
  }

  public void warn(String s, Throwable t) {
    log(s,Project.MSG_WARN);
  }

  public void warn(String s) {
    log(s,Project.MSG_WARN);
  }

  public void info(String s) {
    log(s,Project.MSG_INFO);
  }

  public void info(String s, Throwable t) {
    log(s,Project.MSG_INFO);
  }

  public void debug(String s, Throwable t) {
    log(s,Project.MSG_VERBOSE);
  }

  public void debug(String s) {
    log(s,Project.MSG_VERBOSE);
  }
}
