package org.apache.ode.bpel.obj.migrate;

import org.apache.ode.bpel.obj.ExtensibleImpl;

public class ExtensibeImplEqualityComp implements EqualityComparator{
	private EqualityVisitor visitor;
	
	public ExtensibeImplEqualityComp() {
	}
	public ExtensibeImplEqualityComp(EqualityVisitor visitor){
		this.visitor = visitor;
	}

	@Override
	public Boolean objectsEqual(Object obj1, Object obj2) {
		ExtensibleImpl esi = (ExtensibleImpl)obj1;
		ExtensibleImpl esio = null;
		try{
			esio = (ExtensibleImpl)obj2;
		}catch(ClassCastException e){
			return false;
		}
		visitor.setOther(esio.getFieldContainer());
		return visitor.visit(esi.getFieldContainer());
	}
	@Override
	public Boolean canHanle(Object obj) {
		return obj instanceof ExtensibleImpl;
	}
}
