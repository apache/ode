/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.capi;

/**
 * Interface allowing an object to listen to compilation events.
 */
public interface CompileListener {

  /**
   * Callback invoked for each compilation message.
   * @param compilationMessage message event
   */
  void onCompilationMessage(CompilationMessage compilationMessage);
}
