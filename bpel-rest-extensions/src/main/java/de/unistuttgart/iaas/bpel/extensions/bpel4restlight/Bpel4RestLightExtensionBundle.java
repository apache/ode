package de.unistuttgart.iaas.bpel.extensions.bpel4restlight;

import org.apache.ode.bpel.runtime.common.extension.AbstractExtensionBundle;


/**
 * 
 * Copyright 2011 IAAS University of Stuttgart <br>
 * <br>
 * 
 * @author uwe.breitenbuecher@iaas.uni-stuttgart.de
 * 
 */
public class Bpel4RestLightExtensionBundle extends AbstractExtensionBundle {
	
	public static final String NAMESPACE = "http://iaas.uni-stuttgart.de/bpel/extensions/bpel4restlight";
	
	
	/** {@inheritDoc} */
	@Override
	public String getNamespaceURI() {
		return NAMESPACE;
	}
	
	/** {@inheritDoc} */
	@Override
	public void registerExtensionActivities() {
		super.registerExtensionOperation("logNodes", EPRDemoOperation.class);
		super.registerExtensionOperation("PUT", Bpel4RestLightOperation.class);
		super.registerExtensionOperation("GET", Bpel4RestLightOperation.class);
		super.registerExtensionOperation("POST", Bpel4RestLightOperation.class);
		super.registerExtensionOperation("DELETE", Bpel4RestLightOperation.class);
	}
}
