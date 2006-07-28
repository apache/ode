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
package com.fs.jacob.soup;

/**
 * Base class for items we find in the {@link Soup}.
 * <p>Created on Feb 17, 2004 at 3:44:24 PM.</p>
 * 
 * @author Maciej Szefler <a href="mailto:mbs@fivesight.com">mbs</a>
 */
public class SoupObject {

  /** A unique idefntifer for this object in the soup (should only be set by soup). */
  private Object _id;

  /** A human-readable description of the object. */
  private String _description;

  public String getDescription() {
    return _description;
  }

  public void setDescription(String description) {
    if (_description != null)
      throw new IllegalStateException("Description already set for " + this);
    _description = description;
  }

  public void setId(Object id) {
    if (_id != null)
      throw new IllegalStateException("Object id already set for " + this);
    _id = id;
  }

  public Object getId() {
    return _id;
  }

  public boolean equals(Object obj) {
    if (_id == null || ((SoupObject)obj)._id == null)
      return this==obj;
    return ((SoupObject)obj)._id.equals(_id);
  }
}
