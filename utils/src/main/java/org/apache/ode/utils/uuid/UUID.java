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

package org.apache.ode.utils.uuid;

import java.io.Serializable;

/**
 * Universally Unique Identifier.
 */
public class UUID implements Serializable {

	private static final long serialVersionUID = -2632846855777989452L;

	private static final UUIDGen UUIDGEN = new UUIDGen();

	private String _id;

	public UUID() {
		_id = prefix() + UUIDGEN.nextUUID();
	}

	protected UUID(String id) {
		if (!id.startsWith(prefix())) {
			throw new IllegalArgumentException("Invalid UUID");
		}

		_id = id;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		return (o.getClass() == getClass()) && ((UUID) o)._id.equals(_id);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return _id.hashCode();
	}

	/**
	 * String form of a uuid.
	 * 
	 * @param uuid
	 *            convert the <code>uuid</code> to a string
	 * @return string representation
	 */
	public static String toIdString(UUID uuid) {
		return uuid._id;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return _id;
	}

	protected String prefix() {
		return "";
	}
}
