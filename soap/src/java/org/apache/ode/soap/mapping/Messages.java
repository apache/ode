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

import org.apache.ode.utils.msg.MessageBundle;

/**
 * Message bundle for the <code>org.apache.ode.soap.mapping</code> package.
 */
public class Messages extends MessageBundle {

  /**   The operation did not define an input message. */
  String msgNoInputMessage() {
    return format("The operation did not define an input message.");
  }

  /**  The operation did not define an input binding. */
  String msgNoInputBinding() {
    return format("The operation did not define an input binding. ");
    
  }

  /**   The operation did not define an output message. */
  String msgNoOutputMessage() {
    return format("The operation did not define an output message.");
    
  }

  /**  The operation did not define an output binding. */
  String msgNoOutputBinding() {
    return format("The operation did not define an output binding.");
    
  }

  /**
   *  The port did not provide a binding.
   */
  String msgNoBindingForPort() {
    return format("The port did not provide a binding.");
    
  }

  /**
   *  The port did not provide the required SOAP binding.
   */
  String msgNoSoapBindingForPort() {    
    return format("The port did not provide the required SOAP binding.");
  }


  /**
   *  The operation did not provide the required SOAP binding.
   */
  String msgNoSoapBindingForOperation() {
    return format("The operation did not provide the required SOAP binding.");
    
  }

  /**
   * The port provided multiple SOAP bindings.
   */
  String msgMultipleSoapBindingsForPort() {
    return format("The port provided multiple SOAP bindings.");
    
  }

  /**
   *  The operation "{0}" does not match an operation on port type "{1}".
   */
  String msgNoOperationForBinding(String op, String portType) {
    return format("The operation \"{0}\" does not match an operation on port type \"{1}\".", op, portType);
    
  }

  /**
   *   The operation provided multiple SOAP bindings.
   */
  String msgMultipleSoapBindingsForOperation() {
    return format("The operation provided multiple SOAP bindings.");
    
  }

  /**
   *  The operation "{0}" does not have an associated SOAP binding.
   */
  String msgNoBindingForOperation(String uri, String operation) {
    return format("The operation \"{0}\" does not have an associated SOAP binding.",uri,operation);
    
  }

  /**
   *  The port "{0}" is unknown.
   */
  String msgPortNotKnown(String port) {
    return format("The port \"{0}\" is unknown.",port);
    
  }

  /**
   *  The part "{0}" is unknown.
   */
  String msgPartNotKnown(String partName) {
    return format("The part \"{0}\" is unknown.",partName);
    
  }

  /**  The fault "{0}" is invalid, it must have EXACTLY ONE part. */
  String msgFaultMustHaveExactlyOnePart(String faultName) {
    return format("The fault \"{0}\" is invalid, it must have EXACTLY ONE part. ",faultName);
    
  }


  /**
   *  A wsdl:binding in a description must either be an rpc-literal binding or a
   *           document-literal binding.
   */
  String msgBP_R2705() {
    return format("A wsdl:binding in a description must either be an rpc-literal binding or a" +
        " document-literal binding.");
    
  }

  /**
   *    A wsdl:binding in a description must use the value of "literal" for the use
   *            attribute in all soapbind:body, soapbind:fault, soapbind:header and soapbind:headerfault
   *            elements.
   */
  String msgBP_R2706() {
    return format("A wsdl:binding in a description must use the value of \"literal\" for the use attribute " +
        "in all soapbind:body, soapbind:fault, soapbind:header and soapbind:headerfault elements.");
    
  }

  /**
   *   An rpc-literal binding in a description must refer, in its soapbind:body element(s), only
   *            to wsdl:part element(s) that have been defined using the type attribute;  the part
   *            "{0}" was not defined using the element attribute.
   */
  String msgBP_R2203(String partName) {
    return format("An rpc-literal binding in a description must refer, in its soapbind:body element(s), only " +
        "to wsdl:part element(s) that have been defined using the type attribute;  the part \"{0}\" was not" +
        " defined using the element attribute.", partName);
    
  }

  /**
   *   A document-literal binding in a description must, in each of its soapbind:body element(s),
   *            have at most one part listed in the parts attribute, if the parts attribute is specified.
   */
  String msgBP_R2201() {
    return format("A document-literal binding in a description must, in each of its soapbind:body element(s), " +
        "one part listed in the parts attribute, if the parts attribute is specified.");
  }

  /**
   *   A document-literal binding in a description must refer, in each of its soapbind:body
   *           element(s), only to wsdl:part element(s) that have been defined using the element attribute;
   *           the part "{0}" was not defined using the element attribute.
   */
  String msgBP_R2204(String partName) {
    return format("A document-literal binding in a description must refer, in each of its soapbind:body" +
        "element(s), only to wsdl:part element(s) that have been defined using the element attribute;" +
        "the part \"{0}\" was not defined using the element attribute.",partName);
    
  }


  /** 
   * @todo Message..
   */
  String msgBP_R2706(String partName) {
    return todo();
  }

  /** 
   * @todo Message..
   */
  String msgBP_R2726(String partName) {
    return todo();
  }

  /** 
   * @todo Message..
   */
  String msgBP_R2716(String partName) {
    return todo();
  }

  /** 
   * @todo Message..
   */
  String msgBP_R2205(String partName) {
    return todo();
  }
    
  /** 
   * @todo Message..
   */
  String msgBP_R2721() {
    return todo();    
  }

  /** 
   * @todo Message..
   */
  String msgBP_R2754() {
    return todo();
  }

  /** 
   * @todo Message..
   */
  String msgBP_R2304() {
    return todo();
  }

  /** 
   *  Invalid wrapper element {2} for non-element part "{0}":
   *          wrapper element should have local name "{0}". 
   */
  String msgInvalidWrapperForNonElementPart(String partName, QName typeName, QName elName) {
    return format(" Invalid wrapper element {2} for non-element part \"{0}\":" +
        "wrapper element should have local name \"{0}\". ");
    
  }
}
