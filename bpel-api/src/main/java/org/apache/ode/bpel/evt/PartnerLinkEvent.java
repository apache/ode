package org.apache.ode.bpel.evt;

public abstract class PartnerLinkEvent extends ScopeEvent {
  private String _pLinkName;

  public PartnerLinkEvent() {
    super();
  }

  public PartnerLinkEvent(String pLinkName) {
    super();
    _pLinkName = pLinkName;
  }

  public String getpLinkName() {
    return _pLinkName;
  }

  public void setpLinkName(String pLinkName) {
    _pLinkName = pLinkName;
  }

  public TYPE getType() {
    return TYPE.dataHandling;
  }
}
