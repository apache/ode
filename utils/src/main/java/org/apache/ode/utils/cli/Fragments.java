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

/**
 * <p>
 * Represents a commandline format as an ordered list of
 * {@link org.apache.ode.utils.cli.CommandlineFragment}s that are applied to an array of
 * arguments.
 * </p>
 */
public class Fragments {
  
  private CommandlineFragment[] _cl;
  private CommandlineSyntaxException _reason;
  
  /**
   * <p>
   * Create a new instance.
   * </p>
   * @param frags the {@link CommandlineFragment}s that make up the commandline
   */
  public Fragments(CommandlineFragment[] frags) {
    _cl = frags;
  }
  
  /**
   * <p>
   * Reset the constituent fragments to their initial states.
   * </p>
   */
  public void resetFragments() {
    for (int i=0; i < _cl.length; ++i) {
      _cl[i].reset();
    }
  }
  
  /**
   * <p>
   * Used after the {@link #matches(String[])} method, this returns the fragments
   * together with the settings that they have absorbed from the arguments.  The
   * fragments are returned in order of application, which is not necessarily the
   * order that the user would see the relevant entries on the commandline.
   * </p>
   * @return the array of fragments
   * @see #getFragmentsInUserOrder()
   */
  public CommandlineFragment[] getFragments() {
    return _cl;
  }
  
  /**
   * <p>
   * For use in formatting commandline help, this method orders the constituent
   * fragments in a way that would make sense to a user.  Otherwise, this method is
   * identical to {@link #getFragments()}.
   * </p>
   * @return the array of fragments in the order that makes sense to the user
   * @see #getFragments()
   */
  public CommandlineFragment[] getFragmentsInUserOrder() {
    if (_cl == null || _cl.length == 0) {
      return _cl;
    }
    CommandlineFragment[] cf = new CommandlineFragment[_cl.length];
    int lasts = 0;
    for (int i=0; i< _cl.length; ++i) {
      if (_cl[i] instanceof LastArgument) {
        ++lasts;
        cf[_cl.length - lasts] = _cl[i];
      } else {
        cf[i-lasts] = _cl[i];
      }
    }
    return cf;
  }
  
  /**
   * <p>
   * Used after the {@link #matches(String[])} method, this returns the reason, if
   * any, that one of the {@link CommandlineFragment}s failed to parse the set of
   * arguments.
   * </p>
   * @return
   */
  public CommandlineSyntaxException getReason() {
    return _reason;
  }
  
  /**
   * <p>
   * Apply the {@link CommandlineFragment}s to the supplied arguments (after calling
   * reset on the components).
   * </p>
   * @param s the arguments, e.g., as passed to a <code>main(...)</code>
   * @return <code>true</code> if the {@link CommandlineFragment}s succeeded in
   * consuming the arguments.
   */
  public boolean matches(String[] s) {
    resetFragments();
    try {
      java.util.List<String> l = new ArrayList<String>();
      for (int i=0; i < s.length; ++i) {
        l.add(s[i]);
      }
      for (int i=0; i < _cl.length;++i) {
        l = _cl[i].consume(l);
      }
      if (l.size() != 0) {
        StringBuffer sb = new StringBuffer();
        for (Iterator it = l.iterator(); it.hasNext();) {
          if (sb.length() != 0) {
            sb.append(' ');
          }
          sb.append((String) it.next());          
        }
        _reason = new CommandlineSyntaxException(
            "Not sure what to do with the extra items: " + sb.toString());
        return false;
      }
      return true;
    } catch (CommandlineSyntaxException cse) {
      _reason = cse;
      resetFragments();
      return false;
    }
  }
  
}
