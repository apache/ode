
package org.apache.ode.jbi;

import javax.jbi.JBIException;
import javax.jbi.component.Bootstrap;
import javax.jbi.component.InstallationContext;
import javax.management.ObjectName;

/**
 * ODE Implementation of the JBI {@link  javax.jbi.component.Bootstrap}
 * interface. This is just a place-holder, nothing gets done in the
 * bootstrap at this point. 
 */
public class OdeBootstrap implements Bootstrap {
  
  @SuppressWarnings("unused")
  private InstallationContext _installContext;

	/**
	 * Cleans up any resources allocated by the bootstrap implementation,
	 * including deregistration of the extension MBean, if applicable. This method
	 * will be called after the onInstall() or onUninstall() method is called,
	 * whether it succeeds or fails.
	 *
	 * @throws javax.jbi.JBIException
	 *           when cleanup processing fails to complete successfully.
	 */
	public void cleanUp() throws JBIException {
	}

	/**
	 * Get the JMX ObjectName for the optional installation configuration MBean
	 * for this BPE. If there is none, the value is null.
	 *
	 * @return ObjectName the JMX object name of the installation configuration
	 *         MBean or null if there is no MBean.
	 */
	public ObjectName getExtensionMBeanName() {
		return null;
	}

	/**
	 * Called to initialize the BPE bootstrap.
	 *
	 * @param installContext
	 *          is the context containing information from the install command and
	 *          from the BPE jar file.
	 *
	 * @throws javax.jbi.JBIException
	 *           when there is an error requiring that the installation be
	 *           terminated.
	 */
	public void init(InstallationContext installContext) throws JBIException {
    _installContext = installContext;
	}

	/**
	 * Called at the beginning of installation.
	 *
	 * @throws javax.jbi.JBIException
	 *           when there is an error requiring that the installation be
	 *           terminated.
	 */
	public void onInstall() throws JBIException {
	}

	/**
	 * Called at the beginning of uninstallation.
	 *
	 * @throws javax.jbi.JBIException
	 *           when there is an error requiring that the uninstallation be
	 *           terminated.
	 */
	public void onUninstall() throws JBIException {
    
	}

}
