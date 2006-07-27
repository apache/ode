/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.jacob.examples.cell;

import com.fs.jacob.Abstraction;
import com.fs.jacob.Val;


/**
 * Cell process template Java representation. This class is equivalent to the
 * following process calculus expression: <code> Cell(self, val) = self ? [
 * read(r) = { Cell(self, val) | r ! val(val) } & write(newVal) = {
 * Cell(self, newVal) } ] </code>
 */
public class CELL_<T> extends Abstraction {
  private CellChannel _self;
  private T _val;

  public CELL_(CellChannel self, T val) {
    _self = self;
    _val = val;
  }

  public void self() {
    // INSTANTIATION{Cell(self,val)}
    // ==> self ? [ read(r)={...} & write(newVal)={...} ]
    object(new CellML(_self) {
        public void read(Val r) {
          // COMMUNICATION{x & [read... & ... ] | x ! read}
          // ==> Cell(self, val) ...
          instance(new CELL_<T>(_self, _val));

          // ... | r ! val(val)
          r.val(_val);

          // Note: sequential Java above translates to parallel proc calc expression!
        }

        @SuppressWarnings("unchecked")
        public void write(Object newVal) {
          // COMMUNICATION{x & [... & write...]
          // ==> Cell(self, newVal)
          instance(new CELL_(_self, newVal));
        }
      });
  }

  /**
   * DOCUMENTME
   *
   * @return DOCUMENTME
   */
  public String toString() {
    return "CellProcess[self=" + _self + ", val=" + _val + "]";
  }
}
