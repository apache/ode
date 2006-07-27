/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
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
