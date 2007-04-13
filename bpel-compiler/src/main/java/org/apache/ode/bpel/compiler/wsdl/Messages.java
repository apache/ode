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

package org.apache.ode.bpel.compiler.wsdl;

import org.apache.ode.utils.msg.MessageBundle;

import javax.xml.namespace.QName;

/**
 * Message interface for wsdl bpel extension
 */
public class Messages extends MessageBundle {

  /**
   * BPEL extension error: invalid namespace prefix \"{0}\".
   */
  public String msgInvalidNamespacePrefix(String prefix) {
    return this.format("BPEL extension error: invalid namespace prefix \"{0}\".", prefix);
  }

  /**
   * The string \"{0}\" is not a properly formatted QName.
   */
  public String msgMalformedQName(String str) {
    return this.format("The string \"{0}\" is not a properly formatted QName.", str);
  }

  /**
   * The formatted error message The partnerLinkType {0} does not define any
   * roles.
   */
  public String msgMissingRoleForPartnerLinkType(String name) {
    return this.format("The formatted error message The partnerLinkType {0} does"
        + " not define any roles.", name);
  }

  /**
   * Format an error message about the <code>&lt;X&gt;</code> element
   * requiring the <code>Y</code> attribute.
   * 
   * @param element
   *          the <code>QName</code> of the element
   * @param attribute
   *          the name of the attribute
   * @return the formatted error message
   * 
   * The {1} attribute is required by the {0} element.
   */
  public String msgElementRequiresAttr(String element, String attribute) {
    return this.format("The {1} attribute is required by the {0} element.",
        element, attribute);
  }

  /**
   * Format an error message about the <code>&lt;X&gt;</code> element having
   * content when it should be empty.
   * 
   * @param element
   *          the <code>QName</code> of the element
   * @return the formatted error message
   * 
   * The schema for {0} prohibits child elements or non-whitespace characters as
   * content.
   */
  public String msgElementMustBeEmpty(String element) {
    return this.format("The schema for {0} prohibits child elements or non-whitespace"
        + " characters as content.", element);
  }

  /**
   * Format an error message about a named <code>&lt;role&gt;</code> already
   * being declared within a <code>&lt;partnerLink&gt;</code>.
   * 
   * @param linkName
   *          the <code>QName</code> of the <code>&lt;partnerLink&gt;</code>
   * @param roleName
   *          the name of the <code>&lt;role&gt;</code>
   * @return the formatted message
   * 
   * The role {1} is already defined for the partnerLink {2}.
   */
  public String msgRoleAlreadyDefined(QName linkName, String roleName) {
    return this.format("The role {1} is already defined for the partnerLink {2}.",
        linkName, roleName);
  }

  /**
   * Format an error message that at most <code>n</code> things of type
   * <code>x</code> may be defined with each <code>y</code>.
   * 
   * @param n
   *          the maximum number of things.
   * @param x
   *          the things.
   * @param y
   *          the thing that contains the things.
   * @return the formatted message
   * 
   * No more than {0} {1} item(s) may be defined within one {2}.
   */
  public String msgNoMoreThanNumberOfElements(int n, String x, String y) {
    return this.format("No more than {0} {1} item(s) may be defined within one {2}.",
        n, x, y);
  }

  /**
   * Format an error message about a child element being required.
   * 
   * @param element
   *          the <code>QName</code> of the parent element
   * @param child
   *          the <code>QName</code> of the child element
   * @return the formatted message.
   * 
   * The element {0} requires a child element with name {1}.
   */
  public String msgElementRequiresChild(String element, String child) {
    return this.format("The element {0} requires a child element with name {1}.",
        element, child);
  }

