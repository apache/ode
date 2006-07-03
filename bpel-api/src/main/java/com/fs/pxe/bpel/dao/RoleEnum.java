/*
 * File: $RCSfile$
 * 
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 * 
 */
package com.fs.pxe.bpel.dao;

import java.util.ArrayList;
import java.util.List;

/**
 * Partner-link role enumeration; one of {@link #MY_ROLE} or {@link
 * #PARTNER_ROLE}.
 */
public class RoleEnum {

  /** My Role */
  public static final RoleEnum MY_ROLE = new RoleEnum("MY_ROLE");

  /** Partner Role */
  public static final RoleEnum PARTNER_ROLE = new RoleEnum("PARTNER_ROLE");

  private static final List<RoleEnum> ENUMS = new ArrayList<RoleEnum>();
  private short _id;
  private transient String _name;

  private RoleEnum(String name) {
    _id = (short)ENUMS.size();
    _name = name;
    ENUMS.add(this);
  }

  /**
   * @see Object#equals(java.lang.Object)
   */
  public boolean equals(Object o) {
    return ((RoleEnum)o)._id == _id;
  }

  /**
   * DOCUMENTME
   * 
   * @param code
   *          DOCUMENTME
   * 
   * @return DOCUMENTME
   */
  public static RoleEnum fromCode(short code) {
    return ENUMS.get(code);
  }

  /**
   * @see Object#hashCode()
   */
  public int hashCode() {
    return _id;
  }

  /**
   * DOCUMENTME
   * 
   * @return DOCUMENTME
   */
  public short toCode() {
    return _id;
  }

  /**
   * @see Object#toString()
   */
  public String toString() {
    return "RoleEnum." + _name;
  }

}
