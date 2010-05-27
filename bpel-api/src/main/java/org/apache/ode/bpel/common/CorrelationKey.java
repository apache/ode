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
package org.apache.ode.bpel.common;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.ode.utils.CollectionUtils;

/**
 * <p>
 * Message correlation key. Correlation keys are used to match up incoming
 * messages with a particular process <em>instance</em>. The basic procedure
 * is to generate and save a correlation key when a <code>receive</code> or
 * <code>pick</em> activity is activated, and then to match incoming messages
 * against all correlation keys so saved, finally associating the message with
 * the process instance that had the matching correlation key. In reality this
 * process is somewhat more complicated as pains must be taken to avoid race
 * conditions and to make the matching efficient.</p>
 *
 * <p>The correlation keys used in the above process consists of a collection
 * of name-value pairs, with the name corresponding to a property name (as
 * defined using the <code>&lt;property&gt;</code> element of the BPEL process
 * document) and with the value corresponding to the value of said property as
 * obtained from a message by means of a property alias (as defined using
 * the <code>&lt;propertyAlias;&gt</code> BPEL process document element).
 * </p>
 */
public class CorrelationKey implements Serializable {

    private static final long serialVersionUID = 1L;

    /** CorrelationSet name. */
    private String _csetName;
    /** Key values. */
    private String _keyValues[];

    /**
     * Constructor.
     *
     * @param csetName
     *            correlation set identifier
     * @param keyValues
     *            correlation key values
     */
    public CorrelationKey(String csetName, String[] keyValues) {
        _csetName = csetName;
        _keyValues = keyValues;
    }

    public CorrelationKey(String canonicalForm) {
        int firstTilde = canonicalForm.indexOf('~');
        _csetName = canonicalForm.substring(0, firstTilde == -1 ? canonicalForm.length() : firstTilde);

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
    public String getCorrelationSetName() {
        return _csetName;
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
    public boolean isMatch(CorrelationKey[] keys) {
        for (CorrelationKey key : keys)
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
        if (!(o instanceof CorrelationKey)) {
            return false;
        }

        CorrelationKey okey = (CorrelationKey) o;

        if (okey == null || !okey._csetName.equals(_csetName) || okey._keyValues.length != _keyValues.length)
            return false;

        for (int i = 0; i < _keyValues.length; ++i)
            if (!_keyValues[i].equals(okey._keyValues[i]))
                return false;

        return true;
    }

    /**
     * Generate a hash code from the hash codes of the elements.
     *
     * @see HashMap#hashCode
     * @see Object#hashCode
     */
    public int hashCode() {
        int hashCode = _csetName.hashCode();
        for (String _keyValue : _keyValues)
            hashCode ^= _keyValue.hashCode();
        return hashCode;
    }

    /**
     * @see Object#toString
     */
    public String toString() {
        StringBuffer buf = new StringBuffer("{CorrelationKey ");
        buf.append("setId=");
        buf.append(_csetName);
        buf.append(", values=");
        buf.append(CollectionUtils.makeCollection(ArrayList.class, _keyValues));
        buf.append('}');

        return buf.toString();
    }

    public String toCanonicalString() {
        StringBuffer buf = new StringBuffer();
        buf.append(_csetName);
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
