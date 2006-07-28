/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.ode.sax.fsa.bpel_1_1;

import org.apache.ode.bom.api.Correlation;
import org.apache.ode.bom.api.JoinFailureSuppressor;
import org.apache.ode.bom.api.Process;
import org.apache.ode.sax.fsa.AbstractState;
import org.apache.ode.sax.fsa.ParseContext;
import org.apache.ode.sax.evt.XmlAttributes;
import org.apache.ode.sax.evt.attspec.FilterSpec;
import org.apache.ode.sax.evt.attspec.XmlAttributeSpec;

abstract class BaseBpelState extends AbstractState {

  public static final int START_STATE = -1;
  
  public static final int BPEL11_PROCESS = 1000;
  public static final int BPEL11_PARTNERLINKS = 1001;
  public static final int BPEL11_PARTNERLINK = 1002;
  public static final int BPEL11_PARTNERS = 1003;
  public static final int BPEL11_PARTNER = 1004;
  public static final int BPEL11_VARIABLES = 1005;
  public static final int BPEL11_VARIABLE = 1006;
  public static final int BPEL11_CORRELATIONSETS = 1007;
  public static final int BPEL11_CORRELATIONSET = 1008;
  public static final int BPEL11_FAULTHANDLERS = 1009;
  public static final int BPEL11_CATCH = 1010;
  public static final int BPEL11_CATCHALL = 1011;
  public static final int BPEL11_COMPENSATIONHANDLER = 1012;
  public static final int BPEL11_EVENTHANDLERS = 1013;
  public static final int BPEL11_ONMESSAGE = 1014;
  public static final int BPEL11_CORRELATIONS = 1015;
  public static final int BPEL11_ONALARM = 1016;
  public static final int BPEL11_EMPTY = 1017;
  public static final int BPEL11_INVOKE = 1018;
  public static final int BPEL11_RECEIVE = 1019;
  public static final int BPEL11_REPLY = 1020;
  public static final int BPEL11_ASSIGN = 1021;
  public static final int BPEL11_COPY = 1022;
  public static final int BPEL11_FROM = 1023;
  public static final int BPEL11_TO = 1024;
  public static final int BPEL11_WAIT = 1025;
  public static final int BPEL11_THROW = 1026;
  public static final int BPEL11_TERMINATE = 1027;
  public static final int BPEL11_FLOW = 1028;
  public static final int BPEL11_SWITCH = 1029;
  public static final int BPEL11_WHILE = 1030;
  public static final int BPEL11_SEQUENCE = 1031;
  public static final int BPEL11_PICK = 1032;
  public static final int BPEL11_SCOPE = 1033;
  public static final int BPEL11_CASE = 1034;
  public static final int BPEL11_COMPENSATE = 1035;
  public static final int BPEL11_SOURCE = 1036;
  public static final int BPEL11_TARGET = 1037;
  public static final int BPEL11_LINKS = 1038;
  public static final int BPEL11_LINK = 1039;
  public static final int BPEL11_CORRELATION = 1040;
  public static final int BPEL11_OTHERWISE = 1041;
  
  public static final int BPEL20_PROCESS = 2000;

  protected static final XmlAttributeSpec BPEL11_ACTIVITY_ATTS = new FilterSpec(
      new String[] {},
      new String[] {"name","joinCondition","suppressJoinFailure"});
  
 
  
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
