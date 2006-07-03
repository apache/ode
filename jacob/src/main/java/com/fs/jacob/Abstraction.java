/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */

package com.fs.jacob;

import com.fs.utils.ArrayUtils;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Base class for process abstractions. An abstraction is a parameterized
 * process template, whose instantiation is termed a <em>concretion</em>.
 * Abstractions may define a set of bound channel names or other parameters
 * which are resolved at the time of the concretion. For example the
 * process term abstraction of a memory cell:
 * <code>Cell(s,v) := s ? { read(...) = ... & write(...) = ... }</code>
 * would be represented by the following Java class:
 * <code>
 * <pre>
 * public class Cell extends Abstraction {
 *   private CellChannel s;
 *   private Object v;
 *   public Cell(CellChannel s, Object v) {
 *     this.s = s;
 *     this.v = v;
 *   }
 *   public void self() {
 *     object(new CellML(s) { read(...) {...}
 *                            write(...) {...} } );
 *   }
 * }
 * </pre>
 * </code>
 * An example of the Java expression representing the concretion of this abstraction
 * would look like:
 * <code>
 * <pre>
 *   .
 *   .
 *   // (new c) Cell(c,v)
 *   Integer v = Integer.valueOf(0);
 *   CellChannel c = (CellChannel)newChannel(CellChanell.class);
 *   instance(new Cell(c, v));
 *   .
 *   .
 * </pre>
 * </code>
 *
 * @author Maciej Szefler <a href="mailto:mbs@fivesight.com" />
 */
public abstract class Abstraction extends JavaClosure {
  private static final Log __log = LogFactory.getLog(Abstraction.class);

  private static final Set<Method> IMPLEMENTED_METHODS;
  static {
    try {
      IMPLEMENTED_METHODS = Collections.singleton(Abstraction.class.getMethod("self", ArrayUtils.EMPTY_CLASS_ARRAY));
    } catch (NoSuchMethodException e) {
      throw new AssertionError(e);
    }
  }

  public Set<Method> getImplementedMethods() {
    return IMPLEMENTED_METHODS;
  }

  /**
   * <p>
   * Peform the template reduction, i.e. do whatever it is that the
   * templetized process does. This method may do some combination of in-line
   * Java, and JACOB operations.
   * </p>
   * 
   * <p>
   * <em>Note that JACOB operations are performed in parallel, so the
   * sequencing of JACOB operations is irrelevant</em>
   * </p>
   */
  public abstract void self();

  /**
   * Pretty print.
   * @see Object#toString
   */
  public String toString() {
    StringBuffer buf = new StringBuffer(getClassName());
    buf.append("(...)");

    return buf.toString();
  }

  /**
   * @see JavaClosure#log
   */
  protected Log log() {
    return __log;
  }
}
