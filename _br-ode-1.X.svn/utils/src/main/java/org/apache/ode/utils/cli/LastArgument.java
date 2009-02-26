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
