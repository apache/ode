/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.     
 */
package org.apache.ode.tools.rr.ant;

import org.apache.ode.utils.rr.ResourceRepositoryBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.tools.ant.Project;


/**
 * Sub-element of {@link org.apache.ode.tools.rr.ant.RrTask} for
 * setting up URI <em>aliases</em>. An alias is analogous
 * to a "soft link": it makes a resource already in the repository
 * accessible via an alternate URI.
 *
 * <p>
 * For example, the following:
 * <pre>
 * &lt;rr ... &gt;
 *    &lt;resource uri="uri:bar" file="bar.xml" /&gt;
 *    &lt;alias fromuri="uri:foo" touri="uri:bar" /&gt;
 * &lt;/rr &gt;
 * </pre>
 * creates a resource repository where the XML resource "bar.xml" is accessible both
 * via the "uri:foo" and "uri:bar" URIs.
 */
public class AliasElement implements RrOperation {
  
  private String _fromUri;
  private String _toUri;
  
  /**
   * Set the alias URI.
   * @param from alias URI
   */
  public void setFromUri(String from) {
    _fromUri = from;
  }

  /**
   * Get the alias URI.
   * @return alias URI
   */
  public String getFromUri() {
    return _fromUri;
  }

  /**
   * Set the <em>aliased</em> URI
   * @param to <em>aliased</em> URI
   */
  public void setToUri(String to) {
    _toUri = to;
  }

  /**
   * Get the <em>aliased</em> URI.
   * @return the <em>aliased</em> URI
   */
  public String getToUri() {
    return _toUri;
  }

  public void execute(RrTask executingTask, ResourceRepositoryBuilder rrb)
    throws URISyntaxException, IOException {
    String from = _fromUri;
    String to = _toUri;

    if (from == null || to == null) {
      from = ((from == null) ? "<<null>>" : from);
      to = ((to == null) ? "<<null>>" : to);
      executingTask.log("Unable to alias " + from + " to " + to + "; skipping.", Project.MSG_WARN);
      return;
    }

    rrb.addAlias(new URI(from), new URI(to));
    executingTask.log("Aliased " + from + " to " + to, Project.MSG_VERBOSE);
  }
}
