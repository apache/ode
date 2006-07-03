/*
 * File: $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 * 
 */
package com.fs.pxe.bpel.evt;

/**
 * Base class for all activity events. Note that each activity occurs in some
 * scope, hence this class extends {@link ScopeEvent}.
 */
public abstract class ActivityEvent extends ScopeEvent {
  private String _activityName;
  private String _activityType;
  
  /** Activity declaration id. */
  private int _declarationId;
  
  /** Activity Id, unique for each activity <em>instance</em>. */
  private long _aid;

  public ActivityEvent() {
    super();
  }

  /**
   * Gets activity name
   * 
   * @return name of activity
   */
  public String getActivityName() {
    return _activityName;
  }

  /**
   * Activity type e.g. switch, terminate, invoke
   * 
   * @return
   */
  public String getActivityType() {
    return _activityType;
  }

  public void setActivityName(String activityName) {
    _activityName = activityName;
  }

  public void setActivityType(String activityType) {
    _activityType = activityType;
  }

  public void setActivityDeclarationId(int declarationId) {
    _declarationId = declarationId;
  }

  public int getActivityDeclarationId() {
    return _declarationId;
  }

  public void setActivityId(long id) {
    _aid = id;
  }
  
  public long getActivityId() {
    return _aid;
  }

}
