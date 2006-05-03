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

import org.apache.ode.bom.api.PropertyVal;
import org.apache.ode.utils.NSContext;

import javax.xml.namespace.QName;

public class PropertyValImpl extends BpelObjectImpl implements PropertyVal {
  private static final long serialVersionUID = 1L;

  private String _variable;
  private QName _property;

  public PropertyValImpl(NSContext nsContext) {
    super(nsContext);
  }

  public String getVariable() {
    return _variable;
  }

  public void setVariable(String variable) {
    _variable = variable;
  }

  public QName getProperty() {
    return _property;
  }

  public void setProperty(QName property) {
    _property = property;
  }

}
