package org.apache.ode.bpel.evtproc;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.apache.ode.bpel.engine.BpelEventListener;
import org.apache.ode.bpel.evt.ActivityEnabledEvent;
import org.apache.ode.bpel.evt.ActivityEvent;
import org.apache.ode.bpel.evt.ActivityExecEndEvent;
import org.apache.ode.bpel.evt.ActivityExecStartEvent;
import org.apache.ode.bpel.evt.BpelEvent;
import org.apache.ode.bpel.pmapi.*;

/**
 * Class used to generate {@link org.apache.ode.bpel.pmapi.istate.InstanceDocument}
 * objects.
 */
public class ActivityStateDocumentBuilder implements BpelEventListener {
  
  private HashMap<Long, ActivityInfoDocument> _activities = 
    new HashMap<Long,ActivityInfoDocument>();

  /** 
   * Keep a list of the activity info objects, so we can return
   * them in the order of creation.
   */
  private ArrayList<ActivityInfoDocument> _activitiesOrdered =
    new ArrayList<ActivityInfoDocument>();

  private boolean _removeCompleted = false;
  private Long _scope;
  
  
	public ActivityStateDocumentBuilder() {
	}
    
  public List<ActivityInfoDocument> getActivities() {
    return _activitiesOrdered;
  }
  public boolean isRemoveCompleted() {
    return _removeCompleted;
  }

  public void setRemoveCompleted(boolean removeCompleted) {
    _removeCompleted = removeCompleted;
  }

  public Long getScope() {
    return _scope;
  }

  public void setScope(Long scope) {
    _scope = scope;
  }

  public void onEvent(BpelEvent be) {
    if (be instanceof ActivityEvent) {
      final ActivityEvent event = (ActivityEvent)be;
      ActivityInfoDocument actinf = lookup(event);
      assert actinf != null;
      if (event instanceof ActivityEnabledEvent) {
        actinf.getActivityInfo().setStatus(TActivityStatus.ENABLED);
        Calendar dtEnabled = Calendar.getInstance();
        dtEnabled.setTime(event.getTimestamp());
        actinf.getActivityInfo().setDtEnabled(dtEnabled);
        _activities.put(event.getActivityId(),actinf);
      } if (event instanceof ActivityExecStartEvent) {
        actinf.getActivityInfo().setStatus(TActivityStatus.STARTED);
        Calendar dtStarted = Calendar.getInstance();
        dtStarted.setTime(event.getTimestamp());
        actinf.getActivityInfo().setDtStarted(dtStarted);
      } else if (event instanceof ActivityExecEndEvent) {
        actinf.getActivityInfo().setStatus(TActivityStatus.COMPLETED);
        Calendar dtComp = Calendar.getInstance();
        dtComp.setTime(event.getTimestamp());
        actinf.getActivityInfo().setDtCompleted(dtComp);
        completed(actinf);
      }
    }

	}

  private void completed(ActivityInfoDocument ainf) {
    if (_removeCompleted) {
      _activitiesOrdered.remove(ainf);
      _activities.values().remove(ainf);
    }
  }

  private ActivityInfoDocument lookup(ActivityEvent event) {
    ActivityInfoDocument ainf = _activities.get(event.getActivityId());
    if (ainf == null) {
      ainf = ActivityInfoDocument.Factory.newInstance();
      fill(ainf.addNewActivityInfo(),event);
      ainf.getActivityInfo().setStatus(TActivityStatus.ENABLED);
      _activities.put(event.getActivityId(),ainf);
      _activitiesOrdered.add(ainf);
    }
    return ainf;
  }

  /**
   * Fill the common activity info from an event.
   * @param info 
   * @param event
   */
  private void fill(TActivityInfo info, ActivityEvent event) {
    info.setName(event.getActivityName());
    info.setType(event.getActivityType());
    info.setAiid(""+event.getActivityId());
    info.setScope(TScopeRef.Factory.newInstance());
    info.getScope().setModelId("" + event.getScopeDeclarationId());
    info.getScope().setName(event.getScopeName());
    info.getScope().setSiid("" + event.getScopeId());
  }
}

