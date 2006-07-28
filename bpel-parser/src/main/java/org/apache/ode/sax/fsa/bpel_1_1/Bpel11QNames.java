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

import javax.xml.namespace.QName;

public interface Bpel11QNames {
  
  public static final QName PROCESS = new Bpel11QName("process");
  
  public static final QName SOURCE = new Bpel11QName("source");
  public static final QName TARGET = new Bpel11QName("target");
  
  public static final QName PARTNERLINKS = new Bpel11QName("partnerLinks");
  public static final QName PARTNERLINK = new Bpel11QName("partnerLink");
  
  public static final QName PARTNERS = new Bpel11QName("partners");
  public static final QName PARTNER = new Bpel11QName("partner");
  
  public static final QName VARIABLES = new Bpel11QName("variables");
  public static final QName VARIABLE = new Bpel11QName("variable");
  
  public static final QName CORRELATIONSETS = new Bpel11QName("correlationSets");
  public static final QName CORRELATIONSET = new Bpel11QName("correlationSet");
  
  public static final QName FAULTHANDLERS = new Bpel11QName("faultHandlers");
  public static final QName CATCH = new Bpel11QName("catch");
  public static final QName CATCHALL = new Bpel11QName("catchAll");
  
  public static final QName COMPENSATIONHANDLER = new Bpel11QName("compensationHandler");
  
  public static final QName COMPENSATE = new Bpel11QName("compensate");
  
  public static final QName EVENTHANDLERS = new Bpel11QName("eventHandlers");
  public static final QName ONMESSAGE = new Bpel11QName("onMessage");
  public static final QName ONALARM = new Bpel11QName("onAlarm");
  
  public static final QName CORRELATIONS = new Bpel11QName("correlations");
  public static final QName CORRELATION = new Bpel11QName("correlation");
  
  public static final QName EMPTY = new Bpel11QName("empty");
  public static final QName INVOKE = new Bpel11QName("invoke");
  public static final QName RECEIVE = new Bpel11QName("receive");
  public static final QName REPLY = new Bpel11QName("reply");
  public static final QName ASSIGN = new Bpel11QName("assign");
  public static final QName COPY = new Bpel11QName("copy");
  public static final QName FROM = new Bpel11QName("from");
  public static final QName TO = new Bpel11QName("to");
  public static final QName WAIT = new Bpel11QName("wait");
  public static final QName THROW = new Bpel11QName("throw");
  public static final QName TERMINATE = new Bpel11QName("terminate");
  
  public static final QName FLOW = new Bpel11QName("flow");
  public static final QName LINKS = new Bpel11QName("links");
  public static final QName LINK = new Bpel11QName("link");
  
  public static final QName SWITCH = new Bpel11QName("switch");
  public static final QName CASE = new Bpel11QName("case");
  public static final QName OTHERWISE = new Bpel11QName("otherwise");
  
  public static final QName WHILE = new Bpel11QName("while");
  public static final QName SEQUENCE = new Bpel11QName("sequence");
  public static final QName PICK = new Bpel11QName("pick");
  public static final QName SCOPE = new Bpel11QName("scope");
}
