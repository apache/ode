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
package org.apache.ode.bom.api;

/**
 * Assignment R-value defined in terms of a BPEL expression. This corresponds
 * to the "expression" form of the from-spec.
 * Note: With bpel 2.0, an expression may now be an L-value
 * (hence, the extension of 'To')
 */
public interface ExpressionVal extends From, To {
  /**
   * Get the R-Value {@link Expression}.
   * @return expression
   */
  Expression getExpression();

  /**
   * Set the R-Value {@link Expression}.
   * @param expression  r-valeu {@link Expression}
   */
  void setExpression(Expression expression);
}
