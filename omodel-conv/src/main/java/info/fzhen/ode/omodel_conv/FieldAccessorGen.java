package info.fzhen.ode.omodel_conv;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtStatementList;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;
import spoon.support.reflect.declaration.CtAnnotationImpl;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class FieldAccessorGen extends AbstractProcessor<CtField<?>>{
	private CtField<?> field;
	private Factory factory;
	private CtClass<?> clazz;
	private CtTypeReference<?> fieldType;
	private String fname;
	private String cname;
	private ModifierKind accCtl;
	
	public void process(CtField<?> f) {
		init(f);
		generate();
	}
	private void init(CtField<?> f) {
		this.field = f;
		factory = f.getFactory();
		clazz = (CtClass<?>) field.getDeclaringType();
		fieldType = field.getType();
		fname = field.getSimpleName();
		if (fname.startsWith("_")) {
			fname = fname.substring(1);
		}
		cname = fname.toUpperCase();

		Set<ModifierKind> modifiers = field.getModifiers();
		if (modifiers.contains(ModifierKind.PUBLIC)) {
			accCtl = ModifierKind.PUBLIC;
		} else if (modifiers.contains(ModifierKind.PROTECTED)) {
			accCtl = ModifierKind.PROTECTED;
		} else if (modifiers.contains(ModifierKind.PRIVATE)) {
			accCtl = ModifierKind.PRIVATE;
		} else {
			accCtl = null;
		}
	}

	public void generate() {
		Set<ModifierKind> modifiers = field.getModifiers();
		if (modifiers.contains(ModifierKind.PRIVATE)) {
			return;
		}
		if (modifiers.contains(ModifierKind.STATIC)) {
			return;
		}

		genSetter();
		genGetter();
		genConstant();
	}

	public void genConstant() {
		Set<ModifierKind> modifiers = new LinkedHashSet<ModifierKind>();
		modifiers.add(ModifierKind.PRIVATE);
		modifiers.add(ModifierKind.STATIC);
		modifiers.add(ModifierKind.FINAL);
		field.setModifiers(modifiers);
		field.setType((CtTypeReference) factory.Type().STRING);
		field.setSimpleName(cname);
		CtExpression<?> expression = factory.Code()
				.createCodeSnippetExpression("\"" + fname + "\"");
		field.setDefaultExpression((CtExpression) expression);
	}

	@SuppressWarnings("unchecked")
	public void genSetter() {
		Set<ModifierKind> modifiers = new HashSet<ModifierKind>();
		if (accCtl != null) {
			modifiers.add(accCtl);
		}
		String name = "set" + fname.substring(0, 1).toUpperCase()
				+ fname.substring(1);
		List<CtParameter<?>> parameters = new ArrayList<CtParameter<?>>();
		CtParameter<?> parameter = factory.Core().createParameter();
		parameter.setType((CtTypeReference) fieldType);
		parameter.setSimpleName(fname);
		parameters.add(parameter);
		Set<CtTypeReference<? extends Throwable>> thrownTypes = new HashSet<CtTypeReference<? extends Throwable>>();

		CtBlock<?> body = factory.Core().createBlock();
		CtStatementList<?> statements = factory.Code()
				.createStatementList(body);
		CtStatement statement = factory.Code().createCodeSnippetStatement(
				"fieldContainer.put(" + cname + ", "
						+ parameter.getSimpleName() + ");");
		statements.addStatement(statement);
		body.setStatements(statements.getStatements());

		@SuppressWarnings({ "unchecked", "rawtypes" })
		CtMethod<?> getMeth = factory.Method().create(clazz, modifiers,
				(CtTypeReference) fieldType, name, parameters, thrownTypes,
				body);
	}

	public void genGetter() {
		Set<ModifierKind> modifiers = new HashSet<ModifierKind>();
		if (accCtl != null) {
			modifiers.add(accCtl);
		}
		String name = "get" + fname.substring(0, 1).toUpperCase()
				+ fname.substring(1);
		List<CtParameter<?>> parameters = new ArrayList<CtParameter<?>>();
		Set<CtTypeReference<? extends Throwable>> thrownTypes = new HashSet<CtTypeReference<? extends Throwable>>();

		CtBlock<?> body = factory.Core().createBlock();
		CtStatementList<?> statements = factory.Code()
				.createStatementList(body);
		CtStatement statement = factory.Code().createCodeSnippetStatement(
				"return (" + field.getType().getSimpleName()
						+ ")fieldContainer.get(" + fname.toUpperCase() + ");");
		statements.addStatement(statement);
		body.setStatements(statements.getStatements());

		@SuppressWarnings({ "unchecked", "rawtypes" })
		CtMethod<?> getMeth = factory.Method().create(clazz, modifiers,
				(CtTypeReference) fieldType, name, parameters, thrownTypes,
				body);
		//		AnnotationFactory annof = factory.Annotation();
		//		annof.annotate(getMeth, JsonIgnore.class);
		CtAnnotation<JsonIgnore> jiAnno = new CtAnnotationImpl<JsonIgnore>();
		jiAnno.setAnnotationType(factory.Type().createReference(
				JsonIgnore.class));
		getMeth.addAnnotation(jiAnno);
	}

}
