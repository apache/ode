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
package org.apache.ode.sax.evt;

import java.util.HashMap;
import java.util.Iterator;

import javax.xml.namespace.QName;

import org.xml.sax.Attributes;

public class XmlAttributes {
  
  private HashMap<QName,String> _hm;
  
  public XmlAttributes(Attributes atts) {
    _hm = new HashMap<QName,String>();
    for (int i=0; i < atts.getLength(); ++i) {
      _hm.put(new QName(atts.getURI(i),atts.getLocalName(i)),atts.getValue(i));
    }
  }
  
  public String getValue(QName qn) {
    return _hm.get(qn);
  }
  
  public boolean hasAtt(QName qn) {
    return _hm.get(qn) != null;
  }
  
  public boolean hasAtt(String s) {
    return _hm.get(new QName(s)) != null;
  }
  
  public String getValue(String s) {
    return getValue(new QName(s));
  }
  
  public int getCount() {
    return _hm.size();
  }
  
  public Iterator<QName> getQNames() {
    return _hm.keySet().iterator();
  }
  
  public String toString() {
    if (_hm.size() == 0) {
      return "<<none>>";
    }
    StringBuffer sb = new StringBuffer();
    boolean flag = true;
    for (Iterator<QName> it = getQNames(); it.hasNext(); ) {
      if (!flag) {
        sb.append(' ');
      }
      sb.append( it.next());
      flag = false;
    }
    return sb.toString();
  }
}
