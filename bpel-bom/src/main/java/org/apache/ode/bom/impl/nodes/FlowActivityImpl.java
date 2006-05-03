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
package org.apache.ode.bom.impl.nodes;

import org.apache.ode.bom.api.FlowActivity;
import org.apache.ode.bom.api.Link;
import org.apache.ode.utils.NSContext;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


/**
 * BPEL object model representation of a  <code>&lt;flow&gt;</code> activity.
 *
 * @see CompositeActivityImpl
 */
public class FlowActivityImpl extends CompositeActivityImpl implements FlowActivity {

  private static final long serialVersionUID = 1L;
	private HashSet<Link> _links = new HashSet<Link>();

  /**
   * Constructor.
   *
   * @param nsContext namespace context for this activity
   */
  public FlowActivityImpl(NSContext nsContext) {
    super(nsContext);

  }

  public FlowActivityImpl() {
    super();
  }

  public String getType() {
    return "flow";
  }

  public void addLink(Link link) {
    _links.add(link);
  }

  public Set<Link> getLinks() {
    return Collections.unmodifiableSet(_links);
  }

}
