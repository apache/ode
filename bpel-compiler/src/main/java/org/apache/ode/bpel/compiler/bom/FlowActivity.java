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
package org.apache.ode.bpel.compiler.bom;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Element;


/**
 * BOM representation of the BPEL <code>&lt;flow&gt;</code> activity.
 * See {@link CompositeActivity} for methods used to manipulate child activities.
 */
public class FlowActivity extends CompositeActivity {
  
  public FlowActivity(Element el) {
        super(el);
    }

/**
   * Get the set of links declared in this &lt;flow&gt; activity.
   * @return {@link Set} of {@link Link} declared in this &lt;flow&gt;
   */
  public List<Link> getLinks() {
      Links links = getFirstChild(Links.class);
      if (links == null)
          return Collections.emptyList();
      return links.getChildren(Link.class);
  }
}
