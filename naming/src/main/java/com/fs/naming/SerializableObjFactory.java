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
