/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.naming;

import java.util.EventListener;


/**
 * Listener interface for changes in a <code>BindingMap</code>  object.
 */
public interface BindingMapListener
  extends EventListener {

  //~ Methods .................................................................

  /**
   * Event method triggered when a entry in a binding map changes.
   * @param nameInNamespace DOCUMENTME
   */
  public void bindingChanged(String nameInNamespace);
}