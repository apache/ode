package org.apache.ode.daohib.bpel.hobj;

import org.apache.ode.daohib.hobj.HLargeData;
import org.apache.ode.daohib.hobj.HObject;

/**
 * Persistent representation of a fault.
 * @hibernate.class table="BPEL_FAULT"
 */
public class HFaultData extends HObject {

  private String _name;
  private String _explanation;
  private HLargeData _data;
  private int _lineNo;
  private int _activityId;

  /**
   * @hibernate.property column="FAULTNAME"
   */
	public String getName() {
		return _name;
	}

  public void setName(String name) {
		_name = name;
	}

  /**
   * @hibernate.many-to-one column="LDATA_ID" cascade="delete"
   */
  public HLargeData getData() {
    return _data;
  }

  public void setData(HLargeData data) {
    _data = data;
  }


  /**
   * @hibernate.property column="EXPLANATION"
   */
  public String getExplanation() {
    return _explanation;
  }

  public void setExplanation(String explanation) {
    _explanation = explanation;
  }

  /**
   * @hibernate.property column="LINENO"
   */
  public int getLineNo() {
    return _lineNo;
  }

  public void setLineNo(int lineNo) {
    _lineNo = lineNo;
  }

  /**
   * @hibernate.property column="AID"
   */
  public int getActivityId() {
    return _activityId;
  }

  public void setActivityId(int activityId) {
    _activityId = activityId;
  }
}
