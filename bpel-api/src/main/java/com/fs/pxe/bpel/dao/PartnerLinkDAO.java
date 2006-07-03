package com.fs.pxe.bpel.dao;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

/**
 * Data access object representing the endpoint reference of a specific
 * partner link role (typically the partnerRole). An EPR has an implicit
 * value attributed by the engine (usually by using the WSDL service
 * definition but anyway that's the communication layer business). An
 * EndpointReferenceDAO only has its own value if the default has been
 * overriden (by assignment).
 */
public interface PartnerLinkDAO {

  /**
   * Get the model id of the partner link.
   * @return
   */
  public int getPartnerLinkModelId();
  
  public String getMyRoleName();

  public String getPartnerRoleName();
  
  public String getPartnerLinkName();

  /**
   * Get the service name associated with this partner link.
   * @return
   */
  public QName getMyRoleServiceName();

  public void setMyRoleServiceName(QName svcName);
  
  public Element getMyEPR();

  public void setMyEPR(Element val);

  public Element getPartnerEPR();

  public void setPartnerEPR(Element val);

}
