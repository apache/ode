/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.w3c.dom.Element;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.Serializable;


/**
 * Class for wrapping DOM elements to support transparent serialization.
 * Serialization is based on writing out the XML text of the element.
 */
public final class SerializableElement implements Serializable {
  private static final long serialVersionUID = -1L;
  private static final Log __log = LogFactory.getLog(SerializableElement.class);

  private transient Element e;

  /**
   * Constructor, give the element to serialize as the argument.
   *
   * @param e element needing serialization
   */
  public SerializableElement(Element e) {
    this.e = e;
  }

  /**
   * Get the contained element.
   *
   * @return contained element
   */
  public Element getElement() {
    return e;
  }

  /**
   * De-serialization routines.
   *
   * @see Serializable
   */
  private void readObject(java.io.ObjectInputStream in)
                   throws IOException, ClassNotFoundException {
    try {
      e = DOMUtils.stringToDOM((String)in.readObject());
    } catch (SAXException e) {
      __log.error("De-serialization eror", e);
      throw new IOException(e.getMessage());
    }
  }

  /**
   * Serialization routines.
   *
   * @see Serializable
   */
  private void writeObject(java.io.ObjectOutputStream out)
                    throws IOException {
    out.writeObject(DOMUtils.domToString(e));
  }
}
