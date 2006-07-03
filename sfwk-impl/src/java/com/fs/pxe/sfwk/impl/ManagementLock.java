/*
 * File:      $Id: ManagementLock.java 491 2006-01-02 16:12:04Z holger $
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.impl;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A lock for managing access to the domain. The lock is of a shared-read/write
 * variety permitting shared access by "workers" and exclusive access by
 * "managers". This is used to ensure that only a single management operation
 * is executed at a time, and that no one is working while we are executing
 * a management operation. Management operations are given priority over workers.
 */
final class ManagementLock {

  private ReentrantReadWriteLock _lock = new ReentrantReadWriteLock();

  final void acquireWorkLock() {
    _lock.readLock().lock();
  }

  final void releaseWorkLock() {
    _lock.readLock().unlock();
  }

  final void acquireManagementLock() {
    _lock.writeLock().lock();
  }

  final void releaseManagementLock() {
    _lock.writeLock().unlock();
  }

}
