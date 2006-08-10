package org.apache.ode.bpel.memdao;

import org.apache.ode.bpel.dao.ProcessPropertyDAO;

/**
 * In memory implementation of the {@see ProcessPropertyDAO} interface.
 */
public class ProcessPropertyDAOImpl extends DaoBaseImpl implements ProcessPropertyDAO {

  private String name;
  private String namespace;
  private String simpleContent;
  private String mixedContent;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getNamespace() {
    return namespace;
  }

  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  public String getSimpleContent() {
    return simpleContent;
  }

  public void setSimpleContent(String simpleContent) {
    this.simpleContent = simpleContent;
  }

  public String getMixedContent() {
    return mixedContent;
  }

  public void setMixedContent(String mixedContent) {
    this.mixedContent = mixedContent;
  }
}
