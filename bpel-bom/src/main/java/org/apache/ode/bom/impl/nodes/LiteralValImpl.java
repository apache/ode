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
package org.apache.ode.bom.impl.nodes;

import org.apache.ode.bom.api.LiteralVal;
import org.apache.ode.utils.NSContext;

import org.w3c.dom.Element;

public class LiteralValImpl extends BpelObjectImpl implements LiteralVal {
  private static final long serialVersionUID = 1L;
	private Element _literalNode;

  public LiteralValImpl(NSContext nsctx) {
    super(nsctx);
  }

  public Element getLiteral() {
    return _literalNode;
  }

  public void setLiteral(Element literalNode) {
    _literalNode = literalNode;
  }
}
