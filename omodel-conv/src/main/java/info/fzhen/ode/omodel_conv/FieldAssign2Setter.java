package info.fzhen.ode.omodel_conv;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

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
	public static Logger logger = Logger.getLogger(FieldAssign2Setter.class.getName());
	static{
		BasicConfigurator.configure();
	}
	@Override
	public void process(CtAssignment<?, ?> element) {
//		System.out.println(element);
		CtExpression<?> assigned = element.getAssigned();
		if (assigned instanceof CtFieldAccess){
			CtFieldAccess<?> fAccess = (CtFieldAccess<?>)assigned;
//			if (fAccess.getParent((Class)fAccess.getVariable().getDeclaringType().getDeclaration().getClass()) != null) return;
			CtExpression<?> assignment = element.getAssignment();
			if (fAccess.getVariable().isStatic()) return;
			if (fAccess.getVariable().getQualifiedName().startsWith("org.apache.ode.bpel.o.") ||
					fAccess.getVariable().getQualifiedName().startsWith("org.apache.ode.bpel.elang.xpath10.o") ||
					fAccess.getVariable().getQualifiedName().startsWith("org.apache.ode.bpel.elang.xpath20.o") ||
					fAccess.getVariable().getQualifiedName().startsWith("org.apache.ode.bpel.elang.xquery10.o")) {
				CompilationUnit cu = fAccess.getPosition().getCompilationUnit();
				SourceCodeFragment fragment = new SourceCodeFragment();
				fragment.position = fAccess.getPosition().getSourceEnd() - fAccess.getVariable().getSimpleName().length() + 1;
				fragment.replacementLength = assignment.getPosition().getSourceStart() - fragment.position;
				
				String vname = fAccess.getVariable().getSimpleName();
				if (vname.startsWith("_")) vname = vname.substring(1);
				vname = vname.substring(0,1).toUpperCase() + vname.substring(1);
				String setter = "set" + vname + "(";
				fragment.code = setter;
				cu.addSourceCodeFragment(fragment);
				
				SourceCodeFragment fragment2 = new SourceCodeFragment();
				fragment2.position = assignment.getPosition().getSourceEnd() + 1;
				fragment2.replacementLength = 0;
				fragment2.code = ")";
				cu.addSourceCodeFragment(fragment2);
				logger.info("Accessed field: " + fAccess + "; Generated setter: " + setter + ")");
			}
		}
	}

}
