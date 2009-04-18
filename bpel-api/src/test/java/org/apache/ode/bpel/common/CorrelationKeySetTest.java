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

import static org.junit.Assert.*;
import org.junit.Test;

public class CorrelationKeySetTest {
    private CorrelationKey keyX = new CorrelationKey("1~a~b");
    private CorrelationKey keyY = new CorrelationKey("2~b~c");
    private CorrelationKey keyZ = new CorrelationKey("3~c~d");
    private CorrelationKey optX = new OptionalCorrelationKey("1~a~b");
    private CorrelationKey optY = new OptionalCorrelationKey("2~b~c");
    private CorrelationKey optZ = new OptionalCorrelationKey("3~c~d");
    private CorrelationKey implicit = new CorrelationKey("-1~a");

    @Test
    public void testCanonicalString() throws Exception {
        CorrelationKeySet setA = new CorrelationKeySet();
        setA.add(keyX);
        setA.add(optY);
        assertEquals("@2[1~a~b],[2~b~c]", setA.toCanonicalString());
    }
    
    @Test
    public void testContainsAll() throws Exception {
        CorrelationKeySet setA = new CorrelationKeySet();
        CorrelationKeySet setB = new CorrelationKeySet();
        assertTrue(setA.containsAll(setB));
        
        setA.add(keyX);
        assertTrue(setA.containsAll(setB));

        setB.add(keyY);
        assertFalse(setA.containsAll(setB));

        setA.clear();
        setA.add(keyY);
        assertTrue(setA.containsAll(setB));
    }
    
    @Test
    public void testRestoreFromCanonicalForm() throws Exception {
        assertEquals(new CorrelationKeySet(null), new CorrelationKeySet());
        assertEquals(new CorrelationKeySet(""), new CorrelationKeySet());
        
        assertEquals(new CorrelationKeySet("-1~session_key"), 
                new CorrelationKeySet().add(new CorrelationKey("-1", new String[] {"session_key"})));
        assertEquals(new CorrelationKeySet("1~key1~key2"), 
                new CorrelationKeySet().add(new CorrelationKey("1", new String[] {"key1", "key2"})));

        assertEquals(new CorrelationKeySet("@2"), new CorrelationKeySet());
        assertEquals(new CorrelationKeySet("@2[-1~session_key]"), 
                new CorrelationKeySet().add(new CorrelationKey("-1", new String[] {"session_key"})));
        assertEquals(new CorrelationKeySet("@2[1~key1~key2]"), 
                new CorrelationKeySet().add(new CorrelationKey("1", new String[] {"key1", "key2"})));
        assertEquals(new CorrelationKeySet("@2[1~key1],[2~key2~key3]"), 
                new CorrelationKeySet().add(new CorrelationKey("1", new String[] {"key1"}))
                .add(new CorrelationKey("2", new String[] {"key2", "key3"})));
        assertEquals(new CorrelationKeySet("@2[1~key1],[2~key2~key3]?"), 
                new CorrelationKeySet().add(new CorrelationKey("1", new String[] {"key1"}))
                .add(new CorrelationKey("2", new String[] {"key2", "key3"})));
        assertEquals(3, new CorrelationKeySet("@2[1~key1],[2~key2~key3]?").findSubSets().size());
    }
    
    @Test
    public void testRoutableTo() throws Exception {
        CorrelationKeySet setA = new CorrelationKeySet();
        CorrelationKeySet setB = new CorrelationKeySet();
        assertTrue(setA.isRoutableTo(setB, false));
        assertTrue(setA.isRoutableTo(setB, true));
        
        setA.add(keyX);
        assertTrue(setA.isRoutableTo(setB, false));
        assertTrue(setA.isRoutableTo(setB, true));

        setB.add(keyY);
        assertFalse(setA.isRoutableTo(setB, false));
        assertFalse(setA.isRoutableTo(setB, true));

        setA.clear();
        setA.add(keyY);
        assertTrue(setA.isRoutableTo(setB, false));
        assertTrue(setA.isRoutableTo(setB, true));
        
        CorrelationKeySet inbound = new CorrelationKeySet();
        CorrelationKeySet candidate = new CorrelationKeySet();
        candidate.add(new CorrelationKey("-1~session_key"));
        assertFalse(inbound.isRoutableTo(candidate, false));
        assertTrue(inbound.isRoutableTo(candidate, true));

        inbound.add(new CorrelationKey("-1~session_key_different"));
        assertFalse(inbound.isRoutableTo(candidate, false));
        assertFalse(inbound.isRoutableTo(candidate, true));
        
        inbound.clear();
        inbound.add(keyX);
        inbound.add(implicit);
        candidate.clear();
        candidate.add(keyX);
        assertTrue(inbound.isRoutableTo(candidate, false));
    }
    
    @Test
    public void testFindSubSets() throws Exception {
        StringBuffer buf = new StringBuffer();
        for( CorrelationKeySet subSet : new CorrelationKeySet().findSubSets() ) {
            if( buf.length() > 0 ) {
                buf.append(",");
            }
            buf.append("'").append(subSet.toCanonicalString()).append("'");
        }
        assertEquals("'@2'",  buf.toString());

        CorrelationKeySet keySet = new CorrelationKeySet();
        keySet.add(keyX);
        keySet.add(keyY);
        keySet.add(keyZ);
        assertTrue(keySet.findSubSets().size() == 7);

        keySet = new CorrelationKeySet();       
        keySet.add(optX);
        keySet.add(optY);
        keySet.add(optZ);
        buf = new StringBuffer();
        for( CorrelationKeySet subSet : keySet.findSubSets() ) {
            if( buf.length() > 0 ) {
                buf.append(",");
            }
            buf.append("'").append(subSet.toCanonicalString()).append("'");
        }
        assertEquals("'@2[1~a~b]','@2[2~b~c]','@2[1~a~b],[2~b~c]','@2[3~c~d]','@2[1~a~b],[3~c~d]','@2[2~b~c],[3~c~d]','@2[1~a~b],[2~b~c],[3~c~d]'", 
            buf.toString());

        keySet = new CorrelationKeySet();       
        keySet.add(keyX);
        keySet.add(keyY);
        keySet.add(optZ);
        buf = new StringBuffer();
        for( CorrelationKeySet subSet : keySet.findSubSets() ) {
            if( buf.length() > 0 ) {
                buf.append(",");
            }
            buf.append("'").append(subSet.toCanonicalString()).append("'");
        }
        assertEquals("'@2[1~a~b]','@2[2~b~c]','@2[1~a~b],[2~b~c]','@2[3~c~d]','@2[1~a~b],[3~c~d]','@2[2~b~c],[3~c~d]','@2[1~a~b],[2~b~c],[3~c~d]'", buf.toString());

        keySet = new CorrelationKeySet();       
        keySet.add(keyX);
        keySet.add(optY);
        keySet.add(optZ);
        buf = new StringBuffer();
        for( CorrelationKeySet subSet : keySet.findSubSets() ) {
            if( buf.length() > 0 ) {
                buf.append(",");
            }
            buf.append("'").append(subSet.toCanonicalString()).append("'");
        }
        assertEquals("'@2[1~a~b]','@2[2~b~c]','@2[1~a~b],[2~b~c]','@2[3~c~d]','@2[1~a~b],[3~c~d]','@2[2~b~c],[3~c~d]','@2[1~a~b],[2~b~c],[3~c~d]'", buf.toString());
    }
}
