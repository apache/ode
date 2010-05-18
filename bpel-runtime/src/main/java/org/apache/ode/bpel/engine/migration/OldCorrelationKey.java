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

package org.apache.ode.bpel.engine.migration;

import org.apache.ode.utils.CollectionUtils;

import java.util.List;
import java.util.ArrayList;
import java.io.Serializable;

public class OldCorrelationKey implements Serializable {

    private static final long serialVersionUID = 1L;

    /** CorrelationSet identifier. */
    private int _csetId;
    /** Key values. */
    private String _keyValues[];

    /**
     * Constructor.
     *
     * @param csetId
     *            correlation set identifier
     * @param keyValues
     *            correlation key values
     */
    public OldCorrelationKey(int csetId, String[] keyValues) {
        _csetId = csetId;
        _keyValues = keyValues;
    }

    public OldCorrelationKey(String canonicalForm) {
        int firstTilde = canonicalForm.indexOf('~');
        _csetId = Integer.parseInt(canonicalForm.substring(0, firstTilde == -1 ? canonicalForm.length() : firstTilde));

        if (firstTilde != -1) {
            List<String> keys = new ArrayList<String>();
            char chars[] = canonicalForm.toCharArray();
            StringBuffer work = new StringBuffer();
            for (int i = firstTilde + 1; i < chars.length; ++i) {
                boolean isLast = (i == chars.length - 1);
                if (chars[i] == '~' && !isLast && chars[i + 1] == '~') {
                    work.append(chars[i++]);
                } else if (chars[i] == '~') {
                    keys.add(work.toString());
                    work = new StringBuffer();
                } else {
                    work.append(chars[i]);
                }
            }
            keys.add(work.toString());
            _keyValues = new String[keys.size()];
            keys.toArray(_keyValues);
        } else {
            _keyValues = new String[0];
        }
    }

    /** Return the OCorrelation id for the correlation set */
    public int getCSetId() {
        return _csetId;
    }

    /** Return the values for the correlation set */
    public String[] getValues() {
        return _keyValues;
    }

    /**
     * Check if this key matches any member in a set of keys.
     *
     * @param keys
     *            set of keys to match against
     *
     * @return <code>true</code> if one of the keys in the set
     *         <code>equals(..)</code> this key, <code>false</code>
     *         otherwise
     */
    public boolean isMatch(OldCorrelationKey[] keys) {
        for (OldCorrelationKey key : keys)
            if (key.equals(this)) {
                return true;
            }

        return false;
    }

    /**
     * Equals comperator method.
     *
     * @param o
     *            <code>CorrelationKey</code> object to compare with
     *
     * @return <code>true</code> if the given object
     */
    public boolean equals(Object o) {
        OldCorrelationKey okey = (OldCorrelationKey) o;

        if (okey == null || okey._csetId != _csetId || okey._keyValues.length != _keyValues.length)
            return false;

        for (int i = 0; i < _keyValues.length; ++i)
            if (!_keyValues[i].equals(okey._keyValues[i]))
                return false;

        return true;
    }

    /**
     * Generate a hash code from the hash codes of the elements.
     *
     * @see java.util.HashMap#hashCode
     * @see Object#hashCode
     */
    public int hashCode() {
        int hashCode = _csetId;
        for (String _keyValue : _keyValues)
            hashCode ^= _keyValue.hashCode();
        return hashCode;
    }

    public List<String> toCanonicalList() {
        ArrayList<String> ret = new ArrayList<String>(_keyValues.length + 1);
        ret.add(((Integer) _csetId).toString());
        for (String i : _keyValues)
            ret.add(i);
        return ret;
    }

    /**
     * @see Object#toString
     */
    public String toString() {
        StringBuffer buf = new StringBuffer("{CorrelationKey ");
        buf.append("setId=");
        buf.append(_csetId);
        buf.append(", values=");
        buf.append(CollectionUtils.makeCollection(ArrayList.class, _keyValues));
        buf.append('}');

        return buf.toString();
    }

    public String toCanonicalString() {
        StringBuffer buf = new StringBuffer();
        buf.append(this.getCSetId());
        buf.append('~');
        for (int i = 0; i < getValues().length; ++i) {
            if (i != 0)
                buf.append('~');
            escapeTilde(buf, getValues()[i]);
        }
        return buf.toString();
    }

    static void escapeTilde(StringBuffer buf, String str) {
        if (str == null)
            return;
        char[] chars = str.toCharArray();
        for (char achar : chars) {
            if (achar == '~') {
                buf.append("~~");
            } else {
                buf.append(achar);
            }
        }
    }
}
