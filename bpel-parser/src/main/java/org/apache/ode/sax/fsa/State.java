/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.sax.fsa;

import org.apache.ode.sax.evt.SaxEvent;
import org.apache.ode.sax.evt.StartElement;


/**
 * <p>
 * A &quot;state&quot; in the parsing of an XML document.  The lifecycle of an
 * instance is managed by an {@link org.apache.ode.sax.fsa.FSA} that is responsible for
 * creating, configuring, passing events, and calling lifecycle methods.
 * </p>
 */
public interface State {
  /**
   * Consume a SAX event.
   * @param se the encapsulated event
   * @throws ParseException if the event is invalid, e.g., an inappropriate element.
   */
  public void handleSaxEvent(SaxEvent se) throws ParseException;
  
  /**
   * <p>
   * Consume a completed child element.  Most likely, an instance will use the type
   * of the child (via {@link #getType()}) to determine how to handle it.
   * </p>  
   * @param pn the <code>State</code> that holds the child.
   * @throws ParseException if the child is invalid, e.g., not properly configured.
   */
  public void handleChildCompleted(State pn) throws ParseException;
  
  /**
   * <p>
   * Obtain the context for the current parse operation, e.g., to report errors or
   * warnings.  The {@link ParseContext} for an instance is set when it is created.
   * </p>
   * @return the current <code>ParseContext</code> instance.
   * @see StateFactory#newInstance(StartElement, ParseContext)
   */
  public ParseContext getParseContext();
  
  /**
   * <p>
   * Obtain a factory to create new instance of this state.
   * </p>
   * @return the factory instance.
   */
  public StateFactory getFactory();
  
  /**
   * <p>
   * Get the type of the <code>State</code> as an <code>int</code>.  This will
   * depend on the purpose of the given application, i.e., the type <code>1</code>
   * will be different from parser to parser.
   * </p>
   * @return the type key for the <code>State</code>
   */
  public int getType();
  
  /**
   * <p>
   * A finalization hook for cleaning up resources and finalizing the values of
   * properties.  The default implementation will do nothing.
   * </p>
   */
  public void done();
}
