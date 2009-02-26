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
package org.apache.ode.bpel.extvar.jdbc;

import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.ode.bpel.engine.extvar.ExternalVariableConf;
import org.apache.ode.bpel.engine.extvar.ExternalVariableConf.Variable;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.GUID;
import org.apache.ode.bpel.evar.ExternalVariableModule.Locator;
import org.apache.ode.bpel.evar.ExternalVariableModule.Value;
import org.hsqldb.jdbc.jdbcDataSource;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;



/**
 * Test for the JDBC external variable engine.
 * 
 * @author Maciej Szefler <mszefler at gmail dot com>
 *
 */
public class JdbcExternalVariableEngineTest extends TestCase {
    
    final Long _iid = 123L;
    final QName _pid = new QName("foo", "pid");
    final QName _varType = new QName("foo", "foobar");
    
    ExternalVariableConf _econf;
    jdbcDataSource _ds;
    JdbcExternalVariableModule _engine;
    Element _el1;

    public void setUp() throws Exception {
        _ds = new org.hsqldb.jdbc.jdbcDataSource();
        _ds.setDatabase("jdbc:hsqldb:mem:" + new GUID().toString());
        _ds.setUser("sa");
       
        Connection conn = _ds.getConnection();
        Statement s = conn.createStatement();
        s.execute("create table extvartable1 (" +
                "id1 VARCHAR PRIMARY KEY," +
                "_id2_ VARCHAR," +
                "pid VARCHAR, " +
                "iid INT," +
                "cts DATETIME," +
                "uts DATETIME," +
                "foo VARCHAR," +
                "bar VARCHAR );");
        
        _engine = new JdbcExternalVariableModule();
        _engine.registerDataSource("testds",_ds);
        
        _el1=DOMUtils.stringToDOM("<foobar><id2>ignored</id2><foo>foo</foo><bar>bar</bar></foobar>");
    }
    
    @Test
    public void testConfigurationParsing() throws Exception {
        Document deploydoc = DOMUtils.parse(getClass().getResourceAsStream("evardeploy.xml"));
        NodeList nl = deploydoc.getElementsByTagNameNS(ExternalVariableConf.EXTVARCONF_ELEMENT.getNamespaceURI(), 
                ExternalVariableConf.EXTVARCONF_ELEMENT.getLocalPart());

        ArrayList<Element> al = new ArrayList<Element>();
        for (int i = 0; i < nl.getLength(); ++i) {
            al.add((Element)nl.item(i));
        }
        
        assertTrue(al.size() >= 1);
        _econf = new ExternalVariableConf(al);
        assertEquals(al.size(), _econf.getVariables().size());
    }

    
    @Test
    public void testConfigure() throws Exception {
        testConfigurationParsing();
        for (Variable v :_econf.getVariables())
            _engine.configure(_pid, v.extVariableId, v.configuration);
    }
    
    
    @Test
    public void testInitWriteValue() throws Exception {
        testConfigure();
        
        Locator locator = new Locator("evar1",_pid,_iid);
        Value value = new Value(locator, _el1, null);
        value = _engine.writeValue(_varType, value);
        assertNotNull(value);
        assertNotNull(value.locator);
        System.out.println(DOMUtils.domToString(value.locator.reference));
        assertTrue(DOMUtils.domToString((Element)value.locator.reference).indexOf("id1")!=-1);
        assertTrue(DOMUtils.domToString((Element)value.locator.reference).indexOf("id2")!=-1);
    }

    @Test
    public void testWriteUpdate() throws Exception {
        testConfigure();
        
        Locator locator = new Locator("evar1",_pid,_iid);
        Value value = new Value(locator, _el1, null);
        value = _engine.writeValue(_varType, value);
        String domstr = DOMUtils.domToStringLevel2(value.value);
        Value newvalue = new Value(value.locator,DOMUtils.stringToDOM(domstr.replaceAll("<bar>bar</bar>", "<bar>boohoo</bar>")),null);
        _engine.writeValue(_varType, newvalue);
        QName qname = new QName("http://example.com", "bar");
        Value reread = _engine.readValue(qname, newvalue.locator);
        domstr = DOMUtils.domToString(reread.value);
        assertTrue(domstr.contains("boohoo"));
        assertFalse(domstr.contains(">bar<"));
    }
    
    @Test
    public void testRead() throws Exception {
        testConfigure();
        Locator locator = new Locator("evar1",_pid,_iid);
        Value value = new Value(locator, _el1, null);
        value = _engine.writeValue(_varType, value);
        
        Value readVal = _engine.readValue(_varType, value.locator);
        
        assertEquals(_iid,readVal.locator.iid);
        assertEquals(_pid,readVal.locator.pid);
        assertEquals(2, DOMUtils.countKids((Element)readVal.locator.reference, Node.ELEMENT_NODE));
        assertEquals(DOMUtils.domToString(value.locator.reference), DOMUtils.domToString(readVal.locator.reference));
        

    }
}
