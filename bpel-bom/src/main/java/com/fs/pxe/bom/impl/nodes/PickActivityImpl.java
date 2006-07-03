/*
* File:      $RCSfile$
* Copyright: (C) 1999-2005 FiveSight Technologies Inc.
*
*/
package com.fs.pxe.bom.impl.nodes;

import com.fs.pxe.bom.api.Activity;
import com.fs.pxe.bom.api.OnAlarm;
import com.fs.pxe.bom.api.OnMessage;
import com.fs.pxe.bom.api.PickActivity;
import com.fs.utils.NSContext;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * BPEL object model representation of a <code>&lt;pick&gt;</code> activity.
 */
public class PickActivityImpl extends ActivityImpl implements PickActivity {
  private static final long serialVersionUID = -1L;

  private boolean _createInstance = false;

  private HashSet<OnMessage> _onMessages = new HashSet<OnMessage>();
  private HashSet<OnAlarm> _onAlarms = new HashSet<OnAlarm>();

  /**
   * Constructor.
   *
   * @param nsContext namespace context
   */
  public PickActivityImpl(NSContext nsContext) {
    super(nsContext);
  }

  public PickActivityImpl() {
    super();
  }


  public boolean getCreateInstance() {
    return _createInstance;
  }

  public void setCreateInstance(boolean createInstance) {
    _createInstance = createInstance;
  }

  public boolean isCreateInstance() {
    return _createInstance;
  }

  /**
   * @see Activity#getType()
   */
  public String getType() {
    return "pick";
  }

  public void addOnMessage(OnMessage onMsg) {
    ((OnMessageImpl) onMsg).setDeclaredIn(this);
    _onMessages.add(onMsg);
  }

  public Set <OnMessage> getOnMessages() {
    return Collections.unmodifiableSet(_onMessages);
  }

  public void addOnAlarm(OnAlarm onAlarm) {
    ((OnAlarmImpl) onAlarm).setDeclaredIn(this);
    _onAlarms.add(onAlarm);
  }

  public Set<OnAlarm> getOnAlarms() {
    return Collections.unmodifiableSet(_onAlarms);
  }

}
