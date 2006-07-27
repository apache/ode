/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
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