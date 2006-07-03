package com.fs.pxe.daohib.bpel;

import com.fs.pxe.daohib.bpel.hobj.HProcessProperty;
import com.fs.pxe.daohib.SessionManager;
import com.fs.pxe.bpel.dao.ProcessPropertyDAO;

/**
 * Hibernate-based {@link ProcessPropertyDAO} implementation.
 */
public class ProcessPropertyDAOImpl extends HibernateDao implements ProcessPropertyDAO {

  private HProcessProperty _processProperty;

  public ProcessPropertyDAOImpl(SessionManager sessionManager, HProcessProperty processProperty) {
    super(sessionManager, processProperty);
    _processProperty = processProperty;
  }

  public String getName() {
    return _processProperty.getName();
  }

  public String getSimpleContent() {
    return _processProperty.getSimpleContent();
  }

  public String getNamespace() {
    return _processProperty.getNamespace();
  }

  public String getMixedContent() {
    return _processProperty.getMixedContent();
  }
}
