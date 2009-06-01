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

package org.apache.ode.bpel.common;

import org.apache.ode.utils.CollectionUtils;
import org.apache.ode.utils.msg.MessageBundle;
import org.apache.ode.utils.stl.CollectionsX;
import org.apache.ode.utils.stl.UnaryFunction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base class for filters.
 */
public abstract class Filter<FKEY extends Enum> implements Serializable {
  private static final long serialVersionUID = 9999;
  
  
  /** Internationalization. */
  protected static final Messages __msgs = MessageBundle.getMessages(Messages.class);

  /**
   *  Pattern that matches anything like 'abcde <  fgh' or 'ijklm  =nop' using 
   *  supported comparators
   *  <p>
   *  The not-equal op, '<>' works only with pids.
   *  </p>
   */
  private static final Pattern __comparatorPattern = 
    Pattern.compile("([^=<> ]*) *(<>|<=|>=|<|>|=) *([^=<> ]*)");

  protected Map<FKEY, Restriction<String>> _criteria = new HashMap<FKEY,Restriction<String>>();

  public static class Restriction<V>{
    public final String originalKey;
    public final String op;
    public final V value;
    
    public Restriction(String originalKey, String op, V value) {
      this.originalKey=originalKey;
      this.op=op;
      this.value = value;
    }
  }

  public void init(String filter) {
    if (filter != null) {
      Matcher expressionMatcher = __comparatorPattern.matcher(filter);
      while (expressionMatcher.find()) {
        String filterKey = expressionMatcher.group(1);
        String op = expressionMatcher.group(2);
        String value = expressionMatcher.group(3);
        FKEY keyval;
        try {
          keyval = parseKey(filterKey.toUpperCase().replaceAll("-", "_"));
        } catch (Exception ex) {
          String errmsg = __msgs.msgUnrecognizedFilterKey(filterKey, getFilterKeysStr());
          throw new IllegalArgumentException(errmsg, ex);
        }
        
        Restriction<String> restriction = new Restriction<String>(filterKey,op,value);
        _criteria.put(keyval, restriction);
        
        process(keyval, restriction);
      }
    }

  }

  /**
   * Get the data part of an "op date" string.
   * @param ddf "op date" string
   * @return date component
   */
  public static String getDateWithoutOp(String ddf) {
    if (ddf != null) {
      if (ddf.startsWith("=")) {
        return ddf.substring(1, ddf.length());
      } else if (ddf.startsWith("<=")) {
        return ddf.substring(2, ddf.length());
      } else if (ddf.startsWith(">=")) {
        return ddf.substring(2, ddf.length());
      } else if (ddf.startsWith("<")) {
        return ddf.substring(1, ddf.length());
      } else if (ddf.startsWith(">")) {
        return ddf.substring(1, ddf.length());
      }
    }
    return null;
 
  }

  /**
   * Parse the string representation of a key into an
   * enumeration value. 
   * @param keyVal string representation
   * @return enumeration value
   */
  protected abstract FKEY parseKey(String keyVal);

  /**
   * Get the list of known (recognized) filter keys. 
   * @return recognized filter keys
   */
  protected abstract FKEY[] getFilterKeys();

  /**
   * Perform additional parsing/processing.
   * @param key filter key
   * @param rest restriction
   */
  protected abstract void process(FKEY key, Restriction<String> rest);

  private Collection<String> getFilterKeysStr() {
    return CollectionsX.transform(new ArrayList<String>(),
        CollectionUtils.makeCollection(ArrayList.class , getFilterKeys()),
        new UnaryFunction<FKEY,String>() {
          public String apply(FKEY x) {
            return x.name();
          }
        });
    
  }
}

