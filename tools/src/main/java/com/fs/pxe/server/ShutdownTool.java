/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fs.utils.cli.BaseCommandlineTool;
import com.fs.utils.cli.ConsoleFormatter;
import com.fs.utils.cli.FlagWithArgument;
import com.fs.utils.cli.Fragments;
import com.fs.utils.cli.CommandlineFragment;
import com.fs.utils.jmx.JMXConstants;
import com.fs.pxe.tools.CommandContext;
import com.fs.pxe.tools.ExecutionException;
import com.fs.pxe.tools.mngmt.JmxCommand;
import com.fs.pxe.tools.ClineCommandContext;
import com.fs.pxe.kernel.PxeKernelMBean;

import java.util.Set;
import java.io.IOException;

import javax.management.ObjectInstance;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;

public class ShutdownTool extends BaseCommandlineTool {
	private static final Log LOG = LogFactory.getLog(ShutdownTool.class);
  
	private static final String SYNOPSIS =
		"shutdown the PXE Kernel (usually means terminating the PXE standalone instance).";

	protected static final FlagWithArgument JMX_URL_F =
		new FlagWithArgument("jmxurl","url",
			"JMX service URL (JSR-160) for connecting to the server.",true);

  private static final Fragments STYLE = new Fragments(new CommandlineFragment[] {
      LOGGING, JMX_URL_F
   });
  
	protected static void processJmxUrl(JmxCommand c) {
		if (JMX_URL_F.isSet()) {
			c.setJmxUrl(JMX_URL_F.getValue());
		}
	}
  
	public static void main(String[] args) {
		setClazz(ShutdownTool.class);
		if (HELP.matches(args)) {
			ConsoleFormatter.printSynopsis(
				getProgramName(),SYNOPSIS,
				new Fragments[] {STYLE,HELP});
			System.exit(0);
		}
		
    if (!STYLE.matches(args)) {
     consoleErr("INVALID COMMANDLINE: Try \"" + getProgramName() + " -h\" for help.");
      System.exit(-1);
    }
    
		registerTempFileManager();
		initLogging();
		Shutdown shutdown = new Shutdown();
		processJmxUrl(shutdown);
    if (JMX_URL_F.isSet())
        shutdown.setJmxUrl(JMX_URL_F.getValue());

		try {
			shutdown.execute(new ClineCommandContext(LOG));
			System.exit(0);
		} catch (ExecutionException ee) {
			ee.printStackTrace();
			consoleErr(ee.getMessage());
			System.exit(-1);
		}
	}

	public static class Shutdown extends JmxCommand {

		public void execute(CommandContext context) throws ExecutionException {
			if (getKernel() == null)
				throw new ExecutionException("Unable to find PXE Kernel MBean");
			getKernel().shutdown();
		}

		private PxeKernelMBean kernel;

		protected PxeKernelMBean getKernel() throws ExecutionException {
			if (kernel != null)
				return kernel;
	  
			if(!isConnected())
				connect();

			ObjectName query;
			try {
				query = createKernelObjectQuery();
			} catch (MalformedObjectNameException mone) {
				throw new ExecutionException(mone);
			}
			Set result;
			try {
				result = getConnection().queryMBeans(query,null);
			} catch (IOException ioe) {
				throw new ExecutionException("Unable to query names: " + ioe.getMessage(),ioe);
			}
			if (result.size() > 0) {
				ObjectInstance objectInstance = (ObjectInstance) result.iterator().next();
				ObjectName objectName = objectInstance.getObjectName();
				kernel = (PxeKernelMBean)
					MBeanServerInvocationHandler.newProxyInstance(getConnection(),
						objectName, PxeKernelMBean.class, false);
			}
			return kernel;
		} 
	
		static ObjectName createKernelObjectQuery()
			throws MalformedObjectNameException {
			String query = JMXConstants.JMX_DOMAIN + ":type=Kernel,*";
			return new ObjectName(query);
		}
	}
}
