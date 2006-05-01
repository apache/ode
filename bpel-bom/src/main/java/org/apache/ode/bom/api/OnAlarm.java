/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bom.api;

/**
 * Representation of alarm-based event handlers.
 */
public interface OnAlarm extends BpelObject {
  /**
   * The activity associated with the alarm.
   *
   * @return activity associated with alarm
   */
  Activity getActivity();

  void setActivity(Activity activity);

  /**
   * Set the duration of the alarm.
   *
   * @param for1 {@link Expression}
   */
  void setFor(Expression for1);

  /**
   * Get the duration of the alarm.
   *
   * @return duration of the alarm
   */
  Expression getFor();

  /**
   * Set the deadline when the alarm goes out of effect.
   *
   * @param until the deadline
   */
  void setUntil(Expression until);

  /**
   * Get the deadline when the alarm goes out of effect.
   *
   * @return deadline when alarm goes out of effect
   */
  Expression getUntil();
  
  /**
   * Set the repeatEvery (optional).
   * @param expr a duration expression that specifies the frequency of the repeat
   */
  void setRepeatEvery(Expression expr);
  
  /**
   * Get the repeatEvery (optional)
   * @return the duration expression that specifies the frequency
   */
  Expression getRepeatEvery();
  
  

}
