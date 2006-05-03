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
package org.apache.ode.sax.evt.attspec;

import org.apache.ode.sax.evt.XmlAttributes;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.xml.namespace.QName;

public class FilterSpec implements XmlAttributeSpec {

  private Set<QName> _required;
  private Set<QName> _optional;
  
  public FilterSpec(String[] required, String[] optional) {
    _required = new HashSet<QName>();
    for (int i=0; i < required.length; ++i) {
      _required.add(new QName(required[i]));
    }
    _optional = new HashSet<QName>();
    for (int i=0; i < optional.length; ++i) {
      _optional.add(new QName(optional[i]));
    }
  }
  
  public FilterSpec(QName[] required, QName[] optional) {
    _required = new HashSet<QName>();
    for (int i=0; i < required.length; ++i) {
      _required.add(required[i]);
    }
    _optional = new HashSet<QName>();
    for (int i=0; i < optional.length; ++i) {
      _optional.add(optional[i]);
    }
  }
  
  /**
   * @see org.apache.ode.sax.evt.attspec.XmlAttributeSpec#matches(org.apache.ode.sax.evt.XmlAttributes)
   */
  public boolean matches(XmlAttributes atts) {
    // check for all required.
    for (Iterator<QName> it = _required.iterator(); it.hasNext();) {
      if (atts.getValue(it.next()) == null) {
        // TODO: return a message.
        return false;
      }
    }
    for (Iterator<QName> it = atts.getQNames(); it.hasNext();) {
      QName qn = it.next();
      if (!_required.contains(qn) && !_optional.contains(qn) && qn.getNamespaceURI().equals("")) {
        // TODO: return a message.
        return false;
      }
    }
    return true;
  }
}
