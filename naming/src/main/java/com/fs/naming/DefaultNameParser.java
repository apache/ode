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
package com.fs.naming;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Properties;

import javax.naming.CompositeName;
import javax.naming.CompoundName;
import javax.naming.Name;
import javax.naming.NameParser;


public final class DefaultNameParser implements NameParser, Serializable {

  //~ Instance/static variables ...............................................

  private final Properties NAME_PROPS = new Properties();

  //~ Constructors ............................................................

  /**
   * Creates a new DefaultNameParser object.
   */
  public DefaultNameParser() {

    Enumeration en = System.getProperties().keys();

    while (en.hasMoreElements()) {

      String s = (String)en.nextElement();

      if (s.startsWith("jndi.syntax.")) {

        String value = System.getProperty(s);

        if (value != null && !value.equals(""))
          NAME_PROPS.put(s, value);
      }
    }
  }

  //~ Methods .................................................................

  /**
   * DOCUMENTME
   * 
   * @param parm1 DOCUMENTME
   * @return DOCUMENTME 
   * @throws javax.naming.NamingException DOCUMENTME
   */
  public Name parse(String parm1)
        throws javax.naming.NamingException {

    return (NAME_PROPS.size() > 0)
             ? (Name)new CompoundName(parm1, NAME_PROPS)
             : (Name)new CompositeName(parm1);
  }
}