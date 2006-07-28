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

package org.apache.ode.bpel.compiler;

import org.apache.ode.utils.StreamUtils;
import java.net.URI;
import java.io.IOException;
import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

class DefaultXsltFinder implements XsltFinder {

  private static final Log __log = LogFactory.getLog(DefaultXsltFinder.class);

  private URI _base;

  public DefaultXsltFinder() {
    // no base URL
  }

  public DefaultXsltFinder(URI u) {
    setBaseURI(u);
  }

  public void setBaseURI(URI u) {
    File f = new File(u);
    if (f.exists() && f.isFile()) {
      _base = f.getParentFile().toURI();
    } else {
      _base = u;
    }
  }

  public String loadXsltSheet(URI uri) {
    try {
      return new String(StreamUtils.read(_base.resolve(uri).toURL()));
    } catch (IOException e) {
      if (__log.isDebugEnabled())
        __log.debug("error obtaining resource '" + uri + "' from repository.", e);
      return null;
    }
  }

}
