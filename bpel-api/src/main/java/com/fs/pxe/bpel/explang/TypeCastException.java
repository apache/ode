/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.explang;

/**
 * A {@link EvaluationException} indicating a type conversion error.
 */
public class TypeCastException extends EvaluationException {
  public static final short TYPE_STRING = 0;
  public static final short TYPE_DATE = 1;
  public static final short TYPE_DURATION = 2;
  public static final short TYPE_NODELIST = 3;
  public static final short TYPE_BOOLEAN = 4;
  public static final short TYPE_NODE = 5;
  public static final short TYPE_NUMBER = 6;

  private short _type;
  private String _val;

  public TypeCastException(short type, String val) {
    super("Type conversion error from: " + val,null);
    _type = type;
    _val = val;
  }

  public short getToType() {
    return _type;
  }

  public String getVal() {
    return _val;
  }

}
