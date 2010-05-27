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

import org.w3c.dom.Element;

/**
 * Representation of the BPEL <code>&lt;wait&gt;</code> activity.
 */
public class WaitActivity extends Activity {

  public WaitActivity(Element el) {
        super(el);
    }

/**
   * Get the for expression.
   *
   * @return Returns the for.
   */
  public Expression getFor() {
      return  (Expression) getFirstChild(rewriteTargetNS(Bpel20QNames.FOR));
  }

  /**
   * Get the "until" expression.
   *
   * @return the "until" expression
   */
  public Expression getUntil() {
      return  (Expression) getFirstChild(rewriteTargetNS(Bpel20QNames.UNTIL));
  }
}
