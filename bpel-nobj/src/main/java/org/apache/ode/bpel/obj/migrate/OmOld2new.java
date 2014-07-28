package org.apache.ode.bpel.obj.migrate;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.obj.ExtensibleImpl;

public class OmOld2new extends AbstractObjectVisitor{
    private static final Log __log = LogFactory.getLog(OmOld2new.class);
    
	private static Map<String, String> beanPkgMap = new HashMap<String, String>();
	static{
		beanPkgMap.put("org.apache.ode.bpel.o", "org.apache.ode.bpel.obj");
		beanPkgMap.put("org.apache.ode.bpel.elang.xpath10.o", "org.apache.ode.bpel.elang.xpath10.obj");
		beanPkgMap.put("org.apache.ode.bpel.elang.xpath20.o", "org.apache.ode.bpel.elang.xpath20.obj");
		beanPkgMap.put("org.apache.ode.bpel.elang.xquery10.o", "org.apache.ode.bpel.elang.xquery10.obj");
	}
	
	public Object visit(Object obj){
		__log.debug("migrating object: " + obj.getClass() + "@" + System.identityHashCode(obj));
		Object n;
		if (isMap(obj)){
			n = visitMap(obj);
		}else if (isCollection(obj)){
			n = visitCollection(obj);
		}else if (isArray(obj)){
			n = visitArray(obj);
		}else{
			n = visitPojo(obj);
		}
		rtab.assign(obj, n);
		
		if (isMap(obj)){
			visitMap(obj, n);
		}else if (isCollection(obj)){
			visitCollection(obj, n);
		}else if (isArray(obj)){
			visitArray(obj, n);
		}else{
			visitPojo(obj, n);
		}
		return n;
	}


	@Override
	protected boolean isCollection(Object old) {
		return (old instanceof Collection);
	}

	private boolean isOmodelBean(Object old){
		Class<?> cls = old.getClass();
		if (beanPkgMap.containsKey(cls.getPackage().getName()) && !cls.getSimpleName().equals("Serializer")){
			return true;
		}
		return false;
	}
	@Override
	public Object visitArray(Object old) {
		throw new UnsupportedOperationException("Create new Array is unsupported");
	}

	private void visitArray(Object obj, Object n) {
		throw new UnsupportedOperationException("We don't need the method here");
	}

	@Override
	@SuppressWarnings({ "rawtypes"})
	public Object visitCollection(Object old) {
		Collection o = (Collection) old;
		try {
			Collection n = o.getClass().newInstance();
			return n;
		} catch (Exception e){
			//should not get here
			e.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void visitCollection(Object old, Object nu) {
		Collection o = (Collection) old;
		Collection n = (Collection) nu;
		for (Object obj : o){
			n.add(traverse.traverseObject(obj));
		}
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object visitMap(Object old) {
		Map o = (Map) old;
		try{
			Map n = o.getClass().newInstance();
			return n;
		}catch (Exception e){
			//should not get here
			e.printStackTrace();			
		}
		return null;
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void visitMap(Object obj, Object nu) {
		Set<Entry> entries = ((Map)obj).entrySet();
		Map n = (Map)nu;
		for (Entry e : entries){
			n.put(traverse.traverseObject(e.getKey()), traverse.traverseObject(e.getValue()));
		}
	}

	@Override
	public Object visitPojo(Object old) {
		if (!isOmodelBean(old)){
			return old;
		}else{
			return initiateNew(old);
		}
	}

	private void visitPojo(Object old, Object n) {
		if (isOmodelBean(old)){
			constructNewOm(old, n);
		}
	}
	/**
	 * construct new omodel instances from old ones. Assume <code>old</code> is an old OmodelBean
	 * @param old
	 * @return
	 */
	private Object constructNewOm(Object old, Object tn) {
		assert tn instanceof ExtensibleImpl;
		ExtensibleImpl n = (ExtensibleImpl) tn;
		List<Field> fields  = getAllFields(old.getClass());
		Map<String, Object> fieldMap = n.getFieldContainer();
		for (Field f : fields){
			if ((f.getModifiers() & Modifier.STATIC) != 0){
				continue; //skip static fields
			}
			f.setAccessible(true);
			try{
				String fname = f.getName();
				Object fvalue = f.get(old);
				if (fvalue != null){
					fieldMap.put(fname, traverse.traverseObject(fvalue));
				}else{
					fieldMap.put(fname, null);
				}
			} catch (Exception e) {
				RuntimeException rte = new RuntimeException("Error when try to construct corresponding new Omodel class from old one:"
						+old.getClass() + "; Failed on field:" + f.getName());
				rte.initCause(e);
				throw rte;
			}
		}
		return n;
	}
	
	private List<Field> getAllFields(Class cls) {
		return getAllFieldsRec(cls, new ArrayList<Field>());
	}

	private List<Field> getAllFieldsRec(Class cls, ArrayList<Field> fields) {
		Class par = cls.getSuperclass();
		if (par != null){
			getAllFieldsRec(par, fields);
		}
		fields.addAll(Arrays.asList(cls.getDeclaredFields()));
		return fields;
	}

	private Object initiateNew(Object old) {
		String clsName = old.getClass().getName();
		String qcls = clsName.replace(".o.", ".obj.");
		try {
			return Class.forName(qcls).newInstance();
		} catch (Exception e) {
			RuntimeException rte = new RuntimeException("Error when try to initiate corresponding new Omodel class of old one:"+old.getClass());
			rte.initCause(e);
			throw rte;
		}
	}

	@Override
	public Object visitSet(Object obj) {
		throw new UnsupportedOperationException("We don't really need this operatiion here");
	}
}
