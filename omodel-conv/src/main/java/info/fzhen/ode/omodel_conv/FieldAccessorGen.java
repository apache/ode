package info.fzhen.ode.omodel_conv;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtExpression;
import spoon.reflect.cu.CompilationUnit;
import spoon.reflect.cu.SourceCodeFragment;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtTypeReference;

/**
 * This class generates three items from a non-private and non-static field:
 * 1. A private final String indicates field name;
 * 2. getter annotated by JsonIgnore.
 * 3. setter
 * The initial expression will be dropped out if existed.
 * @author fangzhen
 *
 */
public class FieldAccessorGen extends AbstractProcessor<CtField<?>>{
	private CtField<?> field;
	private CtTypeReference<?> fieldType;
	private String fname;
	private String ofname; //original field name. with _
	private String cname;
	private ModifierKind accCtl;
	private String typeStr;
	public void process(CtField<?> f) {
		init(f);
		generate();
	}
	private void init(CtField<?> f) {
		this.field = f;
		fieldType = field.getType();
		ofname = field.getSimpleName();
		fname = ofname;
		if (ofname.startsWith("_")) {
			fname = fname.substring(1);
		}
		cname = fname.toUpperCase();
		List<CtTypeReference<?>> tas = fieldType.getActualTypeArguments();
		StringBuffer typeStr = new StringBuffer(fieldType.getSimpleName());
		if (tas.size() > 0){
			typeStr.append("<");
			for (CtTypeReference<?> ta : tas){
				typeStr.append(ta.getSimpleName());
				typeStr.append(",");
			}
			typeStr.setCharAt(typeStr.length()-1, '>');
		}
		this.typeStr = typeStr.toString();
		
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
		CtExpression<?> initExp = field.getDefaultExpression();
		CompilationUnit cu = field.getPosition().getCompilationUnit();
		SourceCodeFragment fragment = new SourceCodeFragment();
		if (initExp == null){
			fragment.position = field.getPosition().getSourceEnd() + 3;
		}else{
			fragment.position = initExp.getPosition().getSourceEnd() + 3;
		}
		fragment.replacementLength = 0;
		
		fragment.code = "\n" + genConstant() + "\n" +  genGetter() + "\n" + genSetter() + "\n\n" ;
		cu.addSourceCodeFragment(fragment);
	}

	public String genConstant() {		
		String cfStr = "";
		cfStr += "private static final String ";
		cfStr += cname + " = ";
		cfStr += "\"" + ofname + "\";";
		return cfStr;
	}

	public String genSetter() {
		Set<ModifierKind> modifiers = new HashSet<ModifierKind>();
		if (accCtl != null) {
			modifiers.add(accCtl);
		}
		String name = "set" + fname.substring(0, 1).toUpperCase()
				+ fname.substring(1);
	
		String setStr = "";
		for (ModifierKind m : modifiers){
			setStr += m.toString() + " ";
		}
		setStr += "void ";
		setStr +=  name + "(" + typeStr + " " + fname + "){\n";
		setStr += "fieldContainer.put(" + cname + ", "
				+ fname + ");\n}";
		return setStr;
	}

	public String genGetter() {
		Set<ModifierKind> modifiers = new HashSet<ModifierKind>();
		if (accCtl != null) {
			modifiers.add(accCtl);
		}
		String name = "get" + fname.substring(0, 1).toUpperCase()
				+ fname.substring(1);

		String getStr = "";
		getStr += "@JsonIgnore\n";
		for (ModifierKind m : modifiers){
			getStr += m.toString() + " ";
		}
		getStr += typeStr + " ";
		getStr += name + "(){\n";
		getStr += "return (" + typeStr
				+ ")fieldContainer.get(" + fname.toUpperCase() + ");\n}";
		return getStr;
	}

}
