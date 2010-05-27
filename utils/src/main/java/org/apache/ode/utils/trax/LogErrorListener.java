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
package org.apache.ode.utils.trax;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

import org.apache.commons.logging.Log;

public class LogErrorListener implements ErrorListener {

  private Log _log;

  public LogErrorListener(Log log) {
    _log = log;
  }

  public void warning(TransformerException exception)
      throws TransformerException {
    _log.warn(exception.getMessageAndLocation(),exception);
  }

  public void error(TransformerException exception) throws TransformerException {
    _log.error(exception.getMessageAndLocation(),exception);
  }

  public void fatalError(TransformerException exception)
      throws TransformerException {
    _log.fatal(exception.getMessageAndLocation(),exception);
  }

}
