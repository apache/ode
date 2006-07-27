package org.apache.ode.daohib.bpel.hobj;

import org.apache.ode.daohib.hobj.HObject;

import javax.xml.namespace.QName;

/**
 * Hibernate table-mapped class representing one valued property in
 * a correlation set.
 * @hibernate.class table="BPEL_CORRELATION_PROP"
 */
public class HCorrelationProperty extends HObject {
  private String _name;
  private String _namespace;
  private String _value;
  private HCorrelationSet _correlationSet;

  public HCorrelationProperty() {
    super();
  }

  public HCorrelationProperty(String name, String namespace, String value, HCorrelationSet correlationSet) {
    super();
    _name = name;
    _namespace = namespace;
    _value = value;
    _correlationSet = correlationSet;
  }

  public HCorrelationProperty(QName qname, String value, HCorrelationSet correlationSet) {
    super();
    _name = qname.getLocalPart();
    _namespace = qname.getNamespaceURI();
    _value = value;
    _correlationSet = correlationSet;
  }

  /**
   * @hibernate.property column="NAME"
   */
  public String getName() {
    return _name;
  }

  public void setName(String name) {
    _name = name;
  }

  /**
   * @hibernate.property column="NAMESPACE"
   */
  public String getNamespace() {
    return _namespace;
  }

  public void setNamespace(String namespace) {
    _namespace = namespace;
  }

  /**
   * @hibernate.property column="VALUE"
   */
  public String getValue() {
    return _value;
  }

  public void setValue(String value) {
    _value = value;
  }

  /**
   * @hibernate.many-to-one column="CORR_SET_ID"
   */
  public HCorrelationSet getCorrelationSet() {
    return _correlationSet;
  }

  public void setCorrelationSet(HCorrelationSet correlationSet) {
    _correlationSet = correlationSet;
  }

  public QName getQName() {
    return new QName(getNamespace(), getName());
  }
}
