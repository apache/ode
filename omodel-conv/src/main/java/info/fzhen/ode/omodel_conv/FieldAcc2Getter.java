package info.fzhen.ode.omodel_conv;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.cu.CompilationUnit;
import spoon.reflect.cu.SourceCodeFragment;
import spoon.reflect.reference.CtFieldReference;

/**
 * Transform access to a field to it's getter. It doesn't distinguish if it should be setter or getter, however.
 * So, the {@link FieldAssign2Setter} should be launched beforehand.
 * @author fangzhen
 *
 */
public class FieldAcc2Getter extends AbstractProcessor<CtFieldAccess<?>> {
	public static Logger logger = Logger.getLogger(FieldAcc2Getter.class.getName());
	static{
		BasicConfigurator.configure();
	}
	@Override
	public void process(CtFieldAccess<?> fAccess) {
		if (fAccess.getVariable().isStatic()) return;
//		System.out.println(fAccess + " position " + fAccess.getPosition().getSourceStart() + " "  + fAccess.getPosition().getSourceEnd());
		CtFieldReference<?> targeted = fAccess.getVariable();
		if (targeted.getSimpleName() == "class")return;
		if (targeted.getQualifiedName().startsWith("org.apache.ode.bpel.o.") ||
				targeted.getQualifiedName().startsWith("org.apache.ode.bpel.elang.xpath10.o") ||
				targeted.getQualifiedName().startsWith("org.apache.ode.bpel.elang.xpath20.o") ||
				targeted.getQualifiedName().startsWith("org.apache.ode.bpel.elang.xquery10.o")) {
			CompilationUnit cu = fAccess.getPosition().getCompilationUnit();
			SourceCodeFragment fragment = new SourceCodeFragment();
			fragment.replacementLength = targeted.getSimpleName().length();
			fragment.position = fAccess.getPosition().getSourceEnd() - fragment.replacementLength + 1;
			String vname = fAccess.getVariable().getSimpleName();
			if (vname.startsWith("_")) vname = vname.substring(1);
			vname = vname.substring(0,1).toUpperCase() + vname.substring(1);
			String methString = "get";
			if (targeted.getType().equals(fAccess.getFactory().Type().BOOLEAN_PRIMITIVE) 
					||targeted.getType().equals(fAccess.getFactory().Type().BOOLEAN)){
				methString = "is";
			}
			fragment.code  = methString + vname + "()";
			cu.addSourceCodeFragment(fragment);
			logger.info("Accessed field: " + fAccess + "; Generated getter: " + fragment.code);
		}
	}
}
