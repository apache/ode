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

import java.util.Collections;
import java.util.List;

/**
 * <p>
 * A multiple argument commandline fragment that greedily consumes what's left on
 * the commandline as arguments.
 * </p>
 */
public class MultiArgument implements CommandlineFragment {

  private String _argName;
  private String _description;
  private String[] _values;
  private boolean _isOptional;


  public MultiArgument(String argName, String description, boolean optional) {
    _argName = argName;
    _description = description;
    _isOptional = optional;
  }

  public void reset() {
    _values = null;
  }

  public List<String> consume(List<String> s) throws CommandlineSyntaxException {
    if(s.size() == 0 && !_isOptional) {
      throw new CommandlineSyntaxException("The " + _argName + " argument is required.");
    }
    if (s.size() != 0) {
      _values = s.toArray(new String[s.size()]);
      for (int i=0; i < _values.length; ++i) {
        if (_values[i].startsWith(COMMAND_PREFIX)) {
          throw new CommandlineSyntaxException("The flag " + _values[i] +
              " was found where an argument <" + _argName + "> was expected.");
        }
      }
    }
    return Collections.emptyList();
  }

  public boolean isOptional() {
    return _isOptional;
  }

  public boolean isSet() {
    return _values != null;
  }

  public String[] getValues() {
    return _values;
  }

  public String getUsage() {
    return "<" + _argName + "_1> ... <" + _argName + "_n>";
  }

  public String getDescription() {
    return _description;
  }
}
