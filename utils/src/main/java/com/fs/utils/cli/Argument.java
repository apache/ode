/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.utils.cli;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * Models an argument, e.g., the <code>baz</code> in <code>foo baz</code>, 
 * where <code>foo</code> is the command.
 * </p>
 */
public class Argument implements CommandlineFragment {

  private String _argName;
  private String _description;
  private boolean _isOptional;
  private String _value;
  
  
  public Argument(String argName, String description, boolean optional) {
    _argName = argName;
    _description = description;
    _isOptional = optional;
  }
  
  public void reset() {
    _value = null;
  }
  
  public List<String> consume(List<String> s) throws CommandlineSyntaxException {
    if (s.size() == 0) {
      if (_isOptional) {
        ArrayList<String> l = new ArrayList<String>();
        Collections.copy(l,s);
        return l;
      } else {
        throw new CommandlineSyntaxException("The " + _argName + " argument is required.");
      }
    }
    ArrayList<String> l = new ArrayList<String>(s);
    _value = l.remove(0);
    return l;
  }

  public boolean isOptional() {
    return _isOptional;
  }
  
  public boolean isSet() {
    return _value != null;
  }
  
  public String getValue() {
    return _value;
  }
  
  public String getUsage() {
    return "<" + _argName + ">";
  }

  public String getDescription() {
    return _description;
  }
}
