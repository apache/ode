package com.fs.pxe.bpel.evt;

public class PartnerLinkModificationEvent extends PartnerLinkEvent {
  private static final long serialVersionUID = 1L;
  public PartnerLinkModificationEvent() {
    super();
  }

  public PartnerLinkModificationEvent(String pLinkName) {
    super(pLinkName);
  }
}
