package org.apache.ode.bpel.memdao;

import org.apache.ode.bpel.dao.FaultDAO;
import org.apache.ode.utils.QNameUtils;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;

/**
 * In-memory implementation of {@link FaultDAO}.
 */
public class FaultDaoImpl extends DaoBaseImpl implements FaultDAO {

  private String _name;
  private String _explanation;
  private Element _data;
  private int _lineNo;
  private int _activityId;

  public FaultDaoImpl(String name, String explanation, Element data, int lineNo, int activityId) {
    _name = name;
    _explanation = explanation;
    _data = data;
    _lineNo = lineNo;
    _activityId = activityId;
  }

  public QName getName() {
    return QNameUtils.toQName(_name);
  }

  public void setName(QName name) {
    _name = QNameUtils.fromQName(name);
  }

  public String getExplanation() {
    return _explanation;
  }

  public void setExplanation(String explanation) {
    _explanation = explanation;
  }

  public Element getData() {
    return _data;
  }

  public void setData(Element data) {
    _data = data;
  }

  public int getLineNo() {
    return _lineNo;
  }

  public void setLineNo(int lineNo) {
    _lineNo = lineNo;
  }

  public int getActivityId() {
    return _activityId;
  }

  public void setActivityId(int activityId) {
    _activityId = activityId;
  }

}
