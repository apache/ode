/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.naming;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Hashtable;

import javax.naming.*;
import javax.naming.spi.ObjectFactory;

public final class SerializableObjFactory implements ObjectFactory {

  public SerializableObjFactory() {
    super();
  }

  public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable environment)
      throws Exception {
    Reference ref = (Reference)obj;
    javax.naming.BinaryRefAddr ra = (BinaryRefAddr)ref.get(0);
    ByteArrayInputStream bas = new ByteArrayInputStream((byte[])ra.getContent());
    ObjectInputStream ois = new ObjectInputStream(bas);
    return ois.readObject();
  }

  public static Reference createSerializableRef(Object obj) throws NamingException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream(4096);
    try {
      ObjectOutputStream oos = new ObjectOutputStream(bos);
      oos.writeObject(obj);
      oos.close();
      Reference ref = new Reference(obj.getClass().getName(), SerializableObjFactory.class.getName(),
          null);
      ref.add(new BinaryRefAddr("serialized_object", bos.toByteArray()));
      return ref;
    }
    catch (Exception ex) {
      throw new NamingException(ex.getMessage());
    }
  }

}
