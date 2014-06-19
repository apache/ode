package info.fzhen.ode.omodel_conv;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.cu.CompilationUnit;
import spoon.reflect.cu.SourceCodeFragment;

/**
 * Transform access to a field to it's getter. It doesn't distinguish if it should be setter or getter, however.
 * So, the {@link FieldAssign2Setter} should be launched beforehand.
 * @author fangzhen
 *
 */
public class FieldAcc2Getter extends AbstractProcessor<CtFieldAccess<?>> {

	@Override
	public void process(CtFieldAccess<?> fAccess) {
		if (fAccess.getVariable().isStatic()) return;
//		if (fAccess.getParent((Class)fAccess.getVariable().getDeclaringType().getDeclaration().getClass()) != null) return;
		if (fAccess.getVariable().getQualifiedName()
				.startsWith("org.apache.ode.bpel.o.")) {
			CompilationUnit cu = fAccess.getPosition().getCompilationUnit();
			SourceCodeFragment fragment = new SourceCodeFragment();
			fragment.position = fAccess.getPosition().getSourceStart();
			fragment.replacementLength = fAccess.getPosition().getSourceEnd()
					- fAccess.getPosition().getSourceStart() + 1;
			String vname = fAccess.getVariable().getSimpleName();
			if (vname.startsWith("_")) vname = vname.substring(1);
			vname = vname.substring(0,1).toUpperCase() + vname.substring(1);
			fragment.code  = "get" + vname + "()";
			
			cu.addSourceCodeFragment(fragment);
		}
	}
}
