/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.utils;

import java.io.IOException;
import java.net.ServerSocket;


/**
 * An inter-process synchronization mechanism. This class uses a well known
 * TCP port to synchronize multiple processes. The method of operation is
 * simple: by opening a port, a process prevents other processes on the local
 * machine from opening the same port.
 */
public class ProcessMutex {
  private int port;
  private ServerSocket ss = null;

  /**
   * Constructor.
   *
   * @param port synchronization TCP port
   */
  public ProcessMutex(int port) {
    this.port = port;
  }

  /**
   * Acquire the lock. Will throw InterruptedException after timeout (15
   * seconds).
   *
   * @throws InterruptedException in case of timeout
   * @throws IllegalStateException in case lock is already acquired
   */
  public void lock()
            throws InterruptedException {
    synchronized (this) {
      if (ss != null) {
        throw new IllegalStateException("ProcessMutex: Bad mutex state exception.");
      }

      long startTime = System.currentTimeMillis();

      while ((startTime + 15000) > System.currentTimeMillis()) {
        try {
          ss = new ServerSocket(port);

          break;
        } catch (IOException ioe) {
          Thread.sleep(2);
        }
      }

      if (ss == null) {
        throw new InterruptedException("ProcessMutex: lock() timed out!");
      }
    }
  }

  /**
   * Release the lock
   *
   * @throws IllegalStateException DOCUMENTME
   */
  public void unlock() {
    synchronized (this) {
      if (ss == null) {
        throw new IllegalStateException("ProcessMutex: Bad mutex state exception.");
      }

      try {
        ss.close();
      } catch (IOException ioe) {
        throw new IllegalStateException("ProcessMutex: Error closing socket.");
      } finally {
        ss = null;
        this.notify();
      }
    }
  }
}
