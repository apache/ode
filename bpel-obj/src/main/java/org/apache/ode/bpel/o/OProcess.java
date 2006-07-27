/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.o;

import org.apache.ode.utils.stl.CollectionsX;
import org.apache.ode.utils.stl.MemberOfFunction;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.*;

import javax.wsdl.Operation;

/**
 * Compiled BPEL process representation.
 */
public class OProcess extends OBase {
  
  static final long serialVersionUID = -1L  ;

  /** BPEL version. */
  public final String version;

  /** Various constants that are needed at runtime. */
  public OConstants constants;

  /** Universally Unique Identifier */
  public String uuid;

  /** Namespace of the process. */
  public String targetNamespace;

  /** Name of the process. */
  public String processName;

  /** ProcessImpl-level scope. */
  public OScope procesScope;

  /** All partner links in the process. */
  public final Set<OPartnerLink> allPartnerLinks = new HashSet<OPartnerLink>();

  public final List<OProperty> properties = new ArrayList<OProperty>();


  /** Date process was compiled. */
  public Date compileDate;

  int _childIdCounter = 0;

  List<OBase> _children = new ArrayList<OBase>();
  
  public final HashSet<OExpressionLanguage> expressionLanguages = new HashSet<OExpressionLanguage>();

  public final HashMap<QName, OMessageVarType> messageTypes = new HashMap<QName, OMessageVarType>();

  public final HashMap<QName, OElementVarType> elementTypes = new HashMap<QName, OElementVarType>();

  public final HashMap<QName, OXsdTypeVarType> xsdTypes = new HashMap<QName, OXsdTypeVarType>();

  public final HashMap<URI, OXslSheet> xslSheets = new HashMap<URI, OXslSheet>();

  public OProcess(String bpelVersion) {
    super(null);
    this.version = bpelVersion;
  }

  public OBase getChild(final int id) {
    return CollectionsX.find_if(_children, new MemberOfFunction<OBase>() {
      public boolean isMember(OBase o) {
        return o.getId() == id;
      }
    });
  }

  public List<OBase> getChildren() {
    return _children;
  }

  public OScope getScope(String scopeName) {
    throw new UnsupportedOperationException();
  }


  public Set<OPartnerLink> getAllPartnerLinks() {
    return Collections.unmodifiableSet(allPartnerLinks);
  }

  public OPartnerLink getPartnerLink(String name) {
    for (OPartnerLink partnerLink : allPartnerLinks) {
      if (partnerLink.getName().equals(name)) return partnerLink;
    }
    return null;
  }

  public String getName() {
    return processName;
  }

  public Collection getExpressionLanguages() {
    throw new UnsupportedOperationException(); // TODO: implement me!
  }

  public List<String> getCorrelators() {
    // MOVED from ProcessSchemaGenerator
    List<String> correlators = new ArrayList<String>();

    for (OPartnerLink plink : getAllPartnerLinks()) {
      if (plink.hasMyRole()) {
        for (Iterator opI = plink.myRolePortType.getOperations().iterator(); opI.hasNext();) {
          Operation op = (Operation)opI.next();
          correlators.add(plink.getId() + "." + op.getName());
        }
      }
    }
    
    return correlators;
  }
  
  public static class OProperty extends OBase {
    
    static final long serialVersionUID = -1L  ;
    public final List<OPropertyAlias> aliases = new ArrayList<OPropertyAlias>();
    public QName name;

    public OProperty(OProcess process) { super(process); }

    public OPropertyAlias getAlias(OVarType messageType) {
      for (OPropertyAlias aliase : aliases)
        if (aliase.varType.equals(messageType))
          return aliase;
      return null;
    }

    public String toString() {
      return "{OProperty " + name + "}";
    }
  }

  public static class OPropertyAlias extends OBase {
    
    static final long serialVersionUID = -1L  ;

    public OVarType varType;

    /** For BPEL 1.1 */
    public OMessageVarType.Part part;

    public OExpression location;

    public OPropertyAlias(OProcess owner) {super(owner); }

    public String toString() {
      return "{OPropertyAlias " + getDescription() +  "}";
    }

    public String getDescription() {
      StringBuffer buf = new StringBuffer(varType.toString());
      buf.append('[');
      buf.append(part != null ? part.name : "");
      if (location != null) {
        buf.append("][");
        buf.append(location.toString());
      }
      buf.append(']');
      return buf.toString();
    }
    
  }

  public QName getQName() {
    return new QName(targetNamespace, processName);
  }

}
