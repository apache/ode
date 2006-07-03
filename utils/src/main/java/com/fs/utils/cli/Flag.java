/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.utils.cli;

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
