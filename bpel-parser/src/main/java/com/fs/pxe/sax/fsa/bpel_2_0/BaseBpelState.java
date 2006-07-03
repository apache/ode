/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sax.fsa.bpel_2_0;

import com.fs.pxe.bom.api.Correlation;
import com.fs.pxe.bom.api.JoinFailureSuppressor;
import com.fs.pxe.bom.api.Process;
import com.fs.pxe.sax.fsa.AbstractState;
import com.fs.pxe.sax.fsa.ParseContext;
import com.fs.sax.evt.XmlAttributes;
import com.fs.sax.evt.attspec.FilterSpec;
import com.fs.sax.evt.attspec.XmlAttributeSpec;

abstract class BaseBpelState extends AbstractState {
  
  static final int BPEL_PROCESS = 2000;
  static final int BPEL_PARTNERLINKS = 2001;
  static final int BPEL_PARTNERLINK = 2002;
  // Obsoleted via Issue 130.
  static final int BPEL_PARTNERS = 2003;
  static final int BPEL_PARTNER = 2004;
  static final int BPEL_VARIABLES = 2005;
  static final int BPEL_VARIABLE = 2006;
  static final int BPEL_CORRELATIONSETS = 2007;
  static final int BPEL_CORRELATIONSET = 2008;
  static final int BPEL_FAULTHANDLERS = 2009;
  static final int BPEL_CATCH = 2010;
  static final int BPEL_CATCHALL = 2011;
  static final int BPEL_COMPENSATIONHANDLER = 2012;
  static final int BPEL_EVENTHANDLERS = 2013;
  static final int BPEL_ONMESSAGE = 2014;
  static final int BPEL_CORRELATIONS = 2015;
  static final int BPEL_ONALARM = 2016;
  static final int BPEL_EMPTY = 2017;
  static final int BPEL_INVOKE = 2018;
  static final int BPEL_RECEIVE = 2019;
  static final int BPEL_REPLY = 2020;
  static final int BPEL_ASSIGN = 2021;
  static final int BPEL_COPY = 2022;
  static final int BPEL_FROM = 2023;
  static final int BPEL_TO = 2024;
  static final int BPEL_WAIT = 2025;
  static final int BPEL_THROW = 2026;
  static final int BPEL_EXIT = 2027;
  static final int BPEL_FLOW = 2028;
  static final int BPEL_SWITCH = 2029;
  static final int BPEL_WHILE = 2030;
  static final int BPEL_SEQUENCE = 2031;
  static final int BPEL_PICK = 2032;
  static final int BPEL_SCOPE = 2033;
  static final int BPEL_CASE = 2034;
  static final int BPEL_COMPENSATE = 2035;
  static final int BPEL_SOURCE = 2036;
  static final int BPEL_TARGET = 2037;
  static final int BPEL_LINKS = 2038;
  static final int BPEL_LINK = 2039;
  static final int BPEL_CORRELATION = 2040;
  static final int BPEL_OTHERWISE = 2041;
  static final int BPEL_EXPRESSION = 2042;
  static final int BPEL_RETHROW = 2043;
  static final int BPEL_FOR = 2044;
  static final int BPEL_UNTIL = 2045;
  // Obsoleted via Issue 103.
  // TODO: Remove.
  static final int BPEL_QUERY = 2046;
  static final int BPEL_SERVICE_REF = 2047;
  static final int BPEL_IMPORT = 2048;
  static final int BPEL_REPEATEVERY = 2049;
  static final int BPEL_SOURCES = 2050;
  static final int BPEL_TARGETS = 2051;
  static final int BPEL_IF = 2052;
  static final int BPEL_ELSEIF = 2053;
  static final int BPEL_ELSE = 2054;
  static final int BPEL_THEN = 2055;
  // Added via Issue 135.
  static final int BPEL_TERMINATIONHANDLER = 2056;
  static final int BPEL_ONEVENT = 2057;
  // Added via 103.
  static final int BPEL_LITERAL = 2058;
  // Added via 160.
  static final int BPEL_XMLVALIDATE = 2059;
  // Added via 112.
  static final int BPEL_TOPART = 2060;
  static final int BPEL_FROMPART = 2061;
  static final int BPEL_FOREACH = 2062;
  static final int BPEL_ITERATOR = 2063;
  static final int BPEL_START_COUNTER_VALUE = 2064;
  static final int BPEL_FINAL_COUNTER_VALUE = 2065;
  static final int BPEL_COMPLETION_CONDITION = 2066;
  static final int BPEL_BRANCHES = 2067;

  protected static final XmlAttributeSpec BPEL_ACTIVITY_ATTS = new FilterSpec(
      new String[] {},
      new String[] {"name","suppressJoinFailure"});

  protected BaseBpelState(ParseContext pc) {
    super(pc);
  }
  
  
  protected boolean checkYesNo(String val) {
    return val != null && val.equals("yes");
  }
 
  protected short getSuppressJoinFailure(XmlAttributes atts) {
    if (!atts.hasAtt("suppressJoinFailure")) {
      return JoinFailureSuppressor.SUPJOINFAILURE_NOTSET;
    } else {
      String sjf = atts.getValue("suppressJoinFailure");
      return checkYesNo(sjf)?Process.SUPJOINFAILURE_YES:Process.SUPJOINFAILURE_NO; 
    }    
  }
  
  protected short getInitiateYesNo(XmlAttributes atts) {
    return checkYesNo(atts.getValue("initiate"))?Correlation.INITIATE_YES:Correlation.INITIATE_NO;
  }
  
}
