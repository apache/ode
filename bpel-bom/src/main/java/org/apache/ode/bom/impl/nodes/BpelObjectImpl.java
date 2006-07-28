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

import org.apache.ode.bom.api.BpelObject;
import org.apache.ode.utils.NSContext;

import java.io.Serializable;

/**
 * Base class for all objects in the BPEL object model.
 */
public abstract class BpelObjectImpl implements Serializable, BpelObject {

  private int _lineNo = -1;
  private NSContext _namespaceCtx;
  private String _description;

  protected BpelObjectImpl() {
  }

  protected BpelObjectImpl(NSContext nsctx) {
    _namespaceCtx = nsctx;
  }

  public int getLineNo() {
    return _lineNo;
  }

  public void setLineNo(int lineNo) {
    _lineNo = lineNo;
  }

  public NSContext getNamespaceContext() {
    return _namespaceCtx;
  }

  public void setNamespaceContext(NSContext ctx) {
    assert ctx != null;
    _namespaceCtx = ctx;
  }

  public String getDescription() {
    return _description;
  }

  public void setDescription(String description) {
    _description = description;
  }

}
