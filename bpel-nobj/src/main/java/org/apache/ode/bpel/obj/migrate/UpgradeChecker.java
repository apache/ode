package org.apache.ode.bpel.obj.migrate;

import java.lang.reflect.Field;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.obj.ExtensibleImpl;

public class UpgradeChecker extends AbstractObjectVisitor{
    private static final Log __log = LogFactory.getLog(UpgradeChecker.class);

    private boolean newest = true;
	@Override
	public Object visitPojo(Object obj) {
		if (! (obj instanceof ExtensibleImpl)){
			return null;
		}
		visitExtensible(obj);
		return null;
	}

	private void visitExtensible(Object obj) {
		ExtensibleImpl eobj = (ExtensibleImpl)obj;
		if (eobj.getClassVersion() != eobj.CURRENT_CLASS_VERSION){
			newest = false;
			__log.debug(obj.getClass() + "hasn't upgraded to newest version. current: " 
					+ eobj.getClassVersion() + ", newest: " + eobj.CURRENT_CLASS_VERSION);
		}
		List<Field> fields = MigUtils.getAllFields(obj.getClass());
		for (Field f : fields){
			f.setAccessible(true);
			try {
				Object value = f.get(obj);
				if (value != null){
					traverse.traverseObject(value);
				}
			} catch (Exception e){
				throw new RuntimeException(e);
			}
		}
	}

	public boolean isNewest() {
		return newest;
	}
}
