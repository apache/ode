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
package org.apache.ode.utils.cli;

import java.util.ArrayList;
import java.util.List;

public class Flag implements CommandlineFragment {

  private String _name;
  private boolean _isSet;
  private String _description;
  private boolean _isOptional;
  
  public Flag(String name, String description, boolean optional) {
    _name = COMMAND_PREFIX + name;
    _description = description;
    _isSet = false;
    _isOptional = optional;
  }
  
  public void reset() {
    _isSet = false;
  }
  
  public boolean isOptional() {
    return _isOptional;
  }
  public List<String> consume(List<String> s) throws CommandlineSyntaxException {
    _isSet = s.contains(_name);
    if (!_isSet && !_isOptional) {
      throw new CommandlineSyntaxException("The " + _name + " flag is required.");
    }
    ArrayList<String> l = new ArrayList<String>(s);
    l.remove(_name);
    if (l.contains(_name)) {
      throw new CommandlineSyntaxException("The " + _name + " flag can appear at most once.");
    }
    return l;
  }

  public boolean isSet() {
    return _isSet;
  }
  
  public String getUsage() {
    return _name;
  }

  public String getDescription() {
    return _description;
  }

  public boolean validate() {
    return true;
  }
}
