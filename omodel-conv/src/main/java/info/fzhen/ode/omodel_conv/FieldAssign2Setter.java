package info.fzhen.ode.omodel_conv;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.cu.CompilationUnit;
import spoon.reflect.cu.SourceCodeFragment;

/**
 * Transform a assignment to field to it's setter.
 * @author fangzhen
 *
 */
public class FieldAssign2Setter extends AbstractProcessor<CtAssignment<?, ?>>{

	@Override
	public void process(CtAssignment<?, ?> element) {
		CtExpression<?> assigned = element.getAssigned();
		if (assigned instanceof CtFieldAccess){
			CtFieldAccess<?> fAccess = (CtFieldAccess<?>)assigned;
//			if (fAccess.getParent((Class)fAccess.getVariable().getDeclaringType().getDeclaration().getClass()) != null) return;
			CtExpression<?> assignment = element.getAssignment();
			if (fAccess.getVariable().isStatic()) return;
			if (fAccess.getVariable().getQualifiedName()
					.startsWith("org.apache.ode.bpel.o.")) {
				CompilationUnit cu = fAccess.getPosition().getCompilationUnit();
				SourceCodeFragment fragment = new SourceCodeFragment();
				fragment.position = fAccess.getPosition().getSourceStart();
				fragment.replacementLength = element.getPosition().getSourceEnd()
						- element.getPosition().getSourceStart() + 1;
				
				String vname = fAccess.getVariable().getSimpleName();
				if (vname.startsWith("_")) vname = vname.substring(1);
				vname = vname.substring(0,1).toUpperCase() + vname.substring(1);
				String setter = "set" + vname + " (" + assignment + ")";
				fragment.code = setter;
				cu.addSourceCodeFragment(fragment);
			}
		}
	}

}
