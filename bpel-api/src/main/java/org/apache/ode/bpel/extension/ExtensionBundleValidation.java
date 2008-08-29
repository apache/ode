package org.apache.ode.bpel.extension;

import javax.xml.namespace.QName;
import java.util.Map;

/**
 * Compile-time validation of extensions implemented by your bundle.
 */
public interface ExtensionBundleValidation {

    Map<QName, ExtensionValidator> getExtensionValidators();
    
}
