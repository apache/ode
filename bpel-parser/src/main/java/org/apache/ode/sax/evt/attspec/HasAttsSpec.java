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
package org.apache.ode.sax.evt.attspec;

import org.apache.ode.sax.evt.XmlAttributes;

import javax.xml.namespace.QName;

public class HasAttsSpec implements XmlAttributeSpec {
  
  private QName[] _names;
  
  public HasAttsSpec(QName[] names) {
    _names = names;
  }
  
  public boolean matches(XmlAttributes xatts) {
    boolean match = true;
    for (int i=0; i < _names.length && match; ++i) {
      match &= xatts.getValue(_names[i]) != null;
    }
    return match;
  }
}
