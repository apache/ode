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

package org.apache.ode.utils.msg;

import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;

public abstract class MessageBundle extends ResourceBundle {

  public static <T extends MessageBundle> T getMessages(Class<T> bundleInterface) {
    return getMessages(bundleInterface, Locale.getDefault());
  }

  @SuppressWarnings("unchecked")
  public static <T extends MessageBundle> T getMessages(Class<T> bundleInterface, Locale locale) {
    // TODO maybe catch MissingResourceException here
    return (T)ResourceBundle.getBundle(bundleInterface.getName(), locale, bundleInterface.getClassLoader());
  }

  public MessageBundle() {
    super();
  }

  /**
   * Generate a "todo" message.
   * @return todo message string
   */
  protected String todo() {
    Exception ex = new Exception();
    ex.fillInStackTrace();
    return ex.getStackTrace()[1].getMethodName() + ": NO MESSAGE (TODO)";
  }

  protected Object handleGetObject(String key) {
    throw new UnsupportedOperationException();
  }

  public Enumeration<String> getKeys() {
    throw new UnsupportedOperationException();
  }

  protected String format(String message) {
    return message;
  }

  protected String format(String message, Object... args) {
    if (args == null || args.length == 0) {
      return message;
    }
    else {
      return new MessageFormat(message).format(args);
    }
  }

}
