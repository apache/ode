/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.bapi;

/**
 * Exception indicating a failure in domain message processing. In addition to
 * containing the cause of the failure, objects of this class also indicate
 * the required action to deal with the failure. These indications are used
 * by the binding implementation to appropriately respond to the {@link
 * DomainNode}'s inability to deal with a a given {@link
 * com.fs.pxe.sfwk.bapi.msgs.DomainMsg} in the {@link
 * DomainNode#onMessage(com.fs.pxe.sfwk.bapi.msgs.DomainMsg)} method.
 * Depending on the type of failure, the binding may be asked to consume the
 * message as if nothing went wrong, to rollback the transaction and retry,
 * to rollback and not retry, etc...
 */
public class DomainMsgProcessingException extends Exception {
  /** Commit the transaction, consuming the event. */
  public static final short ACTION_COMMIT_AND_CONSUME = 0;

  /** Commit the transaction, but do not consume the event. */
  public static final short ACTION_COMMIT_AND_RETRY_NOW = 1;

  /** Rollback the transaction and immediately try it again. */
  public static final short ACTION_ROLLBACK_AND_RETRY_NOW = 1;

  /** Rollback the transaction and try it again sometime later. */
  public static final short ACTION_ROLLBACK_AND_RETRY_LATER = 2;

  /** Rollback the transaction but consume the event. */
  public static final short ACTION_ROLLBACK_AND_CONSUME_EVENT = 3;
  private short _action;

  public DomainMsgProcessingException(short action, String msg, Throwable cause) {
    super(msg, cause);
    _action = action;
  }

  public DomainMsgProcessingException(short action, String msg) {
    this(action, msg, null);
  }

  /**
   * Get the requested action for dealing with the failure.
   *
   * @return one of the <code>ACTION_XXXX</code> constants
   */
  public short getAction() {
    return _action;
  }
}
