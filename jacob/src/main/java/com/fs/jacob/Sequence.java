/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.jacob;

public abstract class Sequence extends Abstraction {
  private int _size;
  private int _step;
  private SynchChannel _done;

  public Sequence(int size, SynchChannel done) {
    this(size, 0, done);
  }

  private Sequence(int size, int step, SynchChannel done) {
    _size = size;
    _step = step;
    _done = done;
  }

  /**
   * DOCUMENTME
   */
  public void self() {
    if (_step >= _size) {
      if (_done != null) {
        _done.ret();
      }
    } else {
      SynchChannel r = newChannel(SynchChannel.class);
      object(new SynchML(r) {
          public void ret() {
            ++_step;
            instance(Sequence.this);
          }
        });
      instance(reduce(_step, r));
    }
  }

  protected abstract Abstraction reduce(int n, SynchChannel r);
}
