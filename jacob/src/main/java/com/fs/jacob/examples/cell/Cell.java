/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.jacob.examples.cell;

import com.fs.jacob.Val;

/**
 * Channel type for a cell. The channel allows reading of and setting the
 * values of a cell.
 * @jacob.kind
 */
public interface Cell  {

  /**
   * Read the value of the cell.
   * @param replyTo channel to which the value of the cell is sent
   */
  public void read(Val replyTo);

  /**
   * Write the value of the cell.
   * @param newVal new value of the cell
   */
  public void write(Object newVal);
}
