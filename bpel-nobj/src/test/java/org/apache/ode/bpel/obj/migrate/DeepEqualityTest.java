package org.apache.ode.bpel.obj.migrate;

import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.ode.bpel.obj.DebugInfo;
import org.junit.Test;

public class DeepEqualityTest {
     @Test
	public void simpleListTest(){
		List<String> ls1 = new ArrayList<String>();
		ls1.add("Hello");
		ls1.add("world");
		List<String> ls2 = new ArrayList<String>();
		ls2.add("Hello");
		ls2.add("world");
		TraverseObject traverse = new TraverseObject();
		EqualityVisitor visitor = new EqualityVisitor(ls2);
		traverse.accept(visitor);
		assertEquals(Boolean.TRUE, traverse.traverseObject(ls1));
		ls1.add(0, "!");
		ls2.add("!");
		visitor.setOther(ls2);
		assertEquals(Boolean.TRUE, traverse.traverseObject(ls1));		
	}
	
	@Test
	public void simpleSetTest(){
		Set<String> s1 = new LinkedHashSet();
		s1.add("hello");
		s1.add("world");
		Set<String> s2 = new LinkedHashSet();
		s2.add("world");
		s2.add("hello");
		
		TraverseObject traverse = new TraverseObject();
		EqualityVisitor visitor = new EqualityVisitor(s2);
		traverse.accept(visitor);
		assertEquals(Boolean.TRUE, traverse.traverseObject(s1));
		
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void simpleMapTest(){
		Map<String, Object> m1 = new HashMap();
		m1.put("item1", "string1");
		m1.put("item2", new ArrayList());
		Map<String, Object> m2 = new HashMap();
		m2.put("item1", "string1");
		m2.put("item2", new ArrayList());

		TraverseObject traverse = new TraverseObject();
		EqualityVisitor visitor = new EqualityVisitor(m2);
		traverse.accept(visitor);
		assertEquals(Boolean.TRUE, traverse.traverseObject(m1));
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void simpleTest() throws Exception{
		Map m1 = new HashMap();
		QName n1 = new QName("localpart");
		URI u1 = new URI("urn://a/uri");
		m1.put(u1, n1);
		DebugInfo d1 = new DebugInfo("/a/path", 0, 1, m1);
		m1.put("cyclic", d1);
		
		Map m2 = new HashMap();
		QName n2 = new QName("localpart");
		URI u2 = new URI("urn://a/uri");
		m2.put(u2, n2);
		DebugInfo d2 = new DebugInfo("/a/path", 0, 1, m2);
		m2.put("cylic", d2);

		TraverseObject traverse = new TraverseObject();
		EqualityVisitor visitor = new EqualityVisitor(d2);
		visitor.addCustomComparator(new ExtensibeImplEqualityComp(visitor));
		traverse.accept(visitor);
		assertEquals(Boolean.TRUE, traverse.traverseObject(d1));
	}
}
