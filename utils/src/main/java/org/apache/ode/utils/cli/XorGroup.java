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
import java.util.Iterator;
import java.util.List;

public class XorGroup implements CommandlineFragment {

  private List<CommandlineFragment> _frags;
  private CommandlineFragment _matched;
  private boolean _isOptional;
  private String _description;
  
  public XorGroup(String description, boolean optional) {
    _frags = new ArrayList<CommandlineFragment>();
    _description = description;
    _isOptional = optional;
  }
  
  public void reset() {
    _matched = null;
    for (Iterator<CommandlineFragment> it = _frags.iterator(); it.hasNext();) {
      it.next().reset();
    }
  }
  
  public void addFragment(CommandlineFragment frag) {
    _frags.add(frag);
  }
  
  public boolean didMatch() {
    return _matched != null;
  }
  
  public CommandlineFragment getMatched() {
    return _matched;
  }
  
  public List<String> consume(List<String> s) throws CommandlineSyntaxException {
    List<String> l = null;
    for (Iterator<CommandlineFragment> it = _frags.iterator(); it.hasNext();) {
      CommandlineFragment cf = it.next();
      try {
        l = cf.consume(s);
      } catch (CommandlineSyntaxException cse) {
        continue;
      }
      if (_matched == null) {
        _matched = cf;
      } else {
        throw new CommandlineSyntaxException("Multiple possibilities matched from " + getUsage() + ".");
      }
    }
    if (_matched == null) {
      if (!isOptional()) {
        throw new CommandlineSyntaxException("No possibility matched from " + getUsage() + ".");
      } else {
        return new ArrayList<String>(s);
      }
    } else {
      return l;
    }
  }

  public String getUsage() {
    StringBuffer sb = new StringBuffer();
    for (Iterator<CommandlineFragment> it = _frags.iterator(); it.hasNext(); ) {
      if (sb.length() != 0) {
        sb.append('|');
      }
      sb.append(it.next().getUsage());
    }
    return sb.toString();
  }

  public String getDescription() {
    return _description;
  }

  public boolean isOptional() {
    return _isOptional;
  }
}
