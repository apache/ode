/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.common;

import java.io.Serializable;
import java.text.ParseException;
import java.util.*;

import com.fs.utils.ISO8601DateParser;

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
