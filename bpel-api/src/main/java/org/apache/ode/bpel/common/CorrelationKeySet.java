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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * This class implements a set of correlation keys.
 *
 * The example of canonical forms of correlation key sets are:
 *
 *  <ul>
 *  <li>@2</li>
 *  <li>@2[12~a~b]</li>
 *  <li>@2[12~a~b],[25~b~c]</li>
 *  </ul>
 *
 *  The first example shows an empty correlation key set. The second shows a set with one correlation key inside.
 *  The third shows a set with two keys inside. The correlation keys are sorted by the correlation set ids.
 *
 * @author sean
 *
 */
public class CorrelationKeySet implements Serializable {
    private static final long serialVersionUID = 1L;

    public final static String VERSION_1 = "1";
    public final static String VERSION_2 = "2";

    @SuppressWarnings("unused")
    private String version = VERSION_2;

    private final Set<CorrelationKey> correlationKeys = new TreeSet<CorrelationKey>(new CorrelationKeyComparator());

    /**
     * Default Constructor
     */
    public CorrelationKeySet() {
    }

    /**
     * Restores the state by parsing the given canonical form of correlation key set.
     *
     * @param canonicalForm canonical form of correlation key set
     */
    public CorrelationKeySet(String canonicalForm) {
        restore(canonicalForm);
    }

    /**
     * Adds a correlation key to this correlation key set. If a correlation key with the same correlation set id
     * already exists, the old one is replaced with the given new one.
     *
     * @param ck a correlation key to add
     * @return returns this correlation key set
     */
    public CorrelationKeySet add(CorrelationKey ck) {
        for( CorrelationKey key : correlationKeys ) {
            if( key.getCorrelationSetName().equals(ck.getCorrelationSetName()) ) {
                correlationKeys.remove(ck);
                break;
            }
        }
        correlationKeys.add(ck);

        return this;
    }

    /**
     * Checks if this correlation key set contains the opaque correlation key as the only key
     * in this correlation key set.
     *
     * @return returns true if the correlation key set is opaque
     */
    public boolean isOpaque() {
        return correlationKeys.size() == 1 && correlationKeys.iterator().next().getCorrelationSetName().equals("-1");
    }

    /**
     * Checks if an incoming message with this correlation key set can be accepted by the given
     * correlation key set.
     *
     * @param candidateKeySet a correlation key set stored in a route
     * @param isAllRoute use true if the route="all" is set
     * @return return true if routable
     */
    public boolean isRoutableTo(CorrelationKeySet candidateKeySet, boolean isAllRoute) {
        boolean isRoutable = containsAll(candidateKeySet);

        if( isAllRoute ) {
            isRoutable = isRoutable || candidateKeySet.isOpaque() && isEmpty();
        }

        return isRoutable;
    }

    /**
     * Checks if this correlation key set contains all correlation keys from the given correlation key set.
     *
     * @param c a correlation key set
     * @return return true if this correlation key set is a superset
     */
    public boolean containsAll(CorrelationKeySet c) {
        Iterator<CorrelationKey> e = c.iterator();
        while (e.hasNext())
            if (!contains(e.next()))
                return false;
        return true;
    }

    /**
     * Returns true if this correlation key set contains no correlation keys.
     *
     * @return returns true if empty
     */
    public boolean isEmpty() {
        return correlationKeys.isEmpty();
    }

    /**
     * Returns true if this correlation key set contains the give correlation key.
     *
     * @param correlationKey a correlation key
     * @return
     */
    public boolean contains(CorrelationKey correlationKey) {
        Iterator<CorrelationKey> e = correlationKeys.iterator();
        if (correlationKey == null) {
            while (e.hasNext())
                if (e.next() == null)
                    return true;
        } else {
            while (e.hasNext()) {
                if (correlationKey.equals(e.next()))
                    return true;
            }
        }
        return false;
    }

    /**
     * Returns an iterator on the correlation keys that this correlation key set contains.
     *
     * @return an iterator
     */
    public Iterator<CorrelationKey> iterator() {
        return correlationKeys.iterator();
    }

    /**
     * Removes all correlation keys in this correlation keys.
     */
    public void clear() {
        correlationKeys.clear();
    }

    @Override
    public boolean equals(Object o) {
        if( o == null || !(o instanceof CorrelationKeySet) ) {
            return false;
        }
        CorrelationKeySet another = (CorrelationKeySet)o;

        if( correlationKeys.size() != another.correlationKeys.size() ) {
            return false;
        }

        return containsAll(another);
    }

