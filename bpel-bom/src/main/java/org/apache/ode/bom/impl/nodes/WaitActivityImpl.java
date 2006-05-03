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

import org.apache.ode.bom.api.Expression;
import org.apache.ode.bom.api.WaitActivity;
import org.apache.ode.utils.NSContext;


/**
 * WaitActivityImpl
 */
public class WaitActivityImpl extends ActivityImpl implements WaitActivity {

  private static final long serialVersionUID = -1L;

  private Expression _for;
  private Expression _until;

  /**
   * Constructor.
   *
   * @param nsContext namespace context
   */
  public WaitActivityImpl(NSContext nsContext) {
    super(nsContext);
  }

  public WaitActivityImpl() {
    super();
  }

  public void setFor(Expression for1) {
    _for = for1;
  }

  public org.apache.ode.bom.api.Expression getFor() {
    return _for;
  }

  /**
   * @see ActivityImpl#getType()
   */
  public String getType() {
    return "wait";
  }

  public void setUntil(Expression until) {
    _until = until;
  }

  public Expression getUntil() {
    return _until;
  }
}
