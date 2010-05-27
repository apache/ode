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
package org.apache.ode.tools.bpelc.ant;

import org.apache.ode.tools.CommandTask;
import org.apache.ode.tools.ExecutionException;
import org.apache.ode.tools.bpelc.BpelCompileCommand;
import org.apache.tools.ant.BuildException;

import java.io.File;

public class BpelCTask extends CommandTask {

  BpelCompileCommand _bcc = new BpelCompileCommand();

  public void setTargetdir(File f) {
    _bcc.setOuputDirectory(f);
  }

  public void addConfiguredBpel(BpelSrcElement bse) {
    if (bse.getUrl() == null || bse.getUrl().trim().length() == 0) {
      throw new BuildException("The url attribute is required.");
    }
    _bcc.addBpelProcessUrl(bse.getUrl());
  }

  public void setWsdl(String uri) throws BuildException {
    _bcc.setWsdlImportUri(uri);
  }

  /**
   * @see org.apache.tools.ant.Task#execute()
   */
  public void execute() throws BuildException {
    try {
      _bcc.execute(this);
    } catch (ExecutionException ee) {
      throw new BuildException(ee.getMessage(),ee);
    }
  }
}
