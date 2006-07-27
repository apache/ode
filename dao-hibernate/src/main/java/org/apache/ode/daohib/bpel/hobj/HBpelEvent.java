
package org.apache.ode.daohib.bpel.hobj;

import java.sql.Timestamp;

import org.apache.ode.daohib.hobj.HObject;
import org.apache.ode.daohib.hobj.HLargeData;

/**
 * Row representation of a BPEL event. 
 *
 * @hibernate.class table="BPEL_EVENT"
 */
public class HBpelEvent extends HObject {
  private Timestamp _tstamp;
  private String _type;
  private String _detail;

  private HProcess _process;
  private HProcessInstance _instance;
  private HLargeData _data;

  /** Scope identifier, possibly null. */
  private Long _scopeId;

  /**
   * @hibernate.many-to-one
   *  column="IID"
   */
  public HProcessInstance getInstance() {
    return _instance;
  }

  public void setInstance(HProcessInstance instance) {
    _instance = instance;
  }

  /**
   * @hibernate.many-to-one
   *  column="PID"
   */
  public HProcess getProcess() {
    return _process;
  }

  public void setProcess(HProcess process) {
    _process = process;
  }

  /**
   * @hibernate.property
   *    column="TSTAMP"
   */
  public Timestamp getTstamp() {
    return _tstamp;
  }

  public void setTstamp(Timestamp tstamp) {
    _tstamp = tstamp;
  }

  /**
   * @hibernate.property
   *    column="TYPE"
   */
  public String getType() {
    return _type;
  }

  public void setType(String type) {
    _type = type;
  }

  /**
   * TODO Check 32000 is enough for details
   */
  /**
   * @hibernate.property
   *  column="DETAIL"
   *  type="text"
   *  length="32000"
   */
  public String getDetail() {
    return _detail;
  }

  public void setDetail(String detail) {
    _detail = detail;
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
   * Get the scope identifier of the scope associated with this event. 
   * Note, that this is not implemented as a many-to-one relationship
   * because when scopes are deleted from the database we do not want 
   * their events to suffer the same fate. 
   * @hibernate.property
   *    column="SID"
   */
  public Long getScopeId() {
    return _scopeId;
  }

  public void setScopeId(Long scopeId) {
    _scopeId = scopeId;
  }

}
