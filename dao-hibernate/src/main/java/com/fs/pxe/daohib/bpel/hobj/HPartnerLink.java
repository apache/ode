package com.fs.pxe.daohib.bpel.hobj;

import com.fs.pxe.daohib.hobj.HLargeData;
import com.fs.pxe.daohib.hobj.HObject;

/**
 * The HEndpointReference can either be attached to a scope (when it's specific
 * to a scope instance, for example because it has been assigned during the
 * instance execution) or to a process definition (general endpoint configuration).
 * @hibernate.class table="BPEL_PLINK_VAL"
 */
public class HPartnerLink extends HObject {

  private String _linkName;
  private String _partnerRole;
  private String _myrole;
  private String _svcName;
  private HLargeData _myEPR;
  private HLargeData _partnerEPR;

  private HScope _scope;
  private HProcess _process;
  private int _modelId;

  public HPartnerLink() {
    super();
  }

  /**
   * @hibernate.property column="PARTNER_LINK" length="100" not-null="true"
   */
  public String getLinkName() {
    return _linkName;
  }

  public void setLinkName(String linkName) {
    _linkName = linkName;
  }

  /**
   * @hibernate.property column="PARTNERROLE" length="100" 
   */
  public String getPartnerRole() {
    return _partnerRole;
  }

  public void setPartnerRole(String partnerRoleName) {
    _partnerRole = partnerRoleName;
  }

  /**
   * @hibernate.many-to-one column="MYROLE_EPR" cascade="delete"
   */
  public HLargeData getMyEPR() {
    return _myEPR;
  }

  public void setMyEPR(HLargeData data) {
    _myEPR = data;
  }
  
  /**
   * @hibernate.many-to-one column="PARTNERROLE_EPR" cascade="delete"
   */
  public HLargeData getPartnerEPR() {
    return _partnerEPR;
  }

  public void setPartnerEPR(HLargeData data) {
    _partnerEPR = data;
  } 

  /**
   * @hibernate.many-to-one column="PROCESS"
   */
  public HProcess getProcess() {
    return _process;
  }

  public void setProcess(HProcess process) {
    _process = process;
  }

  /**
   * @hibernate.many-to-one column="SCOPE"
   */
  public HScope getScope() {
    return _scope;
  }

  public void setScope(HScope scope) {
    _scope = scope;
  }

  public void setServiceName(String svcName) {
    _svcName = svcName;
  }

  /**
   * @hibernate.property column="SVCNAME"
   */
  public String getServiceName() {
    return _svcName;
  }

  /**
   * @hibernate.property column="MYROLE" length="100"
   * @return
   */
  public String getMyRole() {
    return _myrole;
  }
  
  public void setMyRole(String myrole) {
    _myrole = myrole;
  }

  /**
   * @hibernate.property column="MODELID"
   */
  public int getModelId() {
    return _modelId;
  }
  
  public void setModelId(int modelId) {
    _modelId = modelId;
  }
}
