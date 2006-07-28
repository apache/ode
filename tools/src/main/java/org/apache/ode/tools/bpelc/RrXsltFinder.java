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

package org.apache.ode.tools.bpelc;

import org.apache.ode.utils.rr.ResourceRepository;
import org.apache.ode.bpel.compiler.XsltFinder;
import org.apache.ode.utils.StreamUtils;

import java.net.URI;
import java.net.URL;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implementation of the {@link XsltFinder} interface that obtains WSDLs from
 * inside a {@link org.apache.ode.sfwk.rr.ResourceRepository} object.
 */
public class RrXsltFinder implements XsltFinder {

  private static final Log __log = LogFactory.getLog(RrXsltFinder.class);
  private ResourceRepository _rr;
  private URI _base;

  public RrXsltFinder(ResourceRepository rr, URI base) {
    _rr = rr;
    _base = base;
  }

  public void setBaseURI(URI base) {
    _base = base;
  }

  public String loadXsltSheet(URI uri) {
    URL url = _rr.resolveURI(resolve(uri));
    if (url == null) {
      if (__log.isDebugEnabled())
        __log.debug("Resource repository does not contain resource: " + url);
      return null;
    }
    try {
      return new String(StreamUtils.read(url));
    } catch (IOException e) {
      if (__log.isDebugEnabled())
        __log.debug("error obtaining resource '" + uri + "' from repository.", e);
      return null;
    }
  }

  /**
   * Resolve the URI relative to the base.
   * @param uri
   * @return uri
   */
  private URI resolve(URI uri) {
    if (_base == null)
      return uri;
    return _base.resolve(uri);
  }

}
