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
package org.apache.ode.sax.evt;

import org.apache.ode.utils.NSContext;

import javax.xml.namespace.QName;

import org.xml.sax.Locator;

public class StartElement extends SaxEvent {
  
  private QName _name;
  private XmlAttributes _atts;
  
  public StartElement(QName name, XmlAttributes atts, Locator loc, NSContext nsc) {
    super(loc,nsc);
    _name = name;
    _atts = atts;
  }
  
  public QName getName() {
    return _name;
  }
  
  public XmlAttributes getAttributes() {
    return _atts;
  }
  
  public short getType() {
    return START_ELEMENT;
  }
  
  public String toString() {
    return "<" + _name.toString() + ">";
  }
}
