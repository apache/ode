/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.evt;

import com.fs.utils.ArrayUtils;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Date;

/**
 * Base interface for all bpel events.
 */
public abstract class BpelEvent implements Serializable {

  private Date _timestamp = new Date();
  private int _lineNo = -1;

  public int getLineNo() {
    return _lineNo;
  }

  public void setLineNo(int lineNo) {
    _lineNo = lineNo;
  }

  public Date getTimestamp() {
    return _timestamp;
  }

  public void setTimestamp(Date tstamp) {
    _timestamp = tstamp;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder("\n" + eventName(this) + ":");

    Method[] methods = getClass().getMethods();
    for (Method method : methods) {
      if (method.getName().startsWith("get")
              && method.getParameterTypes().length == 0) {
        try {
          String field = method.getName().substring(3);
          Object value = method.invoke(this, ArrayUtils.EMPTY_OBJECT_ARRAY);
          if (value == null) {
            continue;
          }
          sb.append("\n\t")
                  .append(field)
                  .append(" = ")
                  .append(value == null ? "null" : value.toString());
        } catch (Exception e) {
          // ignore
        }
      }
    }
    return sb.toString();
  }

  public static String eventName(BpelEvent event){
    String name = event.getClass().getName();
    return name.substring(name.lastIndexOf('.') + 1);
  }

  public enum TYPE {
    dataHandling(1), activityLifecycle(2), scopeHandling(4), instanceLifecycle(8), correlation(16);

    public int bitset = 0;
    TYPE(int bitset) {
      this.bitset = bitset;
    }
  }

  public abstract TYPE getType();

}
