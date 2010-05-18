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

package org.apache.ode.bpel.pmapi;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.StringTokenizer;

/**
 * Used to customize the response document provided by most methods returning
 * process info.
 */
public class ProcessInfoCustomizer {

  public static final ProcessInfoCustomizer ALL = new ProcessInfoCustomizer(Item.ENDPOINTS,Item.PROPERTIES,Item.DOCUMENTS,Item.SUMMARY);
  public static final ProcessInfoCustomizer SUMMARYONLY = new ProcessInfoCustomizer(Item.SUMMARY);
  public static final ProcessInfoCustomizer NONE = new ProcessInfoCustomizer();

  private HashSet<Item> _includes = new HashSet<Item>();
  
  public ProcessInfoCustomizer(String value) {
    StringTokenizer stok = new StringTokenizer(value,",",false);
    while (stok.hasMoreTokens()) {
      String t = stok.nextToken();
      Item i = Item.valueOf(t);
      _includes.add(i);
    }
  }
  
  public String toString() {
    StringBuilder buf = new StringBuilder();
    boolean first = false;
    for (Item i : _includes) {
      if (first)
        first = false;
      else
        buf.append(',');
      buf.append(i.toString());
    }
    return buf.toString();
  }
  
  public ProcessInfoCustomizer(Item... items) {
    for (Item i : items)
      _includes.add(i);
  }

  public boolean includeInstanceSummary() {
    return _includes.contains(Item.SUMMARY);
  }

  public boolean includeDocumentLists() {
    return _includes.contains(Item.DOCUMENTS);
  }

  public boolean includeProcessProperties() {
    return _includes.contains(Item.PROPERTIES);
  }

  public boolean includeEndpoints() {
    return _includes.contains(Item.ENDPOINTS);
  }

  public enum Item {
    SUMMARY,
    DOCUMENTS,
    PROPERTIES,
    ENDPOINTS
  }

}
