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

/**
 * <p>
 * A fragment that represents a flag with an argument, e.g., <code>--foo bar</code>.
 * </p>
 */
public class FlagWithArgument implements CommandlineFragment {

  private String _name;
  private String _argName;
  private String _description;
  private String _arg;
  private boolean _isOptional;

  /**
   * Construct a new fragment holder.
   * @param name the name of the flag, as it will be used on the commandline
   * @param argName the name of the argument, as it will be used to generate usage and help
   * @param description the description of the flag
   * @param optional whether or not this flag is optional
   */
  public FlagWithArgument(String name, String argName, String description, boolean optional) {
    _isOptional = false;
    _name = COMMAND_PREFIX + name;
    _description = description;
    _argName = argName;
    _isOptional = optional;
  }

  public boolean isOptional() {
    return _isOptional;
  }

  public void setOptional(boolean o) {
    _isOptional = o;
  }

  public List<String> consume(List<String> s) throws CommandlineSyntaxException {
    if (!s.contains(_name)) {
      if (!_isOptional) {
        throw new CommandlineSyntaxException(
            "The " + _name + " flag with an argument is required.");
      } else {
        return s;
      }
    }
    ArrayList<String> l = new ArrayList<String>(s);
    int idx = l.indexOf(_name);
    if (idx == l.size()-1 || (l.get(idx+1)).startsWith(COMMAND_PREFIX)) {
      throw new CommandlineSyntaxException(
          "The " + _name + " flag requires an argument.");
    }
    _arg = l.get(idx+1);
    l.remove(idx+1);
    l.remove(idx);
    return l;
  }

  public void reset() {
    _arg = null;
  }

  public boolean isSet() {
    return _arg != null;
  }

  public String getValue() {
    return _arg;
  }

  public String getUsage() {
    return _name + " <" + _argName + ">";
  }

  public String getDescription() {
    return _description;
  }

  public boolean validate() {
    return true;
  }
}