  /**
   * Format an error message about a <code>&lt;role&gt;</code> referring to a
   * <code>portType</code> that can't be dereferenced.
   * 
   * @param roleName
   *          the name of the <code>&lt;role&gt;</code>
   * @param portType
   *          the <code>QName</code> of the <code>portType</code>
   * @return the formatted message
   * 
   * The referenced portType {1} for role {0} is not defined in this definition
   * or an import.
   */
  public String msgNoSuchPortTypeForRole(String roleName, String portType) {
    return this.format("The referenced portType {1} for role {0} is not defined in"
        + " this definition or an import.", roleName, portType);
  }

  /**
   * Format a message about a <code>&lt;propertyAlias&gt;</code> referring to
   * a non-existent WSDL message type.
   * 
   * @param messageType
   *          the non-existent WSDL message type name
   * @return the formatted message
   * 
   * A propertyAlias refers to the non-existent WSDL message type {0}.
   */
  public String msgNoSuchMessageTypeForPropertyAlias(String messageType) {
    return this.format("A propertyAlias refers to the non-existent WSDL message type {0}.",
        messageType);
  }

  /**
   * Format an error message about an element not being permissable as the root
   * element in a WSDL document.
   * 
   * @param qname
   *          the stringified <code>QName</code> of the element
   * @return the formatted message
   * 
   * The element {0} is not permitted as the root element in a WSDL definition.
   */
  public String msgCannotBeDocumentRootElement(String qname) {
    return this.format("The element {0} is not permitted as the root element"
        + " in a WSDL definition.", qname);
  }

  /**
   * Format an error message about a WSDL extensibility element requiring to be
   * a first level child of the definition element.
   * 
   * @param qname
   *          the stringified <code>QName</code> of the element
   * @return the formatted message.
   * 
   * The element {0} must be a child of the WSDL definitions element.
   */
  public String msgMustBeChildOfDef(String qname) {
    return this.format("The element {0} must be a child of the WSDL definitions element.",
        qname);
  }

  /**
   * Format an error message about an extensibility element occurring out of
   * order with respect to other WSDL elements.
   * 
   * @param qname
   *          the stringified <code>QName</code> of the element
   * @return the formatted message
   * 
   * The WSDL extensibility element {0} must occur after all core WSDL
   * declarations in the same WSDL document.
   */
  public String msgExtensibilityElementsMustBeLast(String qname) {
    return this.format("The WSDL extensibility element {0} must occur after all core WSDL"
        + " declarations in the same WSDL document.", qname);
  }

  /**
   * Format a message about a <code>&lt;propertyAlias&gt;</code> referring to
   * a non-existent part of a a WSDL message type.
   * 
   * @param message
   *          the stringified <code>QName</code> of the message
   * @param part
   *          the name of the part
   * @return the formatted message
   * 
   * A propertyAlias refers to the non-existent part {1} on the WSDL message
   * type {0}.
   */
  public String msgNoSuchPartForPropertyAlias(String message, String part) {
    return this.format("A propertyAlias refers to the non-existent part {1} on the"
        + " WSDL message type {0}.");
  }

  /**
   * Format an error message about a <code>thing</code> being
   * (inappropriately) redefined.
   * 
   * @param thing
   *          the thing, e.g., a message, a port type, etc.
   * @param name
   *          the name of the thing
   * @param origin
   *          where it was first defined
   * @param redef
   *          where it was redefined
   * @return the formatted message
   * 
   * The {0} {1} defined in {3} was already defined in {2}.
   */
  public String msgAlreadyDefinedIn(String thing, String name, String origin, String redef) {
    return this.format("The {0} {1} defined in {3} was already defined in {2}.",
        thing, name, origin, redef);
  }

  /**
   * Format an error message about a reference to a
   * <code>&lt;property&gt;</code> that does not exist.
   * 
   * @param name
   *          the stringified <code>QName</code> of the property
   * @param documentBaseURI
   *          the URI of the WSDL that contains the reference.
   * @return the formatted message
   * 
   * A property alias in {1} refers to the property {0}, which is not defined.
   */
  public String msgNoSuchProperty(String name, String documentBaseURI) {
    return this.format("A property alias in {1} refers to the property {0},"
        + " which is not defined.", name, documentBaseURI);
  }

}
