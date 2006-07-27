/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.sax.fsa.bpel_2_0;

import javax.xml.namespace.QName;

interface Bpel20QNames {
  
  public static final QName PROCESS = new Bpel20QName("process");
  
  public static final QName IMPORT = new Bpel20QName("import");
  public static final QName SERVICE_REF = new Bpel20QName("service-ref");
  
  public static final QName SOURCES = new Bpel20QName("sources");
  public static final QName SOURCE = new Bpel20QName("source");
  public static final QName TARGETS = new Bpel20QName("targets");
  public static final QName TARGET = new Bpel20QName("target");
  
  public static final QName PARTNERLINKS = new Bpel20QName("partnerLinks");
  public static final QName PARTNERLINK = new Bpel20QName("partnerLink");
  
  public static final QName VARIABLES = new Bpel20QName("variables");
  public static final QName VARIABLE = new Bpel20QName("variable");
  
  public static final QName CORRELATIONSETS = new Bpel20QName("correlationSets");
  public static final QName CORRELATIONSET = new Bpel20QName("correlationSet");
  
  public static final QName FAULTHANDLERS = new Bpel20QName("faultHandlers");
  public static final QName CATCH = new Bpel20QName("catch");
  public static final QName CATCHALL = new Bpel20QName("catchAll");
  
  public static final QName COMPENSATIONHANDLER = new Bpel20QName("compensationHandler");
  public static final QName TERMINATIONHANDLER = new Bpel20QName("terminationHandler");
  
  public static final QName COMPENSATE = new Bpel20QName("compensate");
  
  public static final QName EVENTHANDLERS = new Bpel20QName("eventHandlers");
  public static final QName ONEVENT = new Bpel20QName("onEvent");
  public static final QName ONMESSAGE = new Bpel20QName("onMessage");
  public static final QName ONALARM = new Bpel20QName("onAlarm");
  
  public static final QName CORRELATIONS = new Bpel20QName("correlations");
  public static final QName CORRELATION = new Bpel20QName("correlation");
  
  public static final QName EMPTY = new Bpel20QName("empty");
  public static final QName INVOKE = new Bpel20QName("invoke");
  public static final QName RECEIVE = new Bpel20QName("receive");
  public static final QName REPLY = new Bpel20QName("reply");
  public static final QName ASSIGN = new Bpel20QName("assign");
  public static final QName COPY = new Bpel20QName("copy");
  public static final QName FROM = new Bpel20QName("from");
  public static final QName TO = new Bpel20QName("to");
  public static final QName WAIT = new Bpel20QName("wait");
  public static final QName THROW = new Bpel20QName("throw");
  public static final QName RETHROW = new Bpel20QName("rethrow");
  public static final QName EXIT = new Bpel20QName("exit");
  
  public static final QName FLOW = new Bpel20QName("flow");
  public static final QName LINKS = new Bpel20QName("links");
  public static final QName LINK = new Bpel20QName("link");
  
  public static final QName SWITCH = new Bpel20QName("switch");
  public static final QName CASE = new Bpel20QName("case");
  public static final QName OTHERWISE = new Bpel20QName("otherwise");
  
  public static final QName IF = new Bpel20QName("if");
  public static final QName THEN = new Bpel20QName("then");
  public static final QName ELSEIF = new Bpel20QName("elseif");
  public static final QName ELSE = new Bpel20QName("else");
  
  public static final QName WHILE = new Bpel20QName("while");
  public static final QName SEQUENCE = new Bpel20QName("sequence");
  public static final QName PICK = new Bpel20QName("pick");
  public static final QName SCOPE = new Bpel20QName("scope");
  
  public static final QName TRANSITION_CONDITION = new Bpel20QName("transitionCondition");
  public static final QName FOR = new Bpel20QName("for");
  public static final QName UNTIL = new Bpel20QName("until");
  public static final QName REPEAT_EVERY = new Bpel20QName("repeatEvery");
  public static final QName JOIN_CONDITION = new Bpel20QName("joinCondition");
  public static final QName CONDITION = new Bpel20QName("condition");
  
  public static final QName QUERY = new Bpel20QName("query");
  public static final QName EXPRESSION = new Bpel20QName("expression");

  public static final QName FOREACH = new Bpel20QName("forEach");
  public static final QName ITERATOR = new Bpel20QName("iterator");
  public static final QName START_COUNTER_VALUE = new Bpel20QName("startCounterValue");
  public static final QName FINAL_COUNTER_VALUE = new Bpel20QName("finalCounterValue");
  public static final QName COMPLETION_CONDITION = new Bpel20QName("completionCondition");
  public static final QName BRANCHES = new Bpel20QName("branches");

}
