/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.utils.cli;

import java.util.ArrayList;
import java.util.List;


/**
 * <p>
 * A fragment implementation that picks-off the last argument on the line early.
 * For example, this would be a way to grab the destination directory of a UNIX
 * <code>mv</code> command.
 * </p>
 */
public class LastArgument extends Argument {
  
  public LastArgument(String argName, String description, boolean optional) {
    super(argName, description, optional);
  }
  
  public List<String> consume(List<String> s) throws CommandlineSyntaxException {
    // if there is only one item or there are no items, then the last item is
    // the same as the first.
    if (s.size() == 0 || s.size() == 1) {
      return super.consume(s);
    } else {
      ArrayList<String> l = new ArrayList<String>(s);
      String o = l.remove(l.size()-1);
      l.add(0,o);
      return super.consume(l);
    }
  }
}
