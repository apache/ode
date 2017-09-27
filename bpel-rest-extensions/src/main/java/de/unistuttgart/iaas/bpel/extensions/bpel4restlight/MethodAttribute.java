package de.unistuttgart.iaas.bpel.extensions.bpel4restlight;

/**
 * 
 * Copyright 2011 IAAS University of Stuttgart <br>
 * <br>
 * 
 * This enum is used to decouple DOM-Attribute names from their
 * String-representation within a certain library (for portability issues)
 * 
 * @author uwe.breitenbuecher@iaas.uni-stuttgart.de
 * 
 */
public enum MethodAttribute {
	REQUESTURI, REQUESTPAYLOADVARIABLE, RESPONSEPAYLOADVARIABLE, STATUSCODEVARIABLE, ACCEPTHEADER;
}