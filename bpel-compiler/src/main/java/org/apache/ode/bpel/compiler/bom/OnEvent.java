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
package org.apache.ode.bpel.compiler.bom;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;


/**
 * Representation of a message-driven event handler.
 */
public class OnEvent extends OnMessage {


  public OnEvent(Element el) {
        super(el);
    }

/**
   * @return the WSDL message type to be used for the incoming message body or
   * <code>null</code> if an element type is to be used instead.
   * @see #getElement()
   */
  public QName getMessageType() {
      return getNamespaceContext().derefQName(getAttribute("messageType"));
  }

  /**
   * @return the element type to be used for the incoming message body or
   * <code>null</code> if a WSDL message type is to be used instead.
   * @see #getMessageType()
   */
  public QName getElementType() {
      return getNamespaceContext().derefQName(getAttribute("element",null));
  }

}