package org.apache.ode.bpel.obj.migrate;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

import org.apache.ode.bpel.obj.Extensible;

/**
 * Upgrade Omodel Object to newest version. Used as
 * visitor of {@link ObjectTraverser}
 * @see ObjectTraverser
 */
public class OmUpgradeVisitor extends AbstractObjectVisitor{

	@Override
	protected boolean isCollection(Object old) {
		return old instanceof Collection;
	}
	@Override
	protected boolean isSet(Object obj){
		return false;
	}

	@Override
	public Object visitPojo(Object obj) {
		if (! (obj instanceof Extensible)){
			return null;
		}
		visitExtensible(obj);
		return null;
	}

	private void visitExtensible(Object obj) {
		((Extensible)obj).upgrade2Newest();
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

}
