/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.utils.cli;

import java.util.List;

/**
 * <p>
 * Basic interface for a fragment of a commandline; that fragment could be a single
 * flag, could be a flag with one or more arguments, or could be an argument.
 * </p>
 */
public interface CommandlineFragment {
  
  /**
   * The prefix (&quot;-&quot;) that signifies that an argument is a flag.
   */
  public static String COMMAND_PREFIX = "-";
  
  /**
   * <p>
   * Reset the fragment to its initial state.  This is useful for reusing fragments
   * in multiple commandline structures.
   * </p>
   */
  public void reset();
  
  /**
   * <p>
   * Grab the pieces of the commandline relevant to this argument, configure the
   * implementation, and then return a new array of arguments that will have the
   * pieces of the original commandline that this command used removed.
   * </p>
   * @param s the list of arguments
   * @return the list of arguments, post parse
   * @throws CommandlineSyntaxException if the commandline is <em>structurally</em> invalid.
   */
  public List<String> consume(List<String> s) throws CommandlineSyntaxException;
  
  /**
   * <p>
   * Construct a usage string for this commandline fragment.  The usage string is
   * used when constructing a strawman commandline example and when formatting help.
   * </p>
   * @return the usage string 
   */
  public String getUsage();
  
  /**
   * <p>
   * Return a description of this commandline fragment.  The description should be a
   * (short) narrative item that describes the purpose of the fragment.  It is only
   * used when formatting help.
   * </p>
   * @return the description
   */
  public String getDescription();
  
  /**
   * @return <code>true</code> if this fragment is optional.
   */
  public boolean isOptional();
  
}
