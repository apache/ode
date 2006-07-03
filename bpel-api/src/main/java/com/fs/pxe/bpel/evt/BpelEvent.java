/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.evt;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Date;

import com.fs.utils.ArrayUtils;

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
     for(int i = 0; i < methods.length; ++i) {
       if (methods[i].getName().startsWith("get")
           && methods[i].getParameterTypes().length == 0) {
         try {
           String field = methods[i].getName().substring(3);
           Object value = methods[i].invoke(this, ArrayUtils.EMPTY_OBJECT_ARRAY);
           if(value == null) {
             continue;
           }
           sb.append("\n\t")
             .append(field)
             .append(" = ")
             .append( value == null ? "null" : value.toString());
         }catch(Exception e){
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

}
