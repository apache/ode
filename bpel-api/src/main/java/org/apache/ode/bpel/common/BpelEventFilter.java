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

import java.io.Serializable;
import java.text.ParseException;
import java.util.*;

import org.apache.ode.utils.ISO8601DateParser;

/**
 * Holds a filter that will get interpreted when listing BPEL events.
 */
public class BpelEventFilter extends Filter<BpelEventFilter.Criteria> implements
    Serializable {

  private static final long serialVersionUID = 9999;

  private String _type;

  private List<Restriction<Date>> _tstampRestrictions = new ArrayList<Restriction<Date>>();

  public int limit;

  /**
   * Criteria that is available through this filter.
   */
  enum Criteria {
    /** Filter on event type. */
    TYPE {
      void process(BpelEventFilter f, Restriction<String> r) {
        f._type = r.value;
      }
    },

    /** Filter on event timestamp. */
    TIMESTAMP {
      void process(BpelEventFilter f, Restriction<String> r) {
        try {
          f._tstampRestrictions.add(new Restriction<Date>(r.originalKey, r.op,
              ISO8601DateParser.parse(r.value)));
        } catch (ParseException e) {
          String errmsg = __msgs.msgISODateParseErr(TIMESTAMP.name(), r.value);
          throw new IllegalArgumentException(errmsg, e);
        }
      }
    };

    abstract void process(BpelEventFilter f, Restriction<String> rest);
  }

  /**
   * Initializes properly the InstanceFilter attributes by pre-parsing the
   * filter and orderKeys strings and setting the limit. A limit inferior than
   * or equal to 0 is ignored.
   *
   * @param filter
   */
  public BpelEventFilter(String filter, int limit) {
    init(filter);
    this.limit = limit;
  }

  public String getTypeFilter() {
    return _type;
  }

  public List<Restriction<Date>> getTimestampFilter() {
    return _tstampRestrictions;
  }

  @Override
  protected Criteria parseKey(String keyVal) {
    return Criteria.valueOf(keyVal);
  }

  @Override
  protected Criteria[] getFilterKeys() {
    return Criteria.values();
  }

  @Override
  protected void process(Criteria key, Restriction<String> rest) {
    key.process(this, rest);
  }

}
