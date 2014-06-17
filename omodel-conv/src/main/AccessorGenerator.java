package info.fzhen.ode.omodel_conv;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtThisAccess;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.CtAbstractVisitor;

public class AccessorGenerator extends CtAbstractVisitor{
	private Factory factory;
	public AccessorGenerator(){
		factory = TestUtils.createFactory();
	}
	public <T> void visitCtThisAccess(CtThisAccess<T> thisAccess) {
	}
	
	@Override
	public <T> void visitCtField(CtField<T> f) {
		CtClass<?> clazz = (CtClass<?>)f.getDeclaringType();
		Set<ModifierKind> modifiers = new HashSet<ModifierKind>();
		modifiers.add(ModifierKind.PUBLIC);
		CtTypeReference<T> returnType = f.getType();
		String fname = f.getSimpleName();
		String name = "get" + fname.substring(0,1).toUpperCase() + fname.substring(1);
		List<CtParameter<?>> parameters = new ArrayList<CtParameter<?>>();
		Set<CtTypeReference<? extends Throwable>> thrownTypes = new HashSet<CtTypeReference<? extends Throwable>>();
		CtBlock<T> body = factory.Core().createBlock();
		CtMethod<T> method = factory.Method().create(clazz, modifiers, returnType, name, parameters, thrownTypes, body);
		clazz.addMethod(method);
	}
}
