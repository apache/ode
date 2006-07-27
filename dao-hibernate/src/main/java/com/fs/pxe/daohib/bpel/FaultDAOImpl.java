package com.fs.pxe.daohib.bpel;

import com.fs.pxe.bpel.dao.FaultDAO;
import com.fs.pxe.daohib.SessionManager;
import com.fs.pxe.daohib.bpel.hobj.HFaultData;
import com.fs.utils.DOMUtils;
import com.fs.utils.QNameUtils;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import java.io.IOException;

/**
 * Hibernate based {@link FaultDAO} implementation
 */
public class FaultDAOImpl extends HibernateDao implements FaultDAO {

  HFaultData _self;

  public FaultDAOImpl(SessionManager sm, HFaultData fault) {
    super(sm, fault);
    _self = fault;
  }

  public QName getName() {
    return QNameUtils.toQName(_self.getName());
  }

  public Element getData() {
    if (_self.getData() == null) return null;
    try {
      return DOMUtils.stringToDOM(_self.getData().getText());
    } catch (SAXException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public String getExplanation() {
    return _self.getExplanation();
  }

  public int getLineNo() {
    return _self.getLineNo();
  }

  public int getActivityId() {
    return _self.getActivityId();
  }
}
