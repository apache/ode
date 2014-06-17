package info.fzhen.ode.omodel_conv;

import java.io.Serializable;

import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;

/**
 * package name org.apache.ode.bpel.o -> package name org.apache.ode.bpel.o 
 * remove implemented interface {@link Serializalble} and corresponding field<code>serialVersionUID</code>
 * @author fangzhen
 *
 */
@SuppressWarnings("rawtypes")
public class SerIntRm extends AbstractProcessor<CtType> {

	@SuppressWarnings({ "unchecked" })
	public void process(CtType ctType) {
		CtTypeReference<Serializable> serType = ctType.getFactory().Type()
				.createReference(Serializable.class);
		if (ctType.removeSuperInterface(serType)) {
			ctType.getPackage().setSimpleName("obj");
			CtField sid = ctType.getField("serialVersionUID");
			if (sid != null){
				ctType.removeField(sid);
			}
		}
	}
}
