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
package org.apache.ode.soap.mapping;

import javax.xml.namespace.QName;

public abstract class Soap11Constants {
  public static final String NS_SOAP11 = "http://schemas.xmlsoap.org/soap/envelope/";
  public static final QName QNAME_ENVELOPE = new QName(NS_SOAP11, "Envelope", "soapenv");
  public static final QName QNAME_HEADER = new QName(NS_SOAP11, "Header", "soapenv");
  public static final QName QNAME_BODY = new QName(NS_SOAP11, "Body", "soapenv");
  public static final QName QNAME_FAULT = new QName(NS_SOAP11, "Fault", "soapenv");

  private Soap11Constants() {
  }
}
