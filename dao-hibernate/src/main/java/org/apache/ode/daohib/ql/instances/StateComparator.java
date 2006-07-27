package org.apache.ode.daohib.ql.instances;

import java.util.Comparator;

import org.apache.commons.lang.ArrayUtils;

import org.apache.ode.bpel.common.ProcessState;
import org.apache.ode.daohib.bpel.hobj.HProcessInstance;

class StateComparator implements Comparator<HProcessInstance> {
  
  private static final short[] order = {
    //"active" status
    ProcessState.STATE_ACTIVE,
    ProcessState.STATE_NEW,
    ProcessState.STATE_READY,
    //"completed"
    ProcessState.STATE_COMPLETED_OK,
    //"error"
    //TODO Create status for error
    200,//noState
    //"failed"
    ProcessState.STATE_COMPLETED_WITH_FAULT,
    //"suspended"
    ProcessState.STATE_SUSPENDED,
    //"terminated"
    ProcessState.STATE_TERMINATED};
  
  private final int multiplier;
  
  public final static StateComparator ASC = new StateComparator(true);
  public final static StateComparator DESC = new StateComparator(false);
  
  protected StateComparator(boolean asc) {
    multiplier = asc?1:-1;
  }
  /**
   * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
   */
  public int compare(HProcessInstance o1, HProcessInstance o2) {
    return multiplier * (ArrayUtils.indexOf(order, o1.getState()) - ArrayUtils.indexOf(order, o2.getState()));
  }

}