    /**
     * Finds all subsets of this correlation key set.
     *
     * @return a list of all subset correlation key sets
     */
    public List<CorrelationKeySet> findSubSets() {
        List<CorrelationKeySet> subSets = new ArrayList<CorrelationKeySet>();

        // if the key set contains a opaque key and at least one non-opaque key, take out the opaque key
        CorrelationKey opaqueKey = null;
        boolean containsNonOpaque = false;
        CorrelationKeySet explicitKeySet = new CorrelationKeySet();
        for( CorrelationKey ckey : correlationKeys ) {
            // assumes only ONE opaque key if there is
            if( ckey.getCorrelationSetName().equals("-1") ) {
                opaqueKey = ckey;
            } else {
                containsNonOpaque = true;
            }
            explicitKeySet.add(ckey);
        }
        if( opaqueKey != null && containsNonOpaque ) {
            explicitKeySet.correlationKeys.remove(opaqueKey);
        }

        // we are generating (2 powered by the number of correlation keys) number of sub-sets
        for( int setIndex = 0; setIndex < Math.pow(2, explicitKeySet.correlationKeys.size()); setIndex++ ) {
            CorrelationKeySet subKeySet = new CorrelationKeySet();
            int bitPattern = setIndex; // the bitPattern will be 0b0000, 0b0001, 0b0010 and so on
            Iterator<CorrelationKey> ckeys = explicitKeySet.iterator();
            while( ckeys.hasNext() && bitPattern > 0 ) { // bitPattern > 0 condition saves half of the iterations
                CorrelationKey ckey = ckeys.next();
                if( (bitPattern & 0x01) > 0 ) {
                    subKeySet.add(ckey);
                }
                bitPattern = bitPattern >> 1;
            }

            if(!subKeySet.isEmpty()) { // we don't want an empty set
                subSets.add(subKeySet);
            }
        }

        if( subSets.isEmpty() ) {
            subSets.add(new CorrelationKeySet());
        }

        return subSets;
    }

    /**
     * Returns a canonical form of this correlation key set.
     *
     * @return
     */
    public String toCanonicalString() {
        StringBuffer buf = new StringBuffer();

        for( CorrelationKey ckey : correlationKeys ) {
            if( buf.length() > 0 ) {
                buf.append(",");
            }
            buf.append("[").append(escapeRightBracket(ckey.toCanonicalString())).append("]");
        }

        return "@" + VERSION_2 + buf.toString();
    }

    private static String escapeRightBracket(String str) {
        if (str == null)
            return null;

        StringBuffer buf = new StringBuffer();

        char[] chars = str.toCharArray();
        for (char achar : chars) {
            if (achar == ']') {
                buf.append("]]");
            } else {
                buf.append(achar);
            }
        }

        return buf.toString();
    }

    @Override
    public String toString() {
        return correlationKeys.toString();
    }

    /**
     * Restores the state of this correlation key set from a canonical form.
     *
     * @param canonicalForm a canonical form of correlation key set
     */
    public void restore(String canonicalForm) {
        if( canonicalForm == null || canonicalForm.trim().length() == 0 ) return;

        if( canonicalForm.startsWith("@") ) {
            parseCanonicalForm(canonicalForm);
        } else {
            version = VERSION_1;
            add( new CorrelationKey(canonicalForm) );
        }
    }

    private static enum ParserState {
        INITIAL, MET_ALPHA, MET_LEFT_BRACKET, MET_RIGHT_BRACKET, MET_COMMA
    }

    // parses a canonical form of correlation key set through an automata subsystem(FSM)
    private void parseCanonicalForm(String canonicalForm) {
        ParserState state = ParserState.INITIAL;

        StringBuffer buf = new StringBuffer();
        for( int i = 0; i < canonicalForm.length(); i++ ) {
            char ch = canonicalForm.charAt(i);
            if( state == ParserState.INITIAL ) {
                if( ch == '@' ) {
                    state = ParserState.MET_ALPHA;
                } else {
                    buf.append(ch);
                    state = ParserState.MET_LEFT_BRACKET;
                }
            } else if( state == ParserState.MET_ALPHA ) {
                if( ch == '[' ) {
                    version = buf.toString();
                    buf.setLength(0);
                    state = ParserState.MET_LEFT_BRACKET;
                } else {
                    buf.append(ch);
                }
            } else if( state == ParserState.MET_LEFT_BRACKET ) {
                if( ch == ']' ) {
                    state = ParserState.MET_RIGHT_BRACKET;
                } else {
                    buf.append(ch);
                }
            } else if( state == ParserState.MET_RIGHT_BRACKET ) {
                if( ch == ']' ) {
                    buf.append(ch);
                    state = ParserState.MET_LEFT_BRACKET;
                } else if( ch == ',' ) {
                    if( buf.toString().trim().length() != 0 ) {
                        add( new CorrelationKey(buf.toString()) );
                    }
                    buf.setLength(0);
                    state = ParserState.MET_COMMA;
                } else if( ch == '?' ) { // this is only a convenient feature for testing
                    if( buf.toString().trim().length() != 0 ) {
                        add( new OptionalCorrelationKey(buf.toString()) );
                    }
                    buf.setLength(0);
                    state = ParserState.MET_COMMA;
                }
            } else if( state == ParserState.MET_COMMA ) {
                if( ch == '[' ) {
                    state = ParserState.MET_LEFT_BRACKET;
                }
            }
        }
        if( buf.toString().trim().length() != 0 ) {
            if( state == ParserState.MET_ALPHA ) {
                version = buf.toString();
            } else {
                add( new CorrelationKey(buf.toString()) );
            }
        }
    }

    private class CorrelationKeyComparator implements Serializable, Comparator<CorrelationKey> {
        private static final long serialVersionUID = 1L;

        public int compare(CorrelationKey o1, CorrelationKey o2) {
            if( o1 == null || o2 == null ) {
                return 0;
            }
            // used only in sorting the correlation keys in the CorrelationKeySet; does not matter with the values
            return o1.getCorrelationSetName().compareTo(o2.getCorrelationSetName());
        }
    }
}