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

import java.util.StringTokenizer;


public class ConsoleFormatter {

  private static final int LINE_WIDTH = 76;

  // for help
  private static final int LEFT_MARGIN = 20;

  private static String spaces(int n) {
    StringBuffer sb = new StringBuffer(n);
    for (int i=0; i < n; ++i) {
      sb.append(' ');
    }
    return sb.toString();
  }

  // long command name is a pathological case, which we'll ignore.
  public static String formatUsage(String command, Fragments f) {
    StringBuffer sb = new StringBuffer(command + " ");
    String spaces = spaces(command.length() + 1);
    int currentLineLength = sb.length();
    CommandlineFragment[] ff = f.getFragmentsInUserOrder();
    for (int i=0; i< ff.length; ++i) {
      String u = ff[i].getUsage();
      if (ff[i].isOptional()) {
        u = "[" + u + "]";
      }
      if (currentLineLength != spaces.length()
          && currentLineLength + u.length() >= LINE_WIDTH)
      {
        sb.append("\n");
        sb.append(spaces);
        currentLineLength = spaces.length();
      }
      if (currentLineLength != spaces.length()) {
        u = ' ' + u;
      }
      sb.append(u);
      currentLineLength += u.length();
    }
    return sb.toString();
  }

  public static String formatHelp(Fragments f) {
    CommandlineFragment[] ff = f.getFragmentsInUserOrder();
    StringBuffer sb = new StringBuffer();
    String spaces = spaces(LEFT_MARGIN);
    for (int i=0; i < ff.length; ++i) {
      String u = ff[i].getUsage();
      if (u.length() > LEFT_MARGIN) {
        sb.append(u);
        sb.append('\n');
        sb.append(spaces);
      } else {
        sb.append(rightPadToWidth(u,LEFT_MARGIN));
      }
      sb.append(wrap(ff[i].getDescription(), LEFT_MARGIN));
    }
    return sb.toString();
  }

  public static String rightPadToWidth(String s, int w) {
    if (s.length() > w) {
      return s;
    } else {
      return s + spaces(w-s.length());
    }
  }

  public static String wrap(String s, int left) {
    String spaces = spaces(left);
    int cll = left;
    StringBuffer sb = new StringBuffer();
    for (StringTokenizer st = new StringTokenizer(s);st.hasMoreTokens();) {
      String t = st.nextToken();
      if (cll != left && cll + t.length() > LINE_WIDTH) {
        sb.append('\n');
        sb.append(spaces);
        cll = left;
      }
      if (cll != left) {
        t = ' ' + t;
      }
      sb.append(t);
      cll += t.length();
    }
    sb.append('\n');
    return sb.toString();
  }

  public static void printSynopsis(String command, String synposis, Fragments[] frags) {
    System.out.print(command + " - ");
    System.out.println(wrap(synposis, command.length() + 3));
    for (int i=0; i < frags.length; ++i) {
      System.out.println(formatUsage(command,frags[i]));
      System.out.println(formatHelp(frags[i]));
    }
  }
}
